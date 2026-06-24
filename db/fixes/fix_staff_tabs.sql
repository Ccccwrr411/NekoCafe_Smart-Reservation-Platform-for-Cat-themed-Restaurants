SET client_encoding TO 'UTF8';

-- ============================================================
-- 员工工作台 4 个空 Tab 补充数据
-- 目标：消息中心 / 叫号(已叫号+过号) / 告警 / 考勤
--
-- 当前登录用户：店员20001 = user_id=224，门店 store_id=1（海淀中关村店）
-- ============================================================

-- ==================== 第 1 部分：消息中心补充 ====================
-- selectByStore 条件：store_id = X AND user_id IS NULL（广播通知）
-- 当前 store_id=1 只有 2 条广播通知（103,104），补到 10+ 条

INSERT INTO public.notifications (notification_id, store_id, user_id, target_role, type, title, content, related_type, related_id, is_read, created_at)
VALUES
  -- 新订单类（未读）
  (110, 1, NULL, 'STAFF', 'order_new',      '新预约提醒',     '有新的预约 #5001 已提交，等待确认',                          'RESERVATION', 5001, false, '2026-06-20 15:05:00'),
  (111, 1, NULL, 'STAFF', 'order_new',      '新预约提醒',     '有新的预约 #5002 已提交，6人桌需要提前准备',                 'RESERVATION', 5002, false, '2026-06-20 16:10:00'),
  -- 上菜/制作类（已读 + 未读混合）
  (112, 1, NULL, 'STAFF', 'order_making',   '制作提醒',       '预约 #3002 的订单已进入制作阶段',                            'RESERVATION', 3002, true,  '2026-06-22 12:05:00'),
  (113, 1, NULL, 'STAFF', 'order_serving',  '上菜提醒',       '预约 #4001 的猫爪蛋糕套餐正在上菜中',                       'RESERVATION', 4001, false, '2026-06-22 11:00:00'),
  -- 系统公告类
  (114, 1, NULL, NULL,    'system',         '系统维护通知',    '系统将于今晚 23:00-23:30 进行例行维护',                     NULL,        NULL, false, '2026-06-21 09:00:00'),
  (115, 1, NULL, NULL,    'promotion',      '新品上线',        '夏日限定「芒果猫爪冰沙」已上架，售价 ¥32',                  NULL,        NULL, true,  '2026-06-20 10:00:00'),
  -- 排队叫号类
  (116, 1, NULL, 'STAFF', 'queue_update',   '排队更新',        '当前等待队列 2 组，请及时处理',                              NULL,        NULL, false, '2026-06-22 12:25:00'),
  -- 退款/异常类
  (117, 1, NULL, 'STAFF', 'refund_request', '退款待审核',      '预约 #7001 的退款申请等待审核中',                             'REFUND',     1,    true,  '2026-06-15 10:35:00')
ON CONFLICT (notification_id) DO NOTHING;


-- ==================== 第 2 部分：叫号记录补充 ====================
-- 当前 store_id=1 队列状态：
--   Q001 → SEATED（已入座）
--   Q002 → WAITING（等待中）→ 改为 CALLED（模拟已叫号）
--   Q004 → WAITING（等待中）

-- 2.1 将 Q002 标记为 CALLED（店员已叫号），Q004 保持 WAITING
UPDATE public.queue SET status = 'CALLED', called_at = '2026-06-22 12:30:00' WHERE queue_id = 2;

-- 2.2 插入更多已叫号 / 已确认 / 过号记录
INSERT INTO public.queue (queue_id, store_id, user_id, party_size, preferred_table_type, status, queue_number, called_at, seated_table_id, created_at)
VALUES
  -- 已叫号但顾客尚未确认（CALLED）
  (6,  1, 256, 6, '六人桌',   'CALLED', 'Q006', '2026-06-22 13:00:00', NULL, '2026-06-22 12:45:00'),
  -- 已叫号且顾客已确认入座（KNOWN → 实际对应 SEATED 但这里用 KNOWN 表示"已确认叫号"）
  -- 注意：前端查询 CALLED 和 KNOWN 状态一起显示在"已叫号"tab
  (7,  1, 230, 2, '双人桌',   'KNOWN',  'Q007', '2026-06-22 11:40:00', 1,    '2026-06-22 11:20:00'),
  (8,  1, 242, 2, '双人桌',   'KNOWN',  'Q008', '2026-06-22 12:15:00', 9,    '2026-06-22 11:55:00'),
  -- 过号（MISSED）：超时未确认自动过号
  (9,  1, 237, 4, '四人桌',   'MISSED', 'Q009', '2026-06-22 10:30:00', NULL, '2026-06-22 10:15:00'),
  (10, 1, 249, 4, '四人桌',   'MISSED', 'Q010', '2026-06-22 14:00:00', NULL, '2026-06-22 13:45:00')
ON CONFLICT (queue_id) DO NOTHING;


-- ==================== 第 3 部分：告警数据（shift_exceptions）====================
-- getAlerts 查询条件：store_id = X （返回该门店所有异常记录）
-- 告警类型(type)：overstay(超时逗留) / no_show(未到店) / equipment(设备故障) 等
-- 状态(status)：PENDING / ACKNOWLEDGED / RESOLVED

