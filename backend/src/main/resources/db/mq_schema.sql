-- ============================================================
-- NekoCafé 高并发 / 消息队列改造 —— 数据库变更脚本 (PostgreSQL)
-- 在业务库 (postgres) 上执行一次即可。全部使用 IF NOT EXISTS，可重复执行。
-- ============================================================

-- ------------------------------------------------------------
-- 1. 本地消息表 (Transactional Outbox)
--    业务事务内写入一条 outbox 记录，由后台轮询器投递到 RabbitMQ，
--    解决「DB 提交了但消息没发 / 消息发了但 DB 回滚」的双写问题（至少一次投递）。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS outbox_message (
    id           BIGSERIAL    PRIMARY KEY,
    exchange     VARCHAR(128) NOT NULL,
    routing_key  VARCHAR(128) NOT NULL,
    payload      TEXT         NOT NULL,            -- JSON 消息体
    msg_type     VARCHAR(64)  NOT NULL,            -- 业务类型，便于排查
    status       VARCHAR(16)  NOT NULL DEFAULT 'PENDING',  -- PENDING / SENT
    retry_count  INT          NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    sent_at      TIMESTAMPTZ
);

-- 轮询器只扫 PENDING，按 id 升序投递
CREATE INDEX IF NOT EXISTS idx_outbox_pending
    ON outbox_message (id) WHERE status = 'PENDING';

-- ------------------------------------------------------------
-- 2. 消费幂等 / 结算协调表
--    message_id 唯一。消费者与「取消订单」都用 INSERT ... ON CONFLICT 抢占同一行，
--    谁先抢到谁决定结果，从根本上避免「取消发生在异步加积分之前」导致的积分错乱。
--      applied = TRUE  : 副作用已真正落库（消费者在同一事务内写入）
--      applied = FALSE : 取消提前打的墓碑（副作用将被跳过）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mq_consumed (
    message_id  VARCHAR(128) PRIMARY KEY,
    applied     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- 3. 预约并发相关索引
--    createReservation 的时段冲突检查按 (table_id, status) 过滤，加索引避免全表扫描，
--    缩短分布式锁临界区时间，直接降低 P95。
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_reservations_table_status
    ON public.reservations (table_id, status);

CREATE INDEX IF NOT EXISTS idx_reservations_user
    ON public.reservations (user_id);

-- ------------------------------------------------------------
-- 4.【DB 级强一致兜底·默认启用】时段不重叠排他约束 (btree_gist)
--    这是「绝不超卖」的最终防线：Redis 分布式锁是性能优化（让不冲突的请求并行），
--    但锁租约在高负载下可能在事务提交前过期 → 仍存在并发插入窗口。该排他约束由
--    数据库强制保证同一桌位的活跃时段永不重叠，无论锁是否抖动 / Redis 是否故障。
--
--    要点（与本库实际 schema 对齐，勿改成注释里旧版的 tstzrange）：
--      · reservation_time 是 TIMESTAMP（无时区）→ 必须用 tsrange；用 tstzrange 会引入
--        timestamp→timestamptz 的 STABLE 转换，导致 EXCLUDE 索引报 "must be IMMUTABLE" 而建失败。
--      · duration_min 可空 → COALESCE(...,120)，否则 NULL 会让区间变成无穷大挡掉整桌。
--      · 命中冲突抛 23P01，ReservationServiceImpl 已捕获并转友好提示。
--
--    ⚠️ 若库中已存在重叠的历史数据，ADD CONSTRAINT 会失败。先用下面这条排查并清理：
--      SELECT a.reservation_id, b.reservation_id
--      FROM public.reservations a JOIN public.reservations b
--        ON a.table_id = b.table_id AND a.reservation_id < b.reservation_id
--       AND a.status IN ('BOOKED','CONFIRMED')
--       AND b.status IN ('BOOKED','CONFIRMED')
--       AND tsrange(a.reservation_time, a.reservation_time + make_interval(mins => COALESCE(a.duration_min,120)))
--        && tsrange(b.reservation_time, b.reservation_time + make_interval(mins => COALESCE(b.duration_min,120)));
-- ------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS btree_gist;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'excl_reservation_overlap') THEN
        ALTER TABLE public.reservations
            ADD CONSTRAINT excl_reservation_overlap
            EXCLUDE USING gist (
                table_id WITH =,
                tsrange(reservation_time,
                        reservation_time + make_interval(mins => COALESCE(duration_min, 120))) WITH &&
            ) WHERE (status IN ('BOOKED','CONFIRMED'));
    END IF;
END $$;
