-- ============================================================
-- 猫咪档案数据修复 v2
-- 修复: 图片URL 404 / gender null / vaccineDue null / desc null
-- 用法: set PGCLIENTENCODING=UTF8 && psql -h ... -d postgres -f "fix_cat_profiles.sql"
--
-- 图片 URL 说明：
--   使用阿里云 OSS 公网直接访问 URL（无需后端 OSS SDK）
--   OSS bucket: nekocafe-images
--   region:     oss-cn-beijing
--   URL 模式:   https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_{cat_id}.png
--   请确认 OSS bucket 中已上传对应图片，且 bucket 设为公共读（或图片设有签名 URL）
-- ============================================================

SET client_encoding TO 'UTF8';

-- ══════════ 1. 修复图片 URL：改为阿里云 OSS 公网直接访问地址 ══════════

UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_1.png'  WHERE cat_id = 1;
UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_2.png'  WHERE cat_id = 2;
UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_3.png'  WHERE cat_id = 3;
UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_4.png'  WHERE cat_id = 4;
UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_5.png'  WHERE cat_id = 5;
UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_6.png'  WHERE cat_id = 6;
UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_7.png'  WHERE cat_id = 7;
UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_8.png'  WHERE cat_id = 8;
UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_9.png'  WHERE cat_id = 9;
UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_10.png' WHERE cat_id = 10;
UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_11.png' WHERE cat_id = 11;
UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_12.png' WHERE cat_id = 12;
UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_13.png' WHERE cat_id = 13;
UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_14.png' WHERE cat_id = 14;
UPDATE cat_profiles SET avatar_url = 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_15.png' WHERE cat_id = 15;

-- ══════════ 2. 补充疫苗健康记录（让更多猫有 vaccineDue） ══════════
INSERT INTO public.cat_health_records (record_id, cat_id, record_type, record_value, record_date, note, staff_id, created_at)
VALUES
  -- 给之前没有疫苗记录的猫补充疫苗数据
  (101, 2,  'VACCINE',   '猫三联',     '2025-12-01', 'nextDue=2026-12-01',    229, now()),
  (102, 3,  'VACCINE',   '狂犬疫苗',   '2025-11-15', 'nextDue=2026-11-15',    229, now()),
  (103, 4,  'VACCINE',   '猫三联',     '2026-01-20', 'nextDue=2027-01-20',    229, now()),
  (104, 7,  'VACCINE',   '猫三联',     '2026-02-10', 'nextDue=2027-02-10',    229, now()),
  (105, 9,  'VACCINE',   '狂犬疫苗',   '2026-03-05', 'nextDue=2027-03-05',    229, now()),
  (106, 14, 'VACCINE',   '猫三联',     '2026-04-01', 'nextDue=2027-04-01',    229, now())
ON CONFLICT (record_id) DO NOTHING;

-- ══════════ 3. 补充互动记录（让详情页有内容） ══════════
INSERT INTO public.cat_health_records (record_id, cat_id, record_type, record_value, record_date, note, staff_id, created_at)
VALUES
  (201, 2,  'INTERACTION', '梳毛',       '2026-06-10', 'mood=happy|喜欢被梳理毛发',      229, now()),
  (202, 2,  'INTERACTION', '喂零食',     '2026-06-15', 'mood=happy|抢食积极',             229, now()),
  (203, 3,  'INTERACTION', '玩耍',       '2026-06-08', 'mood=neutral|对玩具一般般',       229, now()),
  (204, 4,  'INTERACTION', '抱抱',       '2026-06-12', 'mood=happy|粘人撒娇',             229, now()),
  (205, 7,  'INTERACTION', '逗猫棒',     '2026-06-14', 'mood=happy|追得很开心',           229, now()),
  (206, 9,  'INTERACTION', '撸毛',       '2026-06-16', 'mood=happy|呼噜声很大',           229, now())
ON CONFLICT (record_id) DO NOTHING;

