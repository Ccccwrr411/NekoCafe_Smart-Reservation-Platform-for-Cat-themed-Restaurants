-- V004: 通知消息表（覆盖全角色通知）
-- 支持精准推送(user_id)、角色广播(target_role)、全平台广播(all)
CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGSERIAL PRIMARY KEY,
    store_id        INTEGER,                     -- NULL = 系统级通知
    user_id         BIGINT,                      -- NULL = 按角色广播
    target_role     VARCHAR(20),                 -- customer / staff / manager / hq_ops / admin / NULL(全部)
    type            VARCHAR(30) NOT NULL,        -- order_new / order_progress / refund_result / refund_apply / queue_ready / alert_created / system / promotion
    title           VARCHAR(200) NOT NULL,
    content         TEXT,
    related_type    VARCHAR(30),                 -- reservation / refund / queue / system / shift
    related_id      BIGINT,
    is_read         BOOLEAN DEFAULT false,
    created_at      TIMESTAMP DEFAULT now()
);

-- 索引：门店+未读（店员/店长查询）
CREATE INDEX IF NOT EXISTS idx_notif_store_unread
    ON notifications (store_id, is_read)
    WHERE is_read = false;

-- 索引：用户+未读（顾客查询）
CREATE INDEX IF NOT EXISTS idx_notif_user_unread
    ON notifications (user_id, is_read)
    WHERE is_read = false AND user_id IS NOT NULL;

-- 索引：按角色+门店广播（角色查询）
CREATE INDEX IF NOT EXISTS idx_notif_role_store
    ON notifications (target_role, store_id)
    WHERE target_role IS NOT NULL;
