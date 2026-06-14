-- 1. 插入 users (5条)
INSERT INTO users (phone, password_hash, nickname, real_name, id_card, is_verified, status) VALUES
('13800000001', 'hash_1', '张三', '张伟', '110105199001011234', TRUE, 1),
('13800000002', 'hash_2', '李四', '李娜', '110105199202021234', TRUE, 1),
('13800000003', 'hash_3', '王五喵', '王强', '110105199503031234', TRUE, 1),
('13800000004', 'hash_4', '赵六', '赵敏', '110105199804041234', TRUE, 1),
('13800000005', 'hash_5', '孙七', '孙悦', '110105200005051234', FALSE, 1);

-- 2. 插入 roles (5条)
INSERT INTO roles (role_name) VALUES
('超级管理员'),
('店长'),
('店员'),
('兽医'),
('普通顾客');

-- 3. 插入 user_roles (5条，建立用户与角色的映射)
INSERT INTO user_roles (user_id, role_id, store_id) VALUES
(1, 1, NULL), -- 全局超管
(2, 2, 1),    -- 门店1店长
(3, 3, 1),    -- 门店1店员
(4, 4, 1),    -- 门店1兽医
(5, 5, NULL); -- 普通顾客

-- 4. 插入 member_ext (5条)
INSERT INTO member_ext (user_id, level, total_points, cumulative_amount, preferences) VALUES
(1, 5, 10000, 5000.00, '喜欢安静的环境'),
(2, 3, 2500, 1200.00, '靠窗座位'),
(3, 2, 800, 400.00, '喜欢布偶猫'),
(4, 1, 100, 50.00, '无特殊偏好'),
(5, 4, 6000, 3000.00, '需要无糖饮品');
-- 5. 插入 stores (5条)
INSERT INTO stores (name, city, address, longitude, latitude, contact_phone, business_hours) VALUES
('喵了个咪咖啡馆(中心店)', '北京', '朝阳区朝阳大悦城', 116.4551, 39.9242, '010-12345678', '10:00-22:00'),
('猫咖·午后阳光(海淀店)', '北京', '海淀区中关村', 116.3051, 39.9823, '010-87654321', '09:00-21:00'),
('喵星人补给站(三里屯店)', '北京', '朝阳区三里屯', 116.4552, 39.9343, '010-11223344', '11:00-23:00'),
('云端猫语(望京店)', '北京', '朝阳区望京SOHO', 116.4812, 39.9961, '010-44332211', '10:00-22:00'),
('暖爪时光(五道口店)', '北京', '海淀区五道口', 116.3312, 39.9922, '010-55667788', '10:00-23:00');

-- 6. 插入 tables (5条，均归属门店1和2，含实景平面图布局坐标)
INSERT INTO tables (store_id, table_no, capacity, table_type, cat_theme, top, "left", width, height) VALUES
(1, 'A01', 2, '沙发座', '布偶猫专属区', 30, 30, 200, 180),
(1, 'A02', 4, '靠窗座', '橘猫专属区', 30, 270, 220, 180),
(1, 'B01', 6, '包厢', '英短专属区', 240, 30, 280, 260),
(2, 'C01', 2, '吧台', '美短专属区', 30, 30, 200, 180),
(2, 'C02', 4, '榻榻米', '缅因猫专属区', 30, 270, 220, 180);

-- 7. 插入 table_status (5条，对应上述桌位)
INSERT INTO table_status (table_id, status, current_reservation_id) VALUES
(1, 'IDLE', NULL),
(2, 'OCCUPIED', 1),
(3, 'RESERVED', 2),
(4, 'CLEANING', NULL),
(5, 'IDLE', NULL);
-- 8. 插入 dishes (5条)
INSERT INTO dishes (name, category, price, description, tags) VALUES
('猫爪拿铁', '咖啡', 38.00, '招牌猫爪造型拉花拿铁', '招牌,热销'),
('焦糖玛奇朵', '咖啡', 35.00, '经典焦糖风味', '经典'),
('黑森林蛋糕', '甜点', 42.00, '绵密巧克力与樱桃的碰撞', '推荐'),
('三文鱼猫饭', '宠物零食', 25.00, '新鲜三文鱼制作，猫咪最爱', '猫咪特供'),
('冻干拼盘', '宠物零食', 30.00, '鸡肉、鸭肉冻干混合', '猫咪特供');

