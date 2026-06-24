-- ==========================================
-- 为每个表新增5条数据（除users表外）
-- ==========================================

-- 1. roles (新增5条)
INSERT INTO roles (role_name) VALUES
('区域经理'),
('清洁员'),
('收银员'),
('饮品师'),
('活动策划');

-- 2. user_roles (新增5条)
INSERT INTO user_roles (user_id, role_id, store_id) VALUES
(2, 6, 2),    -- 用户2同时担任区域经理
(3, 8, 1),    -- 用户3同时担任饮品师
(4, 7, 1),    -- 用户4同时担任收银员
(5, 9, 1),    -- 用户5担任活动策划
(1, 6, NULL); -- 用户1兼任区域经理

-- 3. member_ext (新增5条)
INSERT INTO member_ext (user_id, level, total_points, cumulative_amount, preferences) VALUES
(1, 5, 12000, 6000.00, '偏爱撸猫时间'),
(2, 4, 5000, 2500.00, '需要安静角落'),
(3, 3, 1500, 750.00, '喜欢和橘猫互动'),
(4, 2, 500, 250.00, '爱喝咖啡'),
(5, 5, 8000, 4000.00, '常带朋友来');

-- 4. stores (新增5条)
INSERT INTO stores (name, city, address, longitude, latitude, contact_phone, business_hours) VALUES
('喵星人俱乐部(国贸店)', '北京', '朝阳区国贸中心', 116.4651, 39.9042, '010-66778899', '10:00-22:00'),
('猫语咖啡(通州店)', '北京', '通州区万达广场', 116.6551, 39.9442, '010-99887766', '09:30-21:30'),
('猫爪联盟(大兴店)', '北京', '大兴区龙湖天街', 116.3551, 39.7242, '010-55443322', '10:00-22:30'),
('橘猫之家(丰台店)', '北京', '丰台区银泰百货', 116.2851, 39.8542, '010-22334455', '10:00-21:00'),
('猫奴天堂(石景山店)', '北京', '石景山区万达广场', 116.1751, 39.9142, '010-33445566', '10:00-22:00');

-- 5. tables (新增5条)
INSERT INTO tables (store_id, table_no, capacity, table_type, cat_theme) VALUES
(3, 'D01', 2, '卡座', '暹罗猫专属区'),
(3, 'D02', 4, '开放式', '波斯猫专属区'),
(4, 'E01', 2, '单人座', '加菲猫专属区'),
(4, 'E02', 6, '大包厢', '全品种互动区'),
(5, 'F01', 3, '环形座', '田园猫专属区');

-- 6. table_status (新增5条)
INSERT INTO table_status (table_id, status, current_reservation_id) VALUES
(6, 'IDLE', NULL),
(7, 'RESERVED', 6),
(8, 'OCCUPIED', 7),
(9, 'CLEANING', NULL),
(10, 'IDLE', NULL);

-- 7. dishes (新增5条)
INSERT INTO dishes (name, category, price, description, tags) VALUES
('抹茶拿铁', '咖啡', 36.00, '清新抹茶与牛奶的完美结合', '推荐'),
('芝士蛋糕', '甜点', 38.00, '浓郁芝士风味', '热销'),
('猫薄荷饼干', '宠物零食', 20.00, '猫咪最爱的猫薄荷口味', '猫咪特供'),
('水果拼盘', '甜点', 45.00, '新鲜时令水果', '健康'),
('招牌猫爪蛋糕', '甜点', 32.00, '可爱猫爪造型蛋糕', '招牌');

-- 8. store_dishes (新增5条)
INSERT INTO store_dishes (store_id, dish_id, price_override, is_available) VALUES
(3, 6, 34.00, TRUE),
(3, 7, NULL, TRUE),
(4, 8, 18.00, TRUE),
(5, 9, NULL, TRUE),
(5, 10, 30.00, FALSE);

-- 9. reservations (新增5条)
INSERT INTO reservations (user_id, store_id, table_id, cat_profile_id, reservation_time, duration_min, party_size, status, total_amount) VALUES
(1, 3, 6, 1, '2026-06-13 11:00:00', 120, 2, 'PENDING', 0.00),
(2, 3, 7, 2, '2026-06-13 14:00:00', 180, 4, 'CONFIRMED', 280.00),
(3, 4, 8, 3, '2026-06-14 10:30:00', 90, 2, 'COMPLETED', 85.00),
(4, 4, 9, 4, '2026-06-14 16:00:00', 120, 6, 'CANCELLED', 0.00),
(5, 5, 10, 5, '2026-06-15 19:30:00', 150, 3, 'PENDING', 0.00);

-- 10. order_items (新增5条)
INSERT INTO order_items (reservation_id, dish_id, quantity, unit_price, subtotal) VALUES
(6, 6, 2, 36.00, 72.00),
(7, 7, 1, 38.00, 38.00),
(8, 8, 2, 20.00, 40.00),
(9, 9, 3, 45.00, 135.00),
(10, 10, 2, 32.00, 64.00);

