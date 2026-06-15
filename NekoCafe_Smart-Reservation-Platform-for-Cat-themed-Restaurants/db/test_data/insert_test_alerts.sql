-- ============================================================
-- 告警系统测试数据
-- 用法：psql -U postgres -d postgres -f 此文件
-- ============================================================

-- ─────────────────────────────────────────────
-- 1. 清理旧的测试数据
-- ─────────────────────────────────────────────
-- 注意：ON CONFLICT DO UPDATE 中的 version 列需要加表名前缀
DELETE FROM shift_exceptions WHERE exception_id >= 10;
DELETE FROM reservations WHERE reservation_id >= 90;
UPDATE table_status SET status = 'IDLE'::status_enum, current_reservation_id = NULL WHERE table_id IN (101, 103);

-- ─────────────────────────────────────────────
-- 2. 基础数据：班次
-- ─────────────────────────────────────────────
INSERT INTO staff_shifts (shift_id, shift_name, start_time, end_time) VALUES
(1, '早班', '08:00', '16:00'),
(2, '中班', '12:00', '20:00'),
(3, '晚班', '16:00', '00:00'),
(4, '通班', '10:00', '22:00'),
(5, '兽医巡诊班', '14:00', '18:00');

-- ─────────────────────────────────────────────
-- 3. 告警数据：各种类型 × 各种状态
-- ─────────────────────────────────────────────

-- 3a. PENDING（待处理）告警——前端应显示【已知晓】按钮
INSERT INTO shift_exceptions (exception_id, store_id, staff_id, exception_date, type, original_schedule_id, new_schedule_id, status, approver_id, reason, created_at) VALUES
(10, 1, 2, '2026-06-12', 'OVERTIME', 2, NULL, 'PENDING', NULL, '客人超时占座 | 桌号 102 | 预约 1002', '2026-06-12 14:00:00'),
(11, 1, 3, '2026-06-12', 'NO_SHOW', 1, NULL, 'PENDING', NULL, '客人预约未到店 | 预约 1005 | 时间 2026-06-12 13:00', '2026-06-12 13:20:00'),
(12, 2, 4, '2026-06-12', 'SWAP', 2, 3, 'PENDING', NULL, '与同事调班申请，中班换晚班', '2026-06-12 11:00:00'),
(13, 1, 3, '2026-06-15', 'LEAVE', 1, NULL, 'PENDING', NULL, '年假申请，6月15日休息一天', '2026-06-12 09:30:00');

-- 3b. ACKNOWLEDGED（已知晓）——前端应显示【✅ 解决】按钮
INSERT INTO shift_exceptions (exception_id, store_id, staff_id, exception_date, type, original_schedule_id, new_schedule_id, status, approver_id, reason, created_at) VALUES
(20, 1, 2, '2026-06-11', 'OVERTIME', 2, NULL, 'ACKNOWLEDGED', 1, '客人超时占座 | 桌号 101 | 预约 1001', '2026-06-11 15:30:00'),
(21, 1, 3, '2026-06-10', 'LEAVE', 1, NULL, 'ACKNOWLEDGED', 1, '病假：感冒需要休息一天', '2026-06-09 08:00:00'),
(22, 2, 4, '2026-06-11', 'SWAP', 3, 2, 'ACKNOWLEDGED', 2, '临时换班：晚班换中班', '2026-06-10 18:00:00');

-- 3c. RESOLVED（已解决）——前端应显示 ✓ 已处理
INSERT INTO shift_exceptions (exception_id, store_id, staff_id, exception_date, type, original_schedule_id, new_schedule_id, status, approver_id, reason, created_at) VALUES
(30, 1, 2, '2026-06-08', 'OVERTIME', 2, NULL, 'RESOLVED', 1, '客人超时占座 | 桌号 104 | 预约 998\n[解决] 已协调客人离席并打扫桌位', '2026-06-08 16:00:00'),
(31, 2, 4, '2026-06-07', 'NO_SHOW', 3, NULL, 'RESOLVED', 2, '客人预约未到店 | 预约 997\n[解决] 已释放桌位，重新开放预约', '2026-06-07 14:30:00');

-- 3d. APPROVED（已通过）和 REJECTED（已驳回）
INSERT INTO shift_exceptions (exception_id, store_id, staff_id, exception_date, type, original_schedule_id, new_schedule_id, status, approver_id, reason, created_at) VALUES
(40, 1, 3, '2026-06-14', 'LEAVE', 1, NULL, 'APPROVED', 1, '调休申请：上周末加班补休', '2026-06-10 10:00:00'),
(41, 2, 5, '2026-06-15', 'LEAVE', 5, NULL, 'REJECTED', 2, '事假：人手不足，已驳回', '2026-06-11 09:00:00');

-- ─────────────────────────────────────────────
-- 4. 触发定时任务的数据
--    注意：以下数据让定时任务能自动生成告警
-- ─────────────────────────────────────────────

-- 4a. OCCUPIED 但已结束的桌位 → 触发 OVERTIME 告警
--     预约在 2 小时前结束，但桌位仍为 OCCUPIED
INSERT INTO reservations (reservation_id, user_id, store_id, table_id, cat_profile_id, reservation_time, duration_min, party_size, special_request, order_amount, total_amount, status, created_at, updated_at)
VALUES (90, 1001, 1, 101, 1, NOW() - INTERVAL '2 hours' - INTERVAL '10 minutes', 90, 2, 'test', 100, 100, 'CONFIRMED'::reservation_status, NOW() - INTERVAL '1 day', NOW() - INTERVAL '2 hours');

INSERT INTO table_status (table_id, status, current_reservation_id, version)
VALUES (101, 'OCCUPIED'::status_enum, 90, 1)
ON CONFLICT (table_id) DO UPDATE SET status = 'OCCUPIED'::status_enum, current_reservation_id = 90, version = table_status.version + 1;

-- 4b. BOOKED 且已过预约时间 20 分钟 → 触发 NO_SHOW 告警
INSERT INTO reservations (reservation_id, user_id, store_id, table_id, cat_profile_id, reservation_time, duration_min, party_size, special_request, order_amount, total_amount, status, created_at, updated_at)
VALUES (91, 1002, 1, 103, 2, NOW() - INTERVAL '25 minutes', 120, 2, 'test', 80, 80, 'BOOKED'::reservation_status, NOW() - INTERVAL '2 days', NOW() - INTERVAL '25 minutes');

-- ─────────────────────────────────────────────
-- 5. 验证查询
-- ─────────────────────────────────────────────
-- 查看所有告警（按类型分组）
SELECT '--- 告警列表（按状态）---' AS info;
SELECT exception_id, store_id, type, status, reason FROM shift_exceptions ORDER BY status, exception_id;

SELECT '--- 待触发 OVERTIME 的桌位（OCCUPIED + 已超时）---' AS info;
SELECT ts.table_id, ts.status, ts.current_reservation_id, r.reservation_time, r.duration_min
FROM table_status ts
JOIN reservations r ON r.reservation_id = ts.current_reservation_id
WHERE ts.status = 'OCCUPIED'
  AND r.reservation_time + (r.duration_min || ' minutes')::INTERVAL < NOW() - INTERVAL '10 minutes';

SELECT '--- 待触发 NO_SHOW 的预约（BOOKED + 超时 20 分）---' AS info;
SELECT reservation_id, store_id, table_id, reservation_time
FROM reservations
WHERE status = 'BOOKED'
  AND reservation_time + INTERVAL '20 minutes' < NOW();
