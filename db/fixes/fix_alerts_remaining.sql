SET client_encoding TO 'UTF8';

-- 补充之前因 batch 中途失败未插入的 ACKNOWLEDGED 和 RESOLVED 告警
INSERT INTO public.shift_exceptions (exception_id, store_id, staff_id, exception_date, type, original_schedule_id, new_schedule_id, status, approver_id, reason, created_at)
VALUES
  -- ACKNOWLEDGED（已知晓）
  (4, 1, 224, '2026-06-21', 'no_show',    8,  NULL, 'ACKNOWLEDGED', 226, '昨日早班因交通原因迟到，已电话确认无大碍', now()),
  (5, 1, 226, '2026-06-21', 'equipment', NULL, NULL, 'ACKNOWLEDGED', 226, '空调出风口异响，已安排明日维修人员上门检查', now()),
  -- RESOLVED（已处理）
  (6, 1, 229, '2026-06-20', 'overstay',  NULL, NULL, 'RESOLVED',   226, '猫咪活动区逗留时间过长，经沟通后顾客配合离开，后续建议设置更明确的提示牌', now()),
  (7, 1, 224, '2026-06-19', 'equipment', NULL, NULL, 'RESOLVED',   224, '收银机打印纸卡纸问题，已更换新纸卷并清洁打印头', now())
ON CONFLICT (exception_id) DO NOTHING;

SELECT status, count(*) FROM shift_exceptions WHERE store_id=1 GROUP BY status ORDER BY status;