-- 9. 插入 store_dishes (5条)
INSERT INTO store_dishes (store_id, dish_id, price_override, is_available) VALUES
(1, 1, 35.00, TRUE), -- 门店1拿铁打折
(1, 2, NULL, TRUE),
(1, 3, NULL, TRUE),
(2, 4, 28.00, TRUE), -- 门店2猫饭涨价
(2, 5, NULL, FALSE); -- 门店2暂不提供冻干

-- 10. 插入 reservations (5条)
INSERT INTO reservations (user_id, store_id, table_id, cat_profile_id, reservation_time, duration_min, party_size, status, total_amount) VALUES
(1, 1, 1, 1, '2026-06-10 14:00:00', 120, 2, 'COMPLETED', 115.00),
(2, 1, 2, 2, '2026-06-10 15:30:00', 90, 4, 'CONFIRMED', 200.00),
(3, 2, 4, NULL, '2026-06-11 10:00:00', 120, 1, 'PENDING', 0.00),
(4, 2, 5, 4, '2026-06-11 18:00:00', 180, 2, 'CANCELLED', 0.00),
(5, 1, 3, 5, '2026-06-12 19:00:00', 120, 6, 'COMPLETED', 350.00);

-- 11. 插入 order_items (5条)
INSERT INTO order_items (reservation_id, dish_id, quantity, unit_price, subtotal) VALUES
(1, 1, 2, 38.00, 76.00),
(1, 4, 1, 25.00, 25.00),
(2, 2, 4, 35.00, 140.00),
(2, 3, 2, 42.00, 84.00),
(5, 5, 2, 30.00, 60.00);

-- 12. 插入 payments (5条)
INSERT INTO payments (reservation_id, payment_method, amount, transaction_id, status, paid_at) VALUES
(1, 'WECHAT', 115.00, 'WX20260610140001', 'PAID', '2026-06-10 14:05:00'),
(2, 'ALIPAY', 200.00, 'ALI20260610153001', 'PAID', '2026-06-10 15:32:00'),
(3, 'WECHAT', 50.00, 'WX20260611100001', 'UNPAID', NULL),
(4, 'CARD', 100.00, 'CARD20260611180001', 'REFUNDED', '2026-06-11 18:01:00'),
(5, 'WECHAT', 350.00, 'WX20260612190001', 'PAID', '2026-06-12 19:10:00');

-- 13. 插入 refund_records (5条)
INSERT INTO refund_records (payment_id, reservation_id, refund_amount, refund_reason, status, operator_id) VALUES
(4, 4, 100.00, '顾客临时有事取消', 'COMPLETED', 2),
(1, 1, 25.00, '猫饭未上齐', 'COMPLETED', 2),
(2, 2, 50.00, '优惠券漏核销退差价', 'PROCESSING', 2),
(5, 5, 30.00, '服务不满意', 'REJECTED', 1),
(5, 5, 10.00, '多收费用退回', 'PROCESSING', 1);
-- 14. 插入 cat_profiles (5条)
INSERT INTO cat_profiles (user_id, name, breed, personality, birth_date, weight_kg, is_default) VALUES
(NULL, '雪球', '布偶猫', '极其粘人', '2023-01-15', 4.5, TRUE),
(NULL, '橘长', '橘猫', '贪吃好睡', '2021-05-20', 7.2, TRUE),
(1, '煤老板', '英短黑猫', '高冷神秘', '2022-10-01', 5.0, FALSE),
(NULL, '咖啡', '暹罗猫', '话痨活泼', '2024-02-14', 3.8, TRUE),
(2, '元宝', '加菲猫', '懒惰温顺', '2020-08-08', 6.5, FALSE);