-- 11. payments (新增5条)
INSERT INTO payments (reservation_id, payment_method, amount, transaction_id, status, paid_at) VALUES
(6, 'WECHAT', 72.00, 'WX20260613110001', 'UNPAID', NULL),
(7, 'ALIPAY', 280.00, 'ALI20260613140001', 'PAID', '2026-06-13 13:55:00'),
(8, 'WECHAT', 85.00, 'WX20260614103001', 'PAID', '2026-06-14 10:32:00'),
(9, 'CARD', 135.00, 'CARD20260614160001', 'REFUNDED', '2026-06-14 15:55:00'),
(10, 'WECHAT', 64.00, 'WX20260615193001', 'UNPAID', NULL);

-- 12. refund_records (新增5条)
INSERT INTO refund_records (payment_id, reservation_id, refund_amount, refund_reason, status, operator_id) VALUES
(9, 9, 135.00, '顾客行程变更', 'COMPLETED', 2),
(7, 7, 50.00, '服务超时补偿', 'PROCESSING', 1),
(8, 8, 20.00, '猫薄荷饼干缺货', 'COMPLETED', 3),
(6, 6, 36.00, '顾客取消预约', 'COMPLETED', 2),
(10, 10, 64.00, '顾客未到自动取消', 'PROCESSING', 1);

-- 13. cat_profiles (新增5条)
INSERT INTO cat_profiles (user_id, name, breed, personality, birth_date, weight_kg, is_default) VALUES
(NULL, '布丁', '金渐层', '活泼好动', '2024-03-08', 3.5, TRUE),
(NULL, '奶糖', '银渐层', '温柔粘人', '2023-09-15', 4.2, TRUE),
(3, '包子', '田园猫', '聪明伶俐', '2022-06-20', 5.5, FALSE),
(NULL, '可乐', '德文卷毛', '调皮捣蛋', '2024-05-10', 2.8, TRUE),
(4, '薯条', '曼基康', '憨厚可爱', '2021-12-25', 4.0, FALSE);

-- 14. cat_health_records (新增5条)
INSERT INTO cat_health_records (cat_id, record_type, record_value, record_date, note, staff_id) VALUES
(5, 'CHECKUP', '健康检查', '2026-04-01', '年度体检正常', 4),
(6, 'VACCINE', '妙三多', '2026-03-10', '基础免疫', 4),
(7, 'WEIGHT', '3.6kg', '2026-05-15', '体重略增', 3),
(8, 'VACCINE', '狂犬疫苗', '2026-01-20', '年度加强针', 4),
(9, 'CHECKUP', '皮肤病治疗', '2026-05-20', '已痊愈', 4);

-- 15. promotions (新增5条)
INSERT INTO promotions (name, type, rule_json, start_time, end_time, applicable_stores) VALUES
('会员日全场7折', 'DISCOUNT', '{"discount_rate": 0.7, "min_spend": 0}', '2026-07-01', '2026-07-31', '[1,2,3,4,5,6,7,8,9,10]'),
('新人专属礼包', 'VOUCHER', '{"reduce_amount": 30, "min_spend": 50}', '2026-06-15', '2026-12-31', '[1,2,3,4,5,6,7,8,9,10]'),
('猫咪生日月特惠', 'DISCOUNT', '{"discount_rate": 0.85, "days": "cat_birthday_month"}', '2026-06-01', '2026-12-31', '[1,2,3]'),
('工作日下午茶', 'VOUCHER', '{"item_id": 1, "special_price": 25}', '2026-06-01', '2026-12-31', '[1,2,3,4,5]'),
('积分双倍活动', 'DISCOUNT', '{"points_multiplier": 2}', '2026-06-20', '2026-06-22', '[1,2,3,4,5,6,7,8,9,10]');

-- 16. user_coupons (新增5条)
INSERT INTO user_coupons (user_id, promo_id, status, used_at, used_reservation_id, expire_time) VALUES
(1, 6, 'UNUSED', NULL, NULL, '2026-07-31 23:59:59'),
(2, 7, 'UNUSED', NULL, NULL, '2026-12-31 23:59:59'),
(3, 8, 'USED', '2026-06-14 10:30:00', 8, '2026-12-31 23:59:59'),
(4, 9, 'UNUSED', NULL, NULL, '2026-12-31 23:59:59'),
(5, 10, 'UNUSED', NULL, NULL, '2026-06-22 23:59:59');

-- 17. coupon_usage (新增5条)
INSERT INTO coupon_usage (coupon_id, reservation_id, discount_amount) VALUES
(6, 6, 30.00),
(7, 7, 28.00),
(8, 8, 17.00),
(9, 9, 20.00),
(10, 10, 12.80);

