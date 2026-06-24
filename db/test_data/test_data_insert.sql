SET client_encoding TO 'UTF8';
-- ============================================================
-- NekoCafe 测试数据插入脚本
-- 在执行前确保数据库已执行 cleanup.sql（干净状态）
-- 所有表的 ID 从序列当前值开始，不会冲突
-- ============================================================

-- 临时关闭触发器（加速插入，最后重新开启）
SET session_replication_role = replica;

-- ============================================================
-- 第 1 部分：基础数据（stores / tables / staff_shifts / dishes）
-- ============================================================

-- 1.1 门店（如果已存在可跳过）
INSERT INTO public.stores (store_id, name, city, address, longitude, latitude, contact_phone, business_hours, status, image_url)
VALUES
  (1, 'NekoCafe 中关村店', '北京', '海淀区中关村大街1号', 116.316016, 39.984786, '010-88888801', '10:00-22:00', 1, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/stores/store_1.png'),
  (2, 'NekoCafe 望京店',   '北京', '朝阳区望京西路10号', 116.470428, 39.989734, '010-88888802', '10:00-22:00', 1, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/stores/store_2.png'),
  (3, 'NekoCafe 上海静安店', '上海', '静安区南京西路100号', 121.448224, 31.230416, '021-66668801', '10:00-22:00', 1, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/stores/store_3.png')
ON CONFLICT (store_id) DO NOTHING;

-- 1.2 桌位（store_id=1 共8桌，store_id=2 共6桌，store_id=3 共5桌）
INSERT INTO public.tables (table_id, store_id, table_no, capacity, table_type, cat_theme, is_active, top, "left", width, height)
VALUES
  -- store_id=1 (8桌): 上排4张, 下排4张
  (1, 1, 'T001', 2, '窗边双人桌', '雪球主题', true, 20,   20,  120, 100),
  (2, 1, 'T002', 2, '普通双人桌', '橘长主题', true, 20,  185,  120, 100),
  (3, 1, 'T003', 4, '四人桌',      '煤老板主题', true, 20,  350,  140, 110),
  (4, 1, 'T004', 4, '四人桌',      '布丁主题',   true, 20,  515,  140, 110),
  (5, 1, 'T005', 6, '六人桌',      '奶糖主题',   true, 165,  20,  160, 120),
  (6, 1, 'T006', 2, '双人桌',      '可乐主题',   true, 165, 185,  120, 100),
  (7, 1, 'T007', 4, '四人桌',      '薯条主题',   true, 165, 350,  140, 110),
  (8, 1, 'T008', 2, 'VIP双人桌',  '雪球主题',   true, 165, 515,  120, 100),
  -- store_id=2 (6桌): 上排3张, 下排3张
  (9,  2, 'T101', 2, '双人桌', '布丁主题', true, 20,   20,  120, 100),
  (10, 2, 'T102', 2, '双人桌', '奶糖主题', true, 20,  185,  120, 100),
  (11, 2, 'T103', 4, '四人桌', '包子主题', true, 20,  350,  140, 110),
  (12, 2, 'T104', 4, '四人桌', '可乐主题', true, 165,  20,  140, 110),
  (13, 2, 'T105', 6, '六人桌', '薯条主题', true, 165, 185,  160, 120),
  (14, 2, 'T106', 2, '双人桌', '雪球主题', true, 165, 350,  120, 100),
  -- store_id=3 (三里屯店, 5桌): 上排3张, 下排2张（左对齐）
  (15, 3, 'T201', 2, '双人桌',     '橘长主题', true, 20,   20,  120, 100),
  (16, 3, 'T202', 4, '四人桌',     '煤老板主题', true, 20,  185,  140, 110),
  (17, 3, 'T203', 4, '四人桌',     '布丁主题',   true, 20,  350,  140, 110),
  (18, 3, 'T204', 6, '六人桌',     '奶糖主题',   true, 165,  20,  160, 120),
  (19, 3, 'T205', 2, '双人桌',     '可乐主题',   true, 165, 185,  120, 100)
ON CONFLICT (table_id) DO NOTHING;

-- 1.3 桌位状态（所有桌位初始为空闲）
INSERT INTO public.table_status (table_id, status, current_reservation_id, version)
SELECT table_id, 'IDLE'::public.status_enum, NULL, 0
FROM public.tables
ON CONFLICT (table_id) DO UPDATE SET status = 'IDLE', current_reservation_id = NULL, version = 0;

-- 1.4 员工班次模板
INSERT INTO public.staff_shifts (shift_id, shift_name, start_time, end_time)
VALUES
  (1, '早班', '08:00:00', '16:00:00'),
  (2, '中班', '12:00:00', '20:00:00'),
  (3, '晚班', '16:00:00', '22:00:00')
ON CONFLICT (shift_id) DO NOTHING;

-- 1.5 菜品（补充更多菜品）
INSERT INTO public.dishes (dish_id, name, category, price, image_url, description, tags, is_active)
VALUES
  (11, '草莓圣代',     '甜点', 28.00, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/dishes/dish_11.png', '新鲜草莓配香草冰淇淋',   '夏季限定', true),
  (12, '拿铁咖啡',     '咖啡', 32.00, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/dishes/dish_12.png', '经典拿铁，可选燕麦奶',   '经典',     true),
  (13, '猫咪曲奇套餐', '宠物零食', 22.00, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/dishes/dish_13.png', '猫咪造型曲奇+猫薄荷茶', '打卡推荐', true),
  (14, '三明治',       '轻食', 38.00, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/dishes/dish_14.png', '全麦面包配鲜蔬沙拉',   '健康轻食', true),
  (15, '猫咪主题午餐', '主餐', 58.00, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/dishes/dish_15.png', '萌猫造型营养午餐',     '推荐',     true),
  (16, '猫爪奶茶',     '饮品', 26.00, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/dishes/dish_16.png', 'Q弹猫爪果冻+红茶奶茶',  '招牌',     true),
  (17, '草鱼猫饭',     '宠物零食', 18.00, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/dishes/dish_17.png', '猫咪专用营养饭',     '猫咪特供', true),
  (18, '提拉米苏',     '甜点', 42.00, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/dishes/dish_18.png', '意式经典提拉米苏',     '推荐',     true),
  (19, '美式咖啡',     '咖啡', 25.00, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/dishes/dish_19.png', '纯正美式黑咖啡',       '经典',     true),
  (20, '水果茶',       '饮品', 30.00, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/dishes/dish_20.png', '多种水果搭配绿茶底',   '清爽',     true)
ON CONFLICT (dish_id) DO NOTHING;

-- 1.6 门店菜品关联（所有门店都有这些菜品）
INSERT INTO public.store_dishes (store_id, dish_id, price_override, is_available)
SELECT s.store_id, d.dish_id, NULL, true
FROM public.stores s, public.dishes d
WHERE d.is_active = true
ON CONFLICT (store_id, dish_id) DO NOTHING;


-- ============================================================
-- 第 2 部分：用户数据（30个测试顾客 + 保留6个系统用户）
-- ============================================================

-- 2.1 插入测试用户（user_id 从 230 开始，避开已保留的 224~229）
-- 密码均为：123456（BCrypt hash，jBCrypt 0.4 生成）
INSERT INTO public.users (user_id, phone, password_hash, nickname, avatar_url, is_verified, status, created_at, openid)
VALUES
  (230, '13800000100', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '小明', 'https://example.com/avatars/1.png', true, 1, '2026-03-01', 'wx_openid_230'),
  (231, '13800000101', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '小红', 'https://example.com/avatars/2.png', true, 1, '2026-03-02', 'wx_openid_231'),
  (232, '13800000102', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '小刚', 'https://example.com/avatars/3.png', false, 1, '2026-03-03', 'wx_openid_232'),
  (233, '13800000103', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '小美', 'https://example.com/avatars/4.png', true, 1, '2026-03-04', 'wx_openid_233'),
  (234, '13800000104', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '阿杰', 'https://example.com/avatars/5.png', true, 1, '2026-03-05', 'wx_openid_234'),
  (235, '13800000105', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '小猫控', 'https://example.com/avatars/6.png', true, 1, '2026-03-06', 'wx_openid_235'),
  (236, '13800000106', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '咖啡迷', 'https://example.com/avatars/7.png', true, 1, '2026-03-07', 'wx_openid_236'),
  (237, '13800000107', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '甜点党', 'https://example.com/avatars/8.png', true, 1, '2026-03-08', 'wx_openid_237'),
  (238, '13800000108', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '周末探店', 'https://example.com/avatars/9.png', false, 1, '2026-03-09', 'wx_openid_238'),
  (239, '13800000109', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '老王', 'https://example.com/avatars/10.png', true, 1, '2026-03-10', 'wx_openid_239'),
  (240, '13800000110', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '小李', 'https://example.com/avatars/11.png', true, 1, '2026-03-11', 'wx_openid_240'),
  (241, '13800000111', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '大刘', 'https://example.com/avatars/12.png', true, 1, '2026-03-12', 'wx_openid_241'),
  (242, '13800000112', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '安安', 'https://example.com/avatars/13.png', true, 1, '2026-03-13', 'wx_openid_242'),
  (243, '13800000113', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '志明', 'https://example.com/avatars/14.png', true, 1, '2026-03-14', 'wx_openid_243'),
  (244, '13800000114', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '春娇', 'https://example.com/avatars/15.png', true, 1, '2026-03-15', 'wx_openid_244'),
  (245, '13800000115', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '大卫', 'https://example.com/avatars/16.png', false, 1, '2026-03-16', 'wx_openid_245'),
  (246, '13800000116', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '小琳', 'https://example.com/avatars/17.png', true, 1, '2026-03-17', 'wx_openid_246'),
  (247, '13800000117', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '阿宝', 'https://example.com/avatars/18.png', true, 1, '2026-03-18', 'wx_openid_247'),
  (248, '13800000118', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '小鱼', 'https://example.com/avatars/19.png', true, 1, '2026-03-19', 'wx_openid_248'),
  (249, '13800000119', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '大飞', 'https://example.com/avatars/20.png', true, 1, '2026-03-20', 'wx_openid_249'),
  (250, '13800000120', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '慧慧', 'https://example.com/avatars/21.png', true, 1, '2026-03-21', 'wx_openid_250'),
  (251, '13800000121', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '阿杰', 'https://example.com/avatars/22.png', true, 1, '2026-03-22', 'wx_openid_251'),
  (252, '13800000122', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '小鹿', 'https://example.com/avatars/23.png', true, 1, '2026-03-23', 'wx_openid_252'),
  (253, '13800000123', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '丸子', 'https://example.com/avatars/24.png', true, 1, '2026-03-24', 'wx_openid_253'),
  (254, '13800000124', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '大毛', 'https://example.com/avatars/25.png', false, 1, '2026-03-25', 'wx_openid_254'),
  (255, '13800000125', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '小米', 'https://example.com/avatars/26.png', true, 1, '2026-03-26', 'wx_openid_255'),
  (256, '13800000126', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '阿花', 'https://example.com/avatars/27.png', true, 1, '2026-03-27', 'wx_openid_256'),
  (257, '13800000127', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '小新', 'https://example.com/avatars/28.png', true, 1, '2026-03-28', 'wx_openid_257'),
  (258, '13800000128', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '风风', 'https://example.com/avatars/29.png', true, 1, '2026-03-29', 'wx_openid_258'),
  (259, '13800000129', '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty', '小月', 'https://example.com/avatars/30.png', true, 1, '2026-03-30', 'wx_openid_259')
ON CONFLICT (user_id) DO NOTHING;

-- 2.2 用户角色（全部为顾客 role_id=1）
INSERT INTO public.user_roles (user_id, role_id, store_id)
SELECT user_id, 1, NULL FROM public.users WHERE user_id BETWEEN 230 AND 259
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 2.3 会员扩展信息（含不同等级和积分）
INSERT INTO public.member_ext (user_id, level, total_points, cumulative_amount, last_visit_time, preferences, created_at)
VALUES
  (230, 1, 120,  600.00,  '2026-06-10', '喜欢安静角落',            now()),
  (231, 2, 580,  2800.00, '2026-06-12', '偏爱猫咪互动时间',        now()),
  (232, 1, 30,   150.00,  NULL,         NULL,                      now()),
  (233, 3, 1500, 7500.00, '2026-06-15', '喜欢尝试新品甜点',       now()),
  (234, 1, 80,   400.00,  '2026-06-08', NULL,                      now()),
  (235, 2, 420,  2100.00, '2026-06-14', '偏爱撸猫，讨厌吵闹',     now()),
  (236, 1, 200,  1000.00, '2026-06-11', '常点美式咖啡',           now()),
  (237, 4, 5000, 25000.00, '2026-06-16', 'VIP客户，需要专属服务', now()),
  (238, 1, 50,   250.00,  NULL,         NULL,                      now()),
  (239, 2, 350,  1750.00, '2026-06-09', '周末常来',               now()),
  (240, 1, 100,  500.00,  '2026-06-13', NULL,                      now()),
  (241, 3, 2200, 11000.00, '2026-06-15', '带朋友来聚会',           now()),
  (242, 1, 60,   300.00,  '2026-06-07', '喜欢窗边位置',           now()),
  (243, 1, 150,  750.00,  '2026-06-12', NULL,                      now()),
  (244, 2, 480,  2400.00, '2026-06-14', '和男朋友常来',           now()),
  (245, 1, 20,   100.00,  NULL,         NULL,                      now()),
  (246, 1, 90,   450.00,  '2026-06-10', '喜欢拍照打卡',           now()),
  (247, 3, 1800, 9000.00, '2026-06-16', '商务洽谈常客',           now()),
  (248, 1, 70,   350.00,  '2026-06-06', NULL,                      now()),
  (249, 2, 400,  2000.00, '2026-06-13', '喜欢晚班时段',           now()),
  (250, 1, 110,  550.00,  '2026-06-11', '偏爱素食轻食',           now()),
  (251, 4, 8000, 40000.00, '2026-06-17', '顶级VIP，需要提前预约', now()),
  (252, 1, 40,   200.00,  NULL,         NULL,                      now()),
  (253, 2, 320,  1600.00, '2026-06-08', '喜欢带小孩来',           now()),
  (254, 1, 10,   50.00,   NULL,         NULL,                      now()),
  (255, 1, 130,  650.00,  '2026-06-12', '常点猫爪蛋糕',           now()),
  (256, 3, 1600, 8000.00, '2026-06-15', '猫咪主题生日会常客',     now()),
  (257, 1, 75,   375.00,  '2026-06-09', NULL,                      now()),
  (258, 2, 450,  2250.00, '2026-06-14', '喜欢尝试新品饮品',       now()),
  (259, 1, 95,   475.00,  '2026-06-10', '周末探店达人',           now())
ON CONFLICT (user_id) DO NOTHING;


-- ============================================================
-- 第 3 部分：猫咪档案（cat_id 1-10 基础 + 11-15 补充）
-- cleanup 全量删除了 cat_profiles，这里完整重建
-- ============================================================
INSERT INTO public.cat_profiles (cat_id, user_id, name, breed, personality, birth_date, weight_kg, avatar_url, is_default, created_at, store_id)
VALUES
  -- cat_id 1-10（与 reservations.cat_profile_id 和 cat_health_records.cat_id 对齐）
  (1,  NULL, '雪球',   '布偶猫',   '极其粘人', '2023-01-15', 4.5, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_1.png',  true,  now(), 1),
  (2,  NULL, '橘长',   '橘猫',     '贪吃好睡', '2021-05-20', 7.2, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_2.png',  true,  now(), 1),
  (3,  NULL, '煤老板', '英短黑猫', '高冷神秘', '2022-10-01', 5.0, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_3.png',  false, now(), 1),
  (4,  NULL, '咖啡',   '暹罗猫',   '话痨活泼', '2024-02-14', 3.8, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_4.png',  true,  now(), 2),
  (5,  NULL, '元宝',   '加菲猫',   '懒惰温顺', '2020-08-08', 6.5, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_5.png',  false, now(), 2),
  (6,  NULL, '布丁',   '金渐层',   '活泼好动', '2024-03-08', 3.5, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_6.png',  true,  now(), 2),
  (7,  NULL, '奶糖',   '银渐层',   '温柔粘人', '2023-09-15', 4.2, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_7.png',  true,  now(), 3),
  (8,  NULL, '包子',   '田园猫',   '聪明伶俐', '2022-06-20', 5.5, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_8.png',  false, now(), 1),
  (9,  NULL, '可乐',   '德文卷毛', '调皮捣蛋', '2024-05-10', 2.8, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_9.png',  true,  now(), 3),
  (10, NULL, '薯条',   '曼基康',   '憨厚可爱', '2021-12-25', 4.0, 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_10.png', false, now(), 1),
  -- cat_id 11-15（补充新猫）
  (11, NULL, '麻团',   '狸花猫',   '活泼好动',   '2023-05-10', 3.8,  'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_11.png', false, now(), 1),
  (12, NULL, '小白',   '英国短毛猫', '温顺粘人', '2022-09-20', 4.2,  'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_12.png', false, now(), 1),
  (13, NULL, '小黑',   '孟买猫',   '调皮好动',   '2024-01-15', 2.5,  'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_13.png', false, now(), 2),
  (14, NULL, '花花',   '三花猫',   '温柔安静',   '2021-11-30', 3.5,  'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_14.png', false, now(), 2),
  (15, NULL, '年糕',   '挪威森林猫', '霸气高冷', '2020-06-18', 5.5,  'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/cats/cat_15.png', false, now(), 3)
ON CONFLICT (cat_id) DO NOTHING;


-- ============================================================
-- 第 4 部分：预约记录（覆盖多种状态，时间分布合理）
-- ============================================================

-- 说明：
-- BOOKED: 已预约未到店
-- CONFIRMED: 已确认到店
-- MAKING: 制作中
-- SERVING: 上菜中
-- COMPLETED: 已完成
-- CANCEL_BOOKING: 取消预约
-- CANCEL_ORDER: 取消订单
-- REFUNDING: 退款中

INSERT INTO public.reservations (reservation_id, user_id, store_id, table_id, cat_profile_id, reservation_time, duration_min, party_size, special_request, order_amount, total_amount, status, points_used, points_earned, created_at, updated_at)
VALUES
  -- 已完成订单（COMPLETED）— 用于评价/积分/统计数据
  (1001, 230, 1, 1, 1,  '2026-06-01 12:00:00', 120, 2, '靠窗安静位置',  86.00, 76.00,  'COMPLETED'::public.reservation_status, 10, 76,  '2026-06-01 10:30:00', now()),
  (1002, 231, 1, 3, 2,  '2026-06-02 18:30:00', 120, 4, NULL,                198.00, 178.00, 'COMPLETED'::public.reservation_status, 20, 178, '2026-06-02 16:00:00', now()),
  (1003, 233, 1, 2, 3,  '2026-06-03 11:00:00', 120, 2, '不要辣',          66.00, 66.00,  'COMPLETED'::public.reservation_status, 0,  66,  '2026-06-03 09:00:00', now()),
  (1004, 235, 2, 9, 6,  '2026-06-03 19:00:00', 120, 2, '需要宝宝椅',      128.00, 108.00, 'COMPLETED'::public.reservation_status, 20, 108, '2026-06-03 17:00:00', now()),
  (1005, 237, 1, 5, 1,  '2026-06-04 12:30:00', 120, 6, '生日聚会布置',    358.00, 328.00, 'COMPLETED'::public.reservation_status, 30, 328, '2026-06-04 10:00:00', now()),
  (1006, 239, 2, 11, 7, '2026-06-05 18:00:00', 120, 4, NULL,                156.00, 156.00, 'COMPLETED'::public.reservation_status, 0,  156, '2026-06-05 15:30:00', now()),
  (1007, 241, 1, 4, 4,  '2026-06-06 11:30:00', 120, 4, '商务洽谈',        272.00, 272.00, 'COMPLETED'::public.reservation_status, 0,  272, '2026-06-06 10:00:00', now()),
  (1008, 244, 2, 10, 6, '2026-06-07 19:30:00', 120, 2, '纪念日布置',       188.00, 168.00, 'COMPLETED'::public.reservation_status, 20, 168, '2026-06-07 17:00:00', now()),
  (1009, 247, 1, 7, 2,  '2026-06-08 12:00:00', 120, 4, NULL,                226.00, 226.00, 'COMPLETED'::public.reservation_status, 0,  226, '2026-06-08 10:30:00', now()),
  (1010, 249, 2, 12, 7, '2026-06-09 18:30:00', 120, 4, '需要停车位',       196.00, 176.00, 'COMPLETED'::public.reservation_status, 20, 176, '2026-06-09 16:00:00', now()),
  (1011, 251, 3, 15, 8, '2026-06-10 11:00:00', 120, 2, 'VIP专属服务',     290.00, 260.00, 'COMPLETED'::public.reservation_status, 30, 260, '2026-06-10 09:00:00', now()),
  (1012, 253, 1, 6, 5,  '2026-06-11 19:00:00', 120, 2, NULL,                118.00, 118.00, 'COMPLETED'::public.reservation_status, 0,  118,  '2026-06-11 17:00:00', now()),

  -- 已确认到店（CONFIRMED）— 今天或明天的订单
  (2001, 230, 1, 1, 1,  '2026-06-22 18:00:00', 120, 2, NULL,            74.00,  74.00,  'CONFIRMED'::public.reservation_status, 0,  0,   '2026-06-21 10:00:00', now()),
  (2002, 236, 1, 3, NULL, '2026-06-22 19:00:00', 120, 4, '需要 wifi',    162.00, 162.00, 'CONFIRMED'::public.reservation_status, 0,  0,   '2026-06-21 11:30:00', now()),
  (2003, 242, 2, 9, 6,  '2026-06-23 12:00:00', 120, 2, '生日蛋糕',      138.00, 118.00, 'CONFIRMED'::public.reservation_status, 20, 0,   '2026-06-21 14:00:00', now()),
  (2004, 250, 1, 2, 3,  '2026-06-22 11:30:00', 120, 2, '素食需求',       66.00,  66.00,  'CONFIRMED'::public.reservation_status, 0,  0,   '2026-06-21 09:00:00', now()),

  -- 制作中（MAKING）
  (3001, 235, 1, 4, 4,  '2026-06-22 12:00:00', 120, 4, NULL,            198.00, 178.00, 'MAKING'::public.reservation_status,   20, 0,   '2026-06-22 11:50:00', now()),
  (3002, 248, 2, 10, 7, '2026-06-22 12:30:00', 120, 2, NULL,             86.00,  86.00,  'MAKING'::public.reservation_status,   0,  0,   '2026-06-22 12:00:00', now()),

  -- 上菜中（SERVING）
  (4001, 237, 1, 5, 1,  '2026-06-22 11:00:00', 120, 6, NULL,            358.00, 328.00, 'SERVING'::public.reservation_status,   30, 0,   '2026-06-22 10:50:00', now()),

  -- 已预约未到店（BOOKED）— 未来预约
  (5001, 255, 1, 8, NULL, '2026-06-25 18:00:00', 120, 2, '希望和雪球互动', 0.00, 0.00, 'BOOKED'::public.reservation_status,    0,  0,   '2026-06-20 15:00:00', now()),
  (5002, 256, 2, 13, 9,  '2026-06-26 19:00:00', 120, 6, NULL,             0.00, 0.00, 'BOOKED'::public.reservation_status,    0,  0,   '2026-06-20 16:00:00', now()),
  (5003, 258, 3, 16, 8,  '2026-06-27 12:00:00', 120, 4, '商务宴请',       0.00, 0.00, 'BOOKED'::public.reservation_status,    0,  0,   '2026-06-21 10:00:00', now()),

  -- 已取消（CANCEL_BOOKING）
  (6001, 232, 1, NULL, NULL, '2026-06-10 12:00:00', 120, 2, NULL,         0.00, 0.00, 'CANCEL_BOOKING'::public.reservation_status, 0, 0, '2026-06-09 10:00:00', now()),
  (6002, 245, 2, NULL, NULL, '2026-06-12 18:00:00', 120, 4, NULL,         0.00, 0.00, 'CANCEL_BOOKING'::public.reservation_status, 0, 0, '2026-06-11 15:00:00', now()),

  -- 退款中（REFUNDING）
  (7001, 238, 1, 1, 2,  '2026-06-15 12:00:00', 120, 2, NULL,            66.00,  66.00,  'REFUNDING'::public.reservation_status,  0,  0,   '2026-06-15 10:00:00', now())
ON CONFLICT (reservation_id) DO NOTHING;


-- ============================================================
-- 第 5 部分：订单明细（order_items）— 与 reservations 对齐
-- ============================================================
INSERT INTO public.order_items (item_id, reservation_id, dish_id, quantity, unit_price, subtotal)
VALUES
  -- 1001: 小明，2人，¥76
  (1, 1001, 1, 2, 38.00, 76.00),
  -- 1002: 小红，4人，¥178
  (2, 1002, 2, 2, 35.00, 70.00),
  (3, 1002, 7, 2, 38.00, 76.00),
  (4, 1002, 9, 1, 32.00, 32.00),
  -- 1003: 小美，2人，¥66
  (5, 1003, 1, 1, 38.00, 38.00),
  (6, 1003, 16, 1, 28.00, 28.00),
  -- 1004: 小猫控，2人，¥108
  (7, 1004, 6, 2, 36.00, 72.00),
  (8, 1004, 8, 1, 20.00, 20.00),
  (9, 1004, 1, 1, 16.00, 16.00),
  -- 1005: 甜点党，6人，¥328
  (10, 1005, 3, 3, 42.00, 126.00),
  (11, 1005, 1, 3, 38.00, 114.00),
  (12, 1005, 8, 3, 20.00, 60.00),
  (13, 1005, 9, 1, 28.00, 28.00),
  -- 1006: 老王，4人，¥156
  (14, 1006, 2, 2, 35.00, 70.00),
  (15, 1006, 7, 2, 38.00, 76.00),
  (16, 1006, 10, 1, 10.00, 10.00),
  -- 1007: 大刘，4人，¥276
  (17, 1007, 15, 2, 58.00, 116.00),
  (18, 1007, 6,  2, 36.00, 72.00),
  (19, 1007, 18, 2, 42.00, 84.00),
  -- 1008: 春娇，2人，¥168
  (20, 1008, 1, 2, 38.00, 76.00),
  (21, 1008, 3, 1, 42.00, 42.00),
  (22, 1008, 8, 2, 20.00, 40.00),
  (23, 1008, 10, 1, 10.00, 10.00),
  -- 1009: 阿宝，4人，¥224
  (24, 1009, 2, 2, 35.00, 70.00),
  (25, 1009, 6, 2, 36.00, 72.00),
  (26, 1009, 18, 2, 42.00, 84.00),
  -- 1010: 大飞，4人，¥176
  (27, 1010, 7, 2, 38.00, 76.00),
  (28, 1010, 1, 2, 38.00, 76.00),
  (29, 1010, 10, 1, 10.00, 10.00),
  (30, 1010, 8, 1, 14.00, 14.00),
  -- 1011: VIP阿杰，2人，¥258
  (31, 1011, 1, 2, 38.00, 76.00),
  (32, 1011, 3, 1, 42.00, 42.00),
  (33, 1011, 15, 1, 58.00, 58.00),
  (34, 1011, 18, 2, 42.00, 84.00),
  -- 1012: 小鹿，2人，¥98
  (35, 1012, 6, 2, 36.00, 72.00),
  (36, 1012, 10, 1, 10.00, 10.00),
  (37, 1012, 8, 1, 20.00, 20.00),
  (38, 1012, 8, 1, 16.00, 16.00),

  -- CONFIRMED 订单明细
  -- 2001: 小明，¥78
  (39, 2001, 1, 1, 38.00, 38.00),
  (40, 2001, 16, 1, 26.00, 26.00),
  (41, 2001, 10, 1, 10.00, 10.00),
  -- 2002: 咖啡迷，¥176
  (42, 2002, 15, 2, 58.00, 116.00),
  (43, 2002, 6, 1, 36.00, 36.00),
  (44, 2002, 10, 1, 10.00, 10.00),
  -- 2003: 阿杰，¥108
  (45, 2003, 1, 2, 38.00, 76.00),
  (46, 2003, 8, 1, 20.00, 20.00),
  (47, 2003, 10, 1, 10.00, 10.00),
  (48, 2003, 10, 1, 12.00, 12.00),
  -- 2004: 慧慧，¥66
  (49, 2004, 1, 1, 38.00, 38.00),
  (50, 2004, 16, 1, 28.00, 28.00),

  -- MAKING 订单明细
  -- 3001: 小猫控，¥178
  (51, 3001, 2, 2, 35.00, 70.00),
  (52, 3001, 7, 2, 38.00, 76.00),
  (53, 3001, 9, 1, 32.00, 32.00),
  -- 3002: 小鱼，¥86
  (54, 3002, 1, 1, 38.00, 38.00),
  (55, 3002, 16, 1, 26.00, 26.00),
  (56, 3002, 10, 1, 10.00, 10.00),
  (57, 3002, 10, 1, 12.00, 12.00),

  -- SERVING 订单明细
  -- 4001: VIP甜点党，¥328
  (58, 4001, 3, 3, 42.00, 126.00),
  (59, 4001, 1, 3, 38.00, 114.00),
  (60, 4001, 8, 3, 20.00, 60.00),
  (61, 4001, 9, 1, 28.00, 28.00),

  -- REFUNDING 订单明细
  -- 7001: 周末探店，¥66
  (62, 7001, 1, 1, 38.00, 38.00),
  (63, 7001, 16, 1, 28.00, 28.00)
ON CONFLICT (item_id) DO NOTHING;


-- ============================================================
-- 第 6 部分：支付记录（payments）— 与 reservations 对齐
-- ============================================================
INSERT INTO public.payments (payment_id, reservation_id, payment_method, amount, transaction_id, status, paid_at, created_at)
VALUES
  (1, 1001, 'WECHAT', 76.00, 'TXN100120260601', 'PAID',   '2026-06-01 11:55:00', '2026-06-01 11:50:00'),
  (2, 1002, 'WECHAT', 178.00, 'TXN100220260602', 'PAID',  '2026-06-02 15:55:00', '2026-06-02 15:50:00'),
  (3, 1003, 'ALIPAY', 66.00, 'TXN100320260603', 'PAID',   '2026-06-03 08:55:00', '2026-06-03 08:50:00'),
  (4, 1004, 'WECHAT', 108.00, 'TXN100420260603', 'PAID',  '2026-06-03 16:55:00', '2026-06-03 16:50:00'),
  (5, 1005, 'BANK_CARD', 328.00, 'TXN100520260604', 'PAID','2026-06-04 09:55:00', '2026-06-04 09:50:00'),
  (6, 1006, 'WECHAT', 156.00, 'TXN100620260605', 'PAID',  '2026-06-05 14:55:00', '2026-06-05 14:50:00'),
  (7, 1007, 'WECHAT', 272.00, 'TXN100720260606', 'PAID',  '2026-06-06 09:55:00', '2026-06-06 09:50:00'),
  (8, 1008, 'ALIPAY', 168.00, 'TXN100820260607', 'PAID',  '2026-06-07 17:55:00', '2026-06-07 17:50:00'),
  (9, 1009, 'WECHAT', 226.00, 'TXN100920260608', 'PAID',  '2026-06-08 10:55:00', '2026-06-08 10:50:00'),
  (10, 1010, 'WECHAT', 176.00, 'TXN101020260609', 'PAID', '2026-06-09 16:55:00', '2026-06-09 16:50:00'),
  (11, 1011, 'WECHAT', 260.00, 'TXN101120260610', 'PAID', '2026-06-10 08:55:00', '2026-06-10 08:50:00'),
  (12, 1012, 'ALIPAY', 118.00, 'TXN101220260611', 'PAID',  '2026-06-11 17:55:00', '2026-06-11 17:50:00'),
  -- CONFIRMED 订单（已支付）
  (13, 2001, 'WECHAT', 74.00, 'TXN200120260621', 'PAID',  '2026-06-21 10:05:00', '2026-06-21 10:00:00'),
  (14, 2002, 'ALIPAY', 162.00, 'TXN200220260621', 'PAID', '2026-06-21 11:35:00', '2026-06-21 11:30:00'),
  (15, 2003, 'WECHAT', 118.00, 'TXN200320260621', 'PAID', '2026-06-21 14:05:00', '2026-06-21 14:00:00'),
  (16, 2004, 'WECHAT', 66.00, 'TXN200420260621', 'PAID',  '2026-06-21 09:05:00', '2026-06-21 09:00:00'),
  -- MAKING 订单（已支付）
  (17, 3001, 'WECHAT', 178.00, 'TXN300120260622', 'PAID', '2026-06-22 11:52:00', '2026-06-22 11:50:00'),
  (18, 3002, 'ALIPAY', 86.00, 'TXN300220260622', 'PAID',  '2026-06-22 12:02:00', '2026-06-22 12:00:00'),
  -- SERVING 订单（已支付）
  (19, 4001, 'WECHAT', 328.00, 'TXN400120260622', 'PAID', '2026-06-22 10:52:00', '2026-06-22 10:50:00'),
  -- REFUNDING 订单（已支付，等待退款）
  (20, 7001, 'WECHAT', 66.00, 'TXN700120260615', 'PAID',  '2026-06-15 10:05:00', '2026-06-15 10:00:00')
ON CONFLICT (payment_id) DO NOTHING;


-- ============================================================
-- 第 7 部分：评价记录（reviews）— 已完成订单的评价
-- ============================================================
INSERT INTO public.reviews (review_id, reservation_id, user_id, store_id, overall_rating, food_rating, service_rating, environment_rating, cat_interaction_rating, content, images, reply, reply_at, status, created_at, updated_at, tags)
VALUES
  (1, 1001, 230, 1, 5, 5, 5, 5, 5, '非常棒的体验！雪球真的很粘人，菜品也很精致，会继续来！', '["https://example.com/reviews/r1_1.png"]', '感谢您的评价，期待再次光临！', now(), 'VISIBLE', '2026-06-01 14:30:00', now(), '["服务好", "猫咪可爱", "环境优雅"]'),
  (2, 1002, 231, 1, 4, 4, 5, 4, 4, '整体不错，就是人多的时候上菜有点慢，不过店员态度很好。', NULL, '非常抱歉上菜慢了，我们会改进！', now(), 'VISIBLE', '2026-06-02 20:30:00', now(), '["菜品精致", "服务好"]'),
  (3, 1003, 233, 1, 5, 5, 4, 5, NULL, '煤老板太酷了！高冷神秘，很喜欢这种氛围。', NULL, NULL, NULL, 'VISIBLE', '2026-06-03 13:00:00', now(), '["猫咪高冷", "环境优雅"]'),
  (4, 1004, 235, 2, 5, 4, 5, 5, 5, '布丁好可爱！！下次还来望京店。', '["https://example.com/reviews/r4_1.png","https://example.com/reviews/r4_2.png"]', '布丁也很喜欢您哦~', now(), 'VISIBLE', '2026-06-03 20:30:00', now(), '["猫咪可爱", "适合情侣"]'),
  (5, 1005, 237, 1, 5, 5, 5, 5, 5, '生日聚会办得很成功，猫咪们都很配合拍照，强烈推荐！', NULL, '谢谢您选择 NekoCafe 举办生日聚会！', now(), 'VISIBLE', '2026-06-04 15:00:00', now(), '["生日聚会", "适合团建", "拍照圣地"]'),
  (6, 1007, 241, 1, 4, 4, 4, 5, 3, '商务洽谈的好地方，环境安静，猫咪也不会太打扰。', NULL, NULL, NULL, 'VISIBLE', '2026-06-06 14:00:00', now(), '["环境优雅", "适合商务"]'),
  (7, 1008, 244, 2, 5, 5, 5, 4, 4, '纪念日布置很用心，感谢店家！', NULL, '祝您们纪念日快乐！', now(), 'VISIBLE', '2026-06-07 21:00:00', now(), '["服务好", "适合情侣"]'),
  (8, 1009, 247, 1, 5, 5, 4, 5, 4, '作为常客，这次体验依然很好，推荐招牌猫爪蛋糕！', NULL, '感谢您一直以来的支持！', now(), 'VISIBLE', '2026-06-08 14:30:00', now(), '["常客推荐", "菜品精致"]'),
  (9, 1011, 251, 3, 5, 5, 5, 5, 5, 'VIP体验非常棒，专属服务很到位，上海静安店的环境也很好。', NULL, '感谢VIP客户的支持，期待为您服务更多次！', now(), 'VISIBLE', '2026-06-10 13:30:00', now(), '["VIP服务", "环境优雅"]')
ON CONFLICT (reservation_id) DO NOTHING;


-- ============================================================
-- 第 8 部分：积分日志（points_log）— 与评价/支付对齐
-- ============================================================
INSERT INTO public.points_log (log_id, user_id, change_amount, balance_after, source, reservation_id, created_at)
VALUES
  (1, 230, 76,  76,  'CONSUME', 1001, '2026-06-01 12:00:00'),
  (2, 231, 178, 178, 'CONSUME', 1002, '2026-06-02 18:30:00'),
  (3, 233, 66,  66,  'CONSUME', 1003, '2026-06-03 11:00:00'),
  (4, 235, 108, 108, 'CONSUME', 1004, '2026-06-03 19:00:00'),
  (5, 237, 328, 328, 'CONSUME', 1005, '2026-06-04 12:30:00'),
  (6, 239, 156, 156, 'CONSUME', 1006, '2026-06-05 18:00:00'),
  (7, 241, 272, 272, 'CONSUME', 1007, '2026-06-06 11:30:00'),
  (8, 244, 168, 168, 'CONSUME', 1008, '2026-06-07 19:30:00'),
  (9, 247, 226, 226, 'CONSUME', 1009, '2026-06-08 12:00:00'),
  (10, 249, 176, 176, 'CONSUME', 1010, '2026-06-09 18:30:00'),
  (11, 251, 260, 260, 'CONSUME', 1011, '2026-06-10 11:00:00'),
  (12, 242, 118, 118, 'CONSUME', 1012, '2026-06-11 19:00:00')
ON CONFLICT (log_id) DO NOTHING;


-- ============================================================
-- 第 9 部分：退款记录（refund_records）
-- ============================================================
INSERT INTO public.refund_records (refund_id, payment_id, reservation_id, refund_amount, refund_reason, refund_transaction_id, status, operator_id, created_at, completed_at)
VALUES
  (1, 20, 7001, 66.00, '用户临时有事无法到店', NULL, 'REQUEST_REFUND'::public.refund_status_enum, NULL, '2026-06-15 10:30:00', NULL)
ON CONFLICT (refund_id) DO NOTHING;


-- ============================================================
-- 第 10 部分：优惠券（promotions 已在 after_cleanup.sql 中，这里插入 user_coupons）
-- ============================================================
INSERT INTO public.user_coupons (coupon_id, user_id, promo_id, status, used_at, used_reservation_id, expire_time, created_at)
VALUES
  (1, 230, 7,  'UNUSED', NULL, NULL, '2026-12-31', now()),  -- 新人专属礼包
  (2, 231, 1,  'UNUSED', NULL, NULL, '2026-06-30', now()),  -- 新店开业8折
  (3, 233, 3,  'USED',   '2026-06-03 10:00:00', 1003, '2026-08-31', now()),
  (4, 235, 4,  'UNUSED', NULL, NULL, '2026-12-31', now()),  -- 周末情侣双人特惠
  (5, 237, 10, 'UNUSED', NULL, NULL, '2026-06-22', now()),  -- 积分双倍活动
  (6, 239, 2,  'UNUSED', NULL, NULL, '2026-12-31', now()),  -- 满100减20
  (7, 241, 6,  'UNUSED', NULL, NULL, '2026-07-31', now()),  -- 会员日全场7折
  (8, 251, 11, 'UNUSED', NULL, NULL, '2026-07-15', now()),  -- 新店开业8折（上海）
  (9, 255, 12, 'UNUSED', NULL, NULL, '2026-06-23', now()),  -- 端午活动九折
  (10, 230, 12, 'UNUSED', NULL, NULL, '2026-06-23', now())   -- 端午活动九折
ON CONFLICT (coupon_id) DO NOTHING;


-- ============================================================
-- 第 10.1 部分：优惠券使用记录（coupon_usage）
-- 与 user_coupons 中 status='USED' 的记录对齐
-- ============================================================
INSERT INTO public.coupon_usage (usage_id, coupon_id, reservation_id, discount_amount, used_at)
VALUES
  (1, 3, 1003, 20.00, '2026-06-03 10:00:00')  -- user 233 在预约 1003 使用了优惠券
ON CONFLICT (usage_id) DO NOTHING;


-- ============================================================
-- 第 11 部分：通知（notifications）
-- ============================================================
INSERT INTO public.notifications (notification_id, store_id, user_id, target_role, type, title, content, related_type, related_id, is_read, created_at)
VALUES
  (100, 1, 230, NULL,     'order_confirm', '预约确认',   '您的预约 #2001 已确认，期待您的光临！', 'RESERVATION', 2001, false, now()),
  (101, 1, 236, NULL,     'order_confirm', '预约确认',   '您的预约 #2002 已确认',              'RESERVATION', 2002, false, now()),
  (102, NULL, 230, NULL,   'promotion',     '端午特惠',   '端午活动九折优惠券已发放，快来看看！', NULL, NULL, false, now()),
  (103, 1, NULL, 'STAFF', 'order_new',     '新订单提醒', '有新的预约订单 #3001 已确认到店，请准备接待', 'RESERVATION', 3001, false, now()),
  (104, 1, NULL, 'STAFF', 'order_serving', '上菜提醒',   '预约 #4001 的订单已开始上菜',          'RESERVATION', 4001, false, now()),
  (105, NULL, NULL, NULL,   'system',        '系统公告',   'NekoCafe 端午活动开始了！全场九折，快来参与！', NULL, NULL, false, now()),
  (106, 2, 242, NULL,     'order_confirm', '预约确认',   '您的预约 #2003 已确认',              'RESERVATION', 2003, true, now()),
  (107, 1, 235, NULL,     'refund_request', '退款申请', '您的预约 #7001 退款申请已提交，等待审核', 'REFUND', 1, false, now())
ON CONFLICT (notification_id) DO NOTHING;


-- ============================================================
-- 第 12 部分：排班记录（staff_schedules）
-- ============================================================
INSERT INTO public.staff_schedules (schedule_id, store_id, staff_id, work_date, shift_id, start_time, end_time, "position", notes, created_at, updated_at)
VALUES
  (1, 1, 224, '2026-06-22', 2, '2026-06-22 12:00:00', '2026-06-22 20:00:00', '店员', NULL, now(), now()),
  (2, 1, 225, '2026-06-22', 3, '2026-06-22 16:00:00', '2026-06-22 22:00:00', '店员', NULL, now(), now()),
  (3, 1, 226, '2026-06-22', 2, '2026-06-22 12:00:00', '2026-06-22 20:00:00', '店长', NULL, now(), now()),
  (4, 2, 224, '2026-06-22', 1, '2026-06-22 08:00:00', '2026-06-22 16:00:00', '店员', NULL, now(), now()),
  (5, 2, 225, '2026-06-22', 2, '2026-06-22 12:00:00', '2026-06-22 20:00:00', '店员', NULL, now(), now()),
  (6, 2, 226, '2026-06-22', 1, '2026-06-22 08:00:00', '2026-06-22 16:00:00', '店长', NULL, now(), now()),
  (7, 1, 229, '2026-06-22', 2, '2026-06-22 12:00:00', '2026-06-22 20:00:00', '猫咪管家', '负责雪球和橘长的看护', now(), now()),
  (8, 1, 224, '2026-06-23', 1, '2026-06-23 08:00:00', '2026-06-23 16:00:00', '店员', NULL, now(), now()),
  (9, 1, 225, '2026-06-23', 2, '2026-06-23 12:00:00', '2026-06-23 20:00:00', '店员', NULL, now(), now())
ON CONFLICT (schedule_id) DO NOTHING;


-- ============================================================
-- 第 13 部分：猫咪健康记录（cat_health_records）
-- ============================================================
INSERT INTO public.cat_health_records (record_id, cat_id, record_type, record_value, record_date, note, staff_id, created_at)
VALUES
  -- 体重记录
  (1, 1, 'WEIGHT',     '3.2kg',    '2026-06-01', '体重正常，毛色亮丽',   229, now()),
  (3, 2, 'WEIGHT',     '5.8kg',    '2026-06-02', '需控制饮食',           229, now()),
  (5, 4, 'WEIGHT',     '4.5kg',    '2026-06-05', '健康',               229, now()),
  (7, 6, 'WEIGHT',     '1.0kg',    '2026-06-08', '幼猫，正常发育',      229, now()),
  (8, 11,'WEIGHT',     '3.8kg',    '2026-06-10', '活泼好动，食欲正常',  229, now()),
  -- 第二条体重记录（历史数据，让趋势图能画出 ≥2 个数据点）
  (301, 1, 'WEIGHT',     '3.0kg',    '2026-03-10', '年初体检',              229, now()),
  (302, 2, 'WEIGHT',     '6.0kg',    '2026-03-12', '开始控制饮食前',         229, now()),
  (303, 3, 'WEIGHT',     '4.7kg',    '2026-02-20', '常规称重',              229, now()),
  (304, 4, 'WEIGHT',     '3.5kg',    '2026-03-05', '幼猫期',                229, now()),
  (305, 5, 'WEIGHT',     '6.8kg',    '2026-03-15', '偏重',                  229, now()),
  (306, 6, 'WEIGHT',     '3.0kg',    '2026-01-08', '幼猫期',                229, now()),
  (307, 7, 'WEIGHT',     '3.9kg',    '2026-02-15', '正常',                  229, now()),
  (308, 8, 'WEIGHT',     '5.2kg',    '2026-04-10', '常规称重',              229, now()),
  (309, 9, 'WEIGHT',     '2.5kg',    '2026-03-18', '德文卷毛体型小',        229, now()),
  (310, 10,'WEIGHT',     '3.7kg',    '2026-03-25', '正常',                  229, now()),
  -- 补充第三条 / 第二条体重记录，确保每只有体重的猫都有 ≥2 个数据点
  (311, 3,  'WEIGHT',     '4.5kg',    '2026-05-15', '近期称重',             229, now()),
  (312, 5,  'WEIGHT',     '6.5kg',    '2026-05-20', '减肥中',               229, now()),
  (313, 7,  'WEIGHT',     '4.0kg',    '2026-05-10', '正常',                 229, now()),
  (314, 8,  'WEIGHT',     '5.3kg',    '2026-05-18', '稳定',                 229, now()),
  (315, 9,  'WEIGHT',     '2.7kg',    '2026-05-22', '成长良好',             229, now()),
  (316, 10, 'WEIGHT',     '3.9kg',    '2026-05-25', '略增重',              229, now()),
  (317, 11, 'WEIGHT',     '3.5kg',    '2026-04-08', '之前记录',             229, now()),
  -- 疫苗记录
  (2, 1, 'VACCINE',    '猫三联',   '2026-06-01', 'nextDue=2027-06-01', 229, now()),
  (6, 5, 'VACCINE',    '狂犬疫苗', '2026-06-05', 'nextDue=2027-06-05', 229, now()),
  (9, 12,'VACCINE',    '猫三联',   '2026-06-12', 'nextDue=2027-06-12', 229, now()),
  -- 补充更多猫的疫苗记录
  (101, 2,  'VACCINE', '猫三联',   '2025-12-01', 'nextDue=2026-12-01', 229, now()),
  (102, 3,  'VACCINE', '狂犬疫苗', '2025-11-15', 'nextDue=2026-11-15', 229, now()),
  (103, 4,  'VACCINE', '猫三联',   '2026-01-20', 'nextDue=2027-01-20', 229, now()),
  (104, 7,  'VACCINE', '猫三联',   '2026-02-10', 'nextDue=2027-02-10', 229, now()),
  (105, 9,  'VACCINE', '狂犬疫苗', '2026-03-05', 'nextDue=2027-03-05', 229, now()),
  (106, 14, 'VACCINE', '猫三联',   '2026-04-01', 'nextDue=2027-04-01', 229, now()),
  -- 互动记录
  (4, 3,  'INTERACTION','玩耍',    '2026-06-03', 'mood=happy|互动积极', 229, now()),
  (201, 2, 'INTERACTION','梳毛',    '2026-06-10', 'mood=happy|喜欢被梳理毛发', 229, now()),
  (202, 2, 'INTERACTION','喂零食',  '2026-06-15', 'mood=happy|抢食积极',       229, now()),
  (203, 3,  'INTERACTION','玩耍',    '2026-06-08', 'mood=neutral|对玩具一般般', 229, now()),
  (204, 4,  'INTERACTION','抱抱',    '2026-06-12', 'mood=happy|粘人撒娇',       229, now()),
  (205, 7,  'INTERACTION','逗猫棒',  '2026-06-14', 'mood=happy|追得很开心',     229, now()),
  (206, 9,  'INTERACTION','撸毛',    '2026-06-16', 'mood=happy|呼噜声很大',     229, now())
ON CONFLICT (record_id) DO NOTHING;


-- ============================================================
-- 第 14 部分：门店日统计（store_daily_stats）
-- ============================================================
INSERT INTO public.store_daily_stats (stat_date, store_id, total_reservations, total_revenue, table_turnover_rate, revenue_per_seat, repeat_customers)
VALUES
  ('2026-06-01', 1, 3,  420.00, 0.38, 17.50, 1),
  ('2026-06-02', 1, 5,  680.00, 0.63, 21.25, 2),
  ('2026-06-03', 1, 4,  540.00, 0.50, 18.00, 1),
  ('2026-06-03', 2, 2,  260.00, 0.33, 21.67, 0),
  ('2026-06-04', 1, 6,  880.00, 0.75, 22.00, 3),
  ('2026-06-05', 2, 3,  380.00, 0.50, 19.00, 1),
  ('2026-06-06', 1, 4,  620.00, 0.50, 20.67, 2),
  ('2026-06-07', 2, 3,  410.00, 0.50, 20.50, 1),
  ('2026-06-08', 1, 5,  720.00, 0.63, 21.82, 2),
  ('2026-06-09', 2, 4,  560.00, 0.67, 21.54, 1),
  ('2026-06-10', 3, 2,  380.00, 0.40, 19.00, 0),
  ('2026-06-11', 1, 3,  320.00, 0.38, 16.84, 1)
ON CONFLICT (stat_date, store_id) DO NOTHING;


-- ============================================================
-- 第 15 部分：排队记录（queue）
-- ============================================================
INSERT INTO public.queue (queue_id, store_id, user_id, party_size, preferred_table_type, status, queue_number, called_at, seated_table_id, created_at)
VALUES
  (1, 1, 230, 2, '双人桌', 'SEATED',   'Q001', '2026-06-22 11:35:00', 1,   '2026-06-22 11:20:00'),
  (2, 1, 236, 4, '四人桌', 'WAITING',  'Q002', NULL,              NULL, '2026-06-22 11:40:00'),
  (3, 2, 242, 2, '双人桌', 'SEATED',   'Q003', '2026-06-22 12:10:00', 9,   '2026-06-22 11:55:00'),
  (4, 1, 255, 2, '双人桌', 'WAITING',  'Q004', NULL,              NULL, '2026-06-22 12:20:00'),
  (5, 3, 258, 4, '四人桌', 'CALLED',   'Q005', '2026-06-22 12:25:00', NULL, '2026-06-22 12:15:00')
ON CONFLICT (queue_id) DO NOTHING;


-- ============================================================
-- 重置序列（在所有数据插入后）
-- ============================================================
SELECT setval('public.users_user_id_seq',             COALESCE((SELECT MAX(user_id) FROM public.users), 1));
SELECT setval('public.reservations_reservation_id_seq', COALESCE((SELECT MAX(reservation_id) FROM public.reservations), 1));
SELECT setval('public.order_items_item_id_seq',        COALESCE((SELECT MAX(item_id) FROM public.order_items), 1));
SELECT setval('public.payments_payment_id_seq',       COALESCE((SELECT MAX(payment_id) FROM public.payments), 1));
SELECT setval('public.reviews_review_id_seq',          COALESCE((SELECT MAX(review_id) FROM public.reviews), 1));
SELECT setval('public.points_log_log_id_seq',          COALESCE((SELECT MAX(log_id) FROM public.points_log), 1));
SELECT setval('public.refund_records_refund_id_seq',   COALESCE((SELECT MAX(refund_id) FROM public.refund_records), 1));
SELECT setval('public.notifications_notification_id_seq', COALESCE((SELECT MAX(notification_id) FROM public.notifications), 1));
SELECT setval('public.staff_schedules_schedule_id_seq', COALESCE((SELECT MAX(schedule_id) FROM public.staff_schedules), 1));
SELECT setval('public.cat_health_records_record_id_seq', COALESCE((SELECT MAX(record_id) FROM public.cat_health_records), 1));
SELECT setval('public.queue_queue_id_seq',            COALESCE((SELECT MAX(queue_id) FROM public.queue), 1));
SELECT setval('public.user_coupons_coupon_id_seq',    COALESCE((SELECT MAX(coupon_id) FROM public.user_coupons), 1));
SELECT setval('public.cat_profiles_cat_id_seq',        COALESCE((SELECT MAX(cat_id) FROM public.cat_profiles), 1));
SELECT setval('public.tables_table_id_seq',            COALESCE((SELECT MAX(table_id) FROM public.tables), 1));
SELECT setval('public.dishes_dish_id_seq',             COALESCE((SELECT MAX(dish_id) FROM public.dishes), 1));
SELECT setval('public.staff_shifts_shift_id_seq',      COALESCE((SELECT MAX(shift_id) FROM public.staff_shifts), 1));

-- 恢复触发器
SET session_replication_role = DEFAULT;

-- ============================================================
-- 完成提示
-- ============================================================
SELECT '测试数据插入完成！' AS result;
SELECT 'users'            AS tbl, count(*) FROM users
UNION ALL
SELECT 'reservations', count(*) FROM reservations
UNION ALL
SELECT 'order_items',  count(*) FROM order_items
UNION ALL
SELECT 'payments',    count(*) FROM payments
UNION ALL
SELECT 'reviews',     count(*) FROM reviews
UNION ALL
SELECT 'points_log',  count(*) FROM points_log
UNION ALL
SELECT 'notifications', count(*) FROM notifications
UNION ALL
SELECT 'queue',       count(*) FROM queue
UNION ALL
SELECT 'store_daily_stats', count(*) FROM store_daily_stats;
