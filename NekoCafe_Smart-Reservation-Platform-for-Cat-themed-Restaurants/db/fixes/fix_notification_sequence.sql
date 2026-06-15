-- 通知表主键序列修复
-- 由于手动插入了 ID=1~11 的数据后，BIGSERIAL 序列未同步，
-- 导致下一条通知插入时产生重复主键错误。
-- 执行此命令将序列重置为当前最大 ID + 1：
SELECT setval('notifications_notification_id_seq', (SELECT COALESCE(MAX(notification_id), 0) + 1 FROM notifications));

-- 验证：查看序列当前值
SELECT currval('notifications_notification_id_seq');

-- 验证：查看通知表最大 ID
SELECT MAX(notification_id) FROM notifications;