-- 18. queue (新增5条)
INSERT INTO queue (store_id, user_id, party_size, preferred_table_type, status, queue_number, called_at, seated_table_id) VALUES
(3, 1, 2, '卡座', 'WAITING', 'D001', NULL, NULL),
(3, 2, 4, '包厢', 'CALLED', 'D002', '2026-06-13 13:50:00', NULL),
(4, 3, 1, '单人座', 'SEATED', 'E001', '2026-06-13 14:00:00', 8),
(4, 4, 6, '大包厢', 'WAITING', 'E002', NULL, NULL),
(5, 5, 3, '环形座', 'CANCELLED', 'F001', NULL, NULL);

-- 19. store_daily_stats (新增5条)
INSERT INTO store_daily_stats (stat_date, store_id, total_reservations, total_revenue, table_turnover_rate, revenue_per_seat, repeat_customers) VALUES
('2026-06-03', 2, 42, 7800.00, 3.2, 70.00, 10),
('2026-06-04', 1, 55, 10200.00, 4.5, 102.00, 18),
('2026-06-04', 2, 38, 7000.00, 2.8, 65.00, 8),
('2026-06-05', 1, 48, 9000.00, 3.8, 90.00, 14),
('2026-06-05', 2, 45, 8200.00, 3.5, 75.00, 12);

-- 20. points_log (新增5条)
INSERT INTO points_log (user_id, change_amount, balance_after, source, reservation_id) VALUES
(2, 280, 2980, '消费赠送', 7),
(3, 85, 885, '消费赠送', 8),
(4, -100, 0, '积分清零', NULL),
(5, 64, 6514, '消费赠送', 10),
(1, 200, 9815, '邀请好友', NULL);

-- 21. staff_shifts (新增5条)
INSERT INTO staff_shifts (shift_name, start_time, end_time) VALUES
('早班A', '07:00:00', '15:00:00'),
('晚班A', '15:00:00', '23:00:00'),
('周末特班', '09:00:00', '21:00:00'),
('临时替班', '10:00:00', '18:00:00'),
('夜班', '22:00:00', '06:00:00');

-- 22. staff_schedules (新增5条)
INSERT INTO staff_schedules (store_id, staff_id, work_date, shift_id, start_time, end_time, position) VALUES
(1, 3, '2026-06-12', 6, '2026-06-12 14:55:00', '2026-06-12 22:00:00', '店员'),
(1, 4, '2026-06-12', 5, '2026-06-12 13:55:00', '2026-06-12 18:05:00', '兽医'),
(2, 2, '2026-06-12', 4, '2026-06-12 09:55:00', '2026-06-12 22:05:00', '店长'),
(2, 5, '2026-06-13', 1, '2026-06-13 07:50:00', '2026-06-13 16:00:00', '店员'),
(1, 2, '2026-06-13', 4, NULL, NULL, '店长');

-- 23. shift_exceptions (新增5条)
INSERT INTO shift_exceptions (store_id, staff_id, exception_date, type, original_schedule_id, new_schedule_id, status, approver_id, reason) VALUES
(2, 5, '2026-06-14', 'LEAVE', NULL, NULL, 'APPROVED', 2, '家庭聚餐'),
(2, 2, '2026-06-17', 'SWAP', NULL, NULL, 'PENDING', 1, '个人事务'),
(1, 4, '2026-06-18', 'LEAVE', NULL, NULL, 'APPROVED', 2, '兽医培训'),
(1, 3, '2026-06-19', 'SWAP', NULL, NULL, 'APPROVED', 2, '调休'),
(2, 4, '2026-06-20', 'LEAVE', NULL, NULL, 'PENDING', 2, '年假');

-- 24. operation_logs (新增5条)
INSERT INTO operation_logs (user_id, username, role_at_time, operation_type, target_type, target_id, request_ip, result) VALUES
(2, '李四', '店长', 'CREATE', 'STAFF_SCHEDULE', '8', '192.168.1.11', 'SUCCESS'),
(3, '王五喵', '店员', 'UPDATE', 'TABLE_STATUS', '2', '192.168.1.12', 'SUCCESS'),
(1, '张三', '超级管理员', 'DELETE', 'PROMOTION', '3', '192.168.1.10', 'SUCCESS'),
(4, '赵六', '兽医', 'ADD', 'CAT_PROFILE', '10', '192.168.1.15', 'SUCCESS'),
(2, '李四', '店长', 'UPDATE', 'RESERVATION', '7', '192.168.1.11', 'FAILED');

-- 25. reviews (新增5条)
INSERT INTO reviews (reservation_id, user_id, store_id, overall_rating, food_rating, service_rating, environment_rating, cat_interaction_rating, content) VALUES
(6, 1, 3, 5, 5, 5, 5, 5, '布丁太可爱了！下次还会来撸它！'),
(7, 2, 3, 4, 4, 5, 4, 5, '奶糖很亲人，一直在我腿上睡觉。'),
(8, 3, 4, 3, 3, 4, 4, 3, '环境一般，但猫咪都很健康活泼。'),
(9, 4, 4, 2, 2, 3, 3, 2, '等待时间太长，体验不佳。'),
(10, 5, 5, 5, 5, 5, 5, 5, '可乐特别调皮，互动感满分！');