-- 15. 插入 cat_health_records (5条)
INSERT INTO cat_health_records (cat_id, record_type, record_value, record_date, note, staff_id) VALUES
(1, 'VACCINE', '狂犬疫苗+妙三多', '2026-01-15', '年度常规免疫', 4),
(1, 'WEIGHT', '4.6kg', '2026-05-01', '体重稳定', 3),
(2, 'CHECKUP', '肠胃炎', '2026-03-10', '需控制饮食，停猫条', 4),
(3, 'WEIGHT', '5.2kg', '2026-06-01', '稍微超重', 3),
(4, 'VACCINE', '狂犬疫苗', '2026-02-14', '首针免疫', 4);
-- 16. 插入 promotions (5条)
INSERT INTO promotions (name, type, rule_json, start_time, end_time, applicable_stores) VALUES
('新店开业全场8折', 'DISCOUNT', '{"discount_rate": 0.8, "min_spend": 0}', '2026-06-01', '2026-06-30', '[1,2,3,4,5]'),
('满100减20', 'VOUCHER', '{"reduce_amount": 20, "min_spend": 100}', '2026-05-01', '2026-12-31', '[1,2]'),
('撸猫单人套餐券', 'VOUCHER', '{"item_id": 1, "special_price": 29.9}', '2026-06-01', '2026-08-31', '[1]'),
('周末情侣双人特惠', 'DISCOUNT', '{"discount_rate": 0.88, "days": [6, 0]}', '2026-01-01', '2026-12-31', '[1,2,3]'),
('VIP无门槛10元券', 'VOUCHER', '{"reduce_amount": 10, "min_spend": 0}', '2026-06-01', '2026-06-15', '[1,2,3,4,5]');

-- 17. 插入 user_coupons (5条)
INSERT INTO user_coupons (user_id, promo_id, status, used_at, used_reservation_id, expire_time) VALUES
(1, 2, 'USED', '2026-06-10 14:00:00', 1, '2026-12-31 23:59:59'),
(2, 1, 'USED', '2026-06-10 15:30:00', 2, '2026-06-30 23:59:59'),
(3, 5, 'UNUSED', NULL, NULL, '2026-06-15 23:59:59'),
(4, 3, 'EXPIRED', NULL, NULL, '2026-05-31 23:59:59'),
(5, 2, 'UNUSED', NULL, NULL, '2026-12-31 23:59:59');

-- 18. 插入 coupon_usage (5条)
INSERT INTO coupon_usage (coupon_id, reservation_id, discount_amount) VALUES
(1, 1, 20.00),
(2, 2, 40.00),
(3, 3, 10.00),
(4, 5, 20.00),
(5, 5, 10.00);

-- 19. 插入 queue (5条)
INSERT INTO queue (store_id, user_id, party_size, preferred_table_type, status, queue_number, called_at, seated_table_id) VALUES
(1, 1, 2, '沙发座', 'SEATED', 'A001', '2026-06-10 13:55:00', 1),
(1, 2, 4, '包厢', 'SEATED', 'B001', '2026-06-10 15:20:00', 3),
(1, 3, 1, '任意', 'CALLED', 'A002', '2026-06-10 16:00:00', NULL),
(1, 4, 2, '靠窗座', 'WAITING', 'A003', NULL, NULL),
(2, 5, 3, '任意', 'CANCELLED', 'C001', NULL, NULL);
-- 20. 插入 store_daily_stats (5条)
INSERT INTO store_daily_stats (stat_date, store_id, total_reservations, total_revenue, table_turnover_rate, revenue_per_seat, repeat_customers) VALUES
('2026-06-01', 1, 45, 8500.00, 3.5, 85.00, 12),
('2026-06-02', 1, 50, 9200.00, 4.0, 92.00, 15),
('2026-06-03', 1, 38, 7600.00, 2.8, 76.00, 8),
('2026-06-01', 2, 30, 5400.00, 2.5, 60.00, 5),
('2026-06-02', 2, 35, 6100.00, 3.0, 65.00, 7);