INSERT INTO public.shift_exceptions (exception_id, store_id, staff_id, exception_date, type, original_schedule_id, new_schedule_id, status, approver_id, reason, created_at)
VALUES
  -- === PENDING 待处理告警（显示在"待处理"Tab）===
  (1,  1, 225, '2026-06-22', 'no_show',    2,  NULL, 'PENDING', NULL, '店员20002（user_id=225）今日中班未按时到岗，已超时15分钟，请确认情况',           now()),
  (2,  1, 224, '2026-06-22', 'equipment', NULL, NULL, 'PENDING', NULL, '3号桌台灯闪烁故障，影响顾客用餐体验，需联系维修',                                   now()),
  (3,  1, 226, '2026-06-22', 'overstay',  NULL, NULL, 'PENDING', NULL, '5号桌预约 #1005 超时逗留超过30分钟（原定时长120分钟），可能需要催促或加时',          now()),

  -- === ACKNOWLEDGED 已知晓（显示在"已知晓"Tab）===
  (4,  1, 224, '2026-06-21', 'no_show',    8,  NULL, 'ACKNOWLEDGED', 226, '昨日早班因交通原因迟到，已电话确认无大碍',                                           now()),
  (5,  1, 226, '2026-06-21', 'equipment', NULL, NULL, 'ACKNOWLEDGED', 226, '空调出风口异响，已安排明日维修人员上门检查',                                               now()),

  -- === RESOLVED 已解决（显示在"已处理"Tab）===
  (6,  1, 229, '2026-06-20', 'overstay',  NULL, NULL, 'RESOLVED',   226, '猫咪活动区逗留时间过长，经沟通后顾客配合离开，后续建议设置更明确的提示牌',             now()),
  (7,  1, 224, '2026-06-19', 'equipment', NULL, NULL, 'RESOLVED',   224, '收银机打印纸卡纸问题，已更换新纸卷并清洁打印头',                                             now())
ON CONFLICT (exception_id) DO NOTHING;


-- ==================== 第 4 部分：考勤记录（shift_exceptions）====================
-- getMyExceptions 查询条件：store_id = X AND staff_id = Y（当前用户 224）
-- 考勤类型(type)：LEAVE(请假) / OVERTIME(加班) / SWAP(调班)
-- 状态(status)：PENDING(待审批) / APPROVED(已通过) / REJECTED(已驳回)

INSERT INTO public.shift_exceptions (exception_id, store_id, staff_id, exception_date, type, original_schedule_id, new_schedule_id, status, approver_id, reason, created_at)
VALUES
  -- === 店员 224 的考勤记录 ===
  -- PENDING 请假（待审批 Tab）
  (10, 1, 224, '2026-06-25', 'LEAVE',    8, NULL, 'PENDING',  NULL, '家中有事需请假一天',                        now()),
  -- APPROVED 加班（已通过 Tab）
  (11, 1, 224, '2026-06-28', 'OVERTIME', NULL, NULL, 'APPROVED', 226, '端午假期期间主动申请加班，负责晚班时段',    now()),
  (12, 1, 224, '2026-06-29', 'OVERTIME', NULL, NULL, 'APPROVED', 226, '周末客流高峰期协助门店运营',                now()),
  -- REJECTED 调班（已驳回 Tab）
  (13, 1, 224, '2026-06-26', 'SWAP',     8, 9,   'REJECTED', 226, '申请与店员20002调班，但因双方排班冲突被驳回',  now()),
  -- APPROVED 请假（已通过 Tab）
  (14, 1, 224, '2026-07-01', 'LEAVE',    NULL,NULL, 'APPROVED', 226, '个人事务请假半天',                          now()),
  -- PENDING 调班（待审批 Tab）
  (15, 1, 224, '2026-07-03', 'SWAP',     NULL,2,   'PENDING',  NULL, '申请将7月3日的早班与中班对调',              now()),

  -- === 其他员工的考勤记录（丰富告警数据）===
  (16, 1, 225, '2026-06-24', 'LEAVE',    9, NULL, 'PENDING',  NULL, '身体不适需休息一天',                        now()),
  (17, 1, 225, '2026-06-27', 'OVERTIME', NULL,NULL, 'APPROVED', 226, '周末加班支援望京店',                        now()),
  (18, 1, 226, '2026-06-23', 'LEAVE',    3, NULL, 'APPROVED', 224, '店长培训外出',                               now())
ON CONFLICT (exception_id) DO NOTHING;


-- ==================== 重置序列 ====================
SELECT setval('public.notifications_notification_id_seq', COALESCE((SELECT MAX(notification_id) FROM public.notifications), 1));
SELECT setval('public.queue_queue_id_seq',            COALESCE((SELECT MAX(queue_id) FROM public.queue), 1));
SELECT setval('public.shift_exceptions_exception_id_seq', COALESCE((SELECT MAX(exception_id) FROM public.shift_exceptions), 1));


-- ==================== 验证结果 ====================
SELECT '--- 消息中心(store_id=1 广播通知) ---' AS info;
SELECT notification_id, target_role, type, title, is_read,
       to_char(created_at, 'MM-DD HH24:MI') AS time
FROM notifications WHERE store_id=1 AND user_id IS NULL ORDER BY created_at DESC;

SELECT '--- 叫号队列(store_id=1 全部状态) ---' AS info;
SELECT queue_number, status, party_size, preferred_table_type,
       CASE WHEN called_at IS NOT NULL THEN to_char(called_at, 'HH24:MI') END AS called_time,
       to_char(created_at, 'MM-DD HH24:MI') AS created_time
FROM queue WHERE store_id=1 ORDER BY queue_id;

SELECT '--- 告警(store_id=1 全部) ---' AS info;
SELECT exception_id, type, status,
       LEFT(reason, 40) AS reason_short,
       to_char(created_at, 'MM-DD HH24:MI') AS time
FROM shift_exceptions WHERE store_id=1 ORDER BY created_at DESC;

SELECT '--- 考勤(staff_id=224 我的申请) ---' AS info;
SELECT exception_id, type, status, exception_date,
       LEFT(reason, 30) AS reason_short
FROM shift_exceptions WHERE store_id=1 AND staff_id=224 ORDER BY created_at DESC;

SELECT 'Done!' AS result;