-- ══════════ 4. 补充第二条体重记录（让体重趋势图能画出折线，需要 ≥2 个数据点） ══════════
-- 前端 drawWeightChart() 要求 labels.length >= 2 才会绘制
-- 每只已有体重的猫补充一条 3~4 个月前的历史记录
INSERT INTO public.cat_health_records (record_id, cat_id, record_type, record_value, record_date, note, staff_id, created_at)
VALUES
  -- 雪球(cat1): 3.0kg → 3.2kg（缓慢增长）
  (301, 1, 'WEIGHT', '3.0kg', '2026-03-10', '年初体检',              229, now()),
  -- 橘长(cat2): 6.0kg → 5.8kg（减肥有效）
  (302, 2, 'WEIGHT', '6.0kg', '2026-03-12', '开始控制饮食前',         229, now()),
  -- 煤老板(cat3): 4.7kg → 4.8kg（稳定）
  (303, 3, 'WEIGHT', '4.7kg', '2026-02-20', '常规称重',              229, now()),
  -- 咖啡(cat4): 3.5kg → 4.5kg（幼猫快速成长）
  (304, 4, 'WEIGHT', '3.5kg', '2026-03-05', '幼猫期',                229, now()),
  -- 元宝(cat5): 6.8kg → 6.5kg（略减）
  (305, 5, 'WEIGHT', '6.8kg', '2026-03-15', '偏重',                  229, now()),
  -- 布丁(cat6): 3.0kg → 3.5kg（成长中）
  (306, 6, 'WEIGHT', '3.0kg', '2026-01-08', '幼猫期',                229, now()),
  -- 奶糖(cat7): 3.9kg → 4.2kg（稳步增长）
  (307, 7, 'WEIGHT', '3.9kg', '2026-02-15', '正常',                  229, now()),
  -- 包子(cat8): 5.2kg → 5.5kg（稳定）
  (308, 8, 'WEIGHT', '5.2kg', '2026-04-10', '常规称重',              229, now()),
  -- 可乐(cat9): 2.5kg → 2.8kg（成长）
  (309, 9, 'WEIGHT', '2.5kg', '2026-03-18', '德文卷毛体型小',        229, now()),
  -- 薯条(cat10): 3.7kg → 4.0kg（增重）
  (310, 10, 'WEIGHT', '3.7kg', '2026-03-25', '正常',                  229, now()),
  -- 补充第三条 / 第二条体重记录，确保每只有体重的猫都有 ≥2 个数据点
  (311, 3,  'WEIGHT', '4.5kg', '2026-05-15', '近期称重',             229, now()),
  (312, 5,  'WEIGHT', '6.5kg', '2026-05-20', '减肥中',               229, now()),
  (313, 7,  'WEIGHT', '4.0kg', '2026-05-10', '正常',                 229, now()),
  (314, 8,  'WEIGHT', '5.3kg', '2026-05-18', '稳定',                 229, now()),
  (315, 9,  'WEIGHT', '2.7kg', '2026-05-22', '成长良好',             229, now()),
  (316, 10, 'WEIGHT', '3.9kg', '2026-05-25', '略增重',              229, now()),
  (317, 11, 'WEIGHT', '3.5kg', '2026-04-08', '之前记录',             229, now())
ON CONFLICT (record_id) DO NOTHING;

-- 验证结果
SELECT '=== 修复后图片URL（应全部为 oss-cn-beijing.aliyuncs.com）===' AS info;
SELECT cat_id, name, avatar_url FROM cat_profiles ORDER BY cat_id;

SELECT '' AS info;
SELECT '=== 健康记录统计 ===' AS info;
SELECT record_type, COUNT(*) FROM cat_health_records GROUP BY record_type;

SELECT '' AS info;
SELECT '=== 每只猫的疫苗记录数 ===' AS info;
SELECT c.cat_id, c.name,
  (SELECT COUNT(*) FROM cat_health_records h WHERE h.cat_id = c.cat_id AND h.record_type='VACCINE') AS vaccines,
  (SELECT COUNT(*) FROM cat_health_records h WHERE h.cat_id = c.cat_id AND h.record_type='WEIGHT') AS weights,
  (SELECT COUNT(*) FROM cat_health_records h WHERE h.cat_id = c.cat_id AND h.record_type='INTERACTION') AS interactions
FROM cat_profiles c ORDER BY c.cat_id;