-- 21. 插入 points_log (5条)
INSERT INTO points_log (user_id, change_amount, balance_after, source, reservation_id) VALUES
(1, 115, 10115, '消费赠送', 1),
(1, -500, 9615, '兑换猫粮', NULL),
(2, 200, 2700, '消费赠送', 2),
(5, 350, 6350, '消费赠送', 5),
(5, 100, 6450, '签到活动', NULL);

-- 22. 插入 staff_shifts (5条)
INSERT INTO staff_shifts (shift_name, start_time, end_time) VALUES
('早班', '08:00:00', '16:00:00'),
('中班', '12:00:00', '20:00:00'),
('晚班', '16:00:00', '24:00:00'),
('通班', '10:00:00', '22:00:00'),
('兽医巡诊班', '14:00:00', '18:00:00');

-- 23. 插入 staff_schedules (5条)
INSERT INTO staff_schedules (store_id, staff_id, work_date, shift_id, start_time, end_time, position) VALUES
(1, 2, '2026-06-10', 4, '2026-06-10 09:55:00', '2026-06-10 22:05:00', '店长'),
(1, 3, '2026-06-10', 1, '2026-06-10 07:50:00', '2026-06-10 16:00:00', '店员'),
(1, 4, '2026-06-10', 5, '2026-06-10 13:50:00', '2026-06-10 18:10:00', '驻店兽医'),
(1, 3, '2026-06-11', 2, NULL, NULL, '店员'),
(1, 2, '2026-06-11', 4, NULL, NULL, '店长');

-- 24. 插入 shift_exceptions (5条)
INSERT INTO shift_exceptions (store_id, staff_id, exception_date, type, original_schedule_id, new_schedule_id, status, approver_id, reason) VALUES
(1, 3, '2026-06-11', 'LEAVE', 4, NULL, 'APPROVED', 2, '病假'),
(1, 2, '2026-06-15', 'SWAP', NULL, NULL, 'PENDING', 1, '与同事换班'),
(2, 5, '2026-06-16', 'LEAVE', NULL, NULL, 'REJECTED', 2, '事假'),
(1, 3, '2026-06-20', 'LEAVE', NULL, NULL, 'PENDING', 2, '年假申请'),
(2, 4, '2026-06-21', 'SWAP', NULL, NULL, 'APPROVED', 1, '调休');

-- 25. 插入 operation_logs (5条)
INSERT INTO operation_logs (user_id, username, role_at_time, operation_type, target_type, target_id, request_ip, result) VALUES
(1, '张三', '超级管理员', 'UPDATE', 'STORE_INFO', '1', '192.168.1.10', 'SUCCESS'),
(2, '李四', '店长', 'CREATE', 'RESERVATION', '6', '192.168.1.11', 'SUCCESS'),
(2, '李四', '店长', 'REFUND', 'PAYMENT', '4', '192.168.1.11', 'SUCCESS'),
(4, '赵六', '兽医', 'ADD', 'HEALTH_RECORD', '2', '192.168.1.15', 'SUCCESS'),
(3, '王五喵', '店员', 'DELETE', 'DISH', '5', '192.168.1.12', 'FAILED');

-- 26. 插入 reviews (5条)
INSERT INTO reviews (reservation_id, user_id, store_id, overall_rating, food_rating, service_rating, environment_rating, cat_interaction_rating, content) VALUES
(1, 1, 1, 5, 5, 5, 5, 5, '雪球太可爱了！咖啡也很好喝，下次还会来！'),
(2, 2, 1, 4, 4, 4, 5, 4, '环境不错，橘长一直在睡觉，没怎么互动。'),
(5, 5, 1, 5, 5, 5, 5, 5, '朋友聚会定的包厢，体验极佳！'),
(3, 3, 2, 2, 3, 2, 3, 2, '人太多了，排队等了很久，猫咪看起来很累。'),
(4, 4, 2, 1, NULL, NULL, NULL, NULL, '临时取消了，没去成。');
