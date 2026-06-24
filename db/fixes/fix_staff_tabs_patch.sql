SET client_encoding TO 'UTF8';

-- 补充之前因 staff_id NOT NULL 失败的 2 条告警
INSERT INTO public.shift_exceptions (exception_id, store_id, staff_id, exception_date, type, original_schedule_id, new_schedule_id, status, approver_id, reason, created_at)
VALUES
  (2, 1, 224, '2026-06-22', 'equipment', NULL, NULL, 'PENDING', NULL, '3号桌台灯闪烁故障，影响顾客用餐体验，需联系维修', now()),
  (3, 1, 226, '2026-06-22', 'overstay',  NULL, NULL, 'PENDING', NULL, '5号桌预约超时逗留超过30分钟，可能需要催促或加时', now())
ON CONFLICT (exception_id) DO NOTHING;

-- 同步修复 ACKNOWLEDGED 和 RESOLVED 中 staff_id 为 NULL 的记录（4,5,6,7）
UPDATE public.shift_exceptions SET staff_id = 224 WHERE exception_id = 4 AND staff_id IS NULL;
UPDATE public.shift_exceptions SET staff_id = 226 WHERE exception_id IN (5, 6) AND staff_id IS NULL;
UPDATE public.shift_exceptions SET staff_id = 224 WHERE exception_id = 7 AND staff_id IS NULL;

-- 验证：告警全量
SELECT '=== store_id=1 全部告警 ===' AS info;
SELECT exception_id, staff_id, type, status,
       LEFT(reason, 35) AS reason_short,
       to_char(created_at, 'MM-DD HH24:MI') AS time
FROM shift_exceptions WHERE store_id=1 ORDER BY status, created_at DESC;

SELECT 'Done!' AS result;
