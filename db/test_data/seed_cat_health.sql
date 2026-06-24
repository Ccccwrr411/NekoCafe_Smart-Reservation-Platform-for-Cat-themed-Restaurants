-- ================================================================
-- 补充猫咪健康记录测试数据
-- 执行方式: psql -h 82.157.130.254 -U admin_user -d postgres -f seed_cat_health.sql
-- ================================================================

-- 1. 更新疫苗记录的 note，加入 nextDue 信息
UPDATE cat_health_records SET note = '年度常规免疫 nextDue=2027-01-15' WHERE record_id = 1;
UPDATE cat_health_records SET note = '首针免疫 nextDue=2027-02-14' WHERE record_id = 5;
UPDATE cat_health_records SET note = '基础免疫 nextDue=2027-03-10' WHERE record_id = 6;
UPDATE cat_health_records SET note = '年度加强针 nextDue=2027-01-20' WHERE record_id = 8;

-- 2. 插入互动记录（INTERACTION 类型）
INSERT INTO cat_health_records (cat_id, record_type, record_value, record_date, note, staff_id, created_at) VALUES
(1, 'INTERACTION', '梳毛', '2026-05-20', 'mood=happy|非常喜欢被梳毛，呼噜声很大', 3, NOW()),
(1, 'INTERACTION', '玩耍', '2026-06-01', 'mood=happy|和客人玩逗猫棒非常开心', 3, NOW()),
(1, 'INTERACTION', '喂食', '2026-06-10', 'mood=neutral|正常进食，胃口好', 3, NOW()),
(2, 'INTERACTION', '喂食', '2026-05-15', 'mood=happy|吃了两碗猫粮，非常满足', 3, NOW()),
(2, 'INTERACTION', '玩耍', '2026-06-05', 'mood=grumpy|不想动，只想睡觉', 3, NOW()),
(3, 'INTERACTION', '玩耍', '2026-05-25', 'mood=neutral|被逗猫棒吸引了一下就走了', 3, NOW()),
(3, 'INTERACTION', '梳毛', '2026-06-08', 'mood=grumpy|不太愿意被梳毛，跑开了', 3, NOW()),
(4, 'INTERACTION', '玩耍', '2026-05-18', 'mood=happy|和客人互动频繁，叫声不停', 3, NOW()),
(4, 'INTERACTION', '喂食', '2026-06-02', 'mood=happy|吃得很开心，还想要更多', 3, NOW()),
(5, 'INTERACTION', '梳毛', '2026-06-03', 'mood=happy|很享受梳毛，一动不动', 3, NOW()),
(5, 'INTERACTION', '玩耍', '2026-06-09', 'mood=neutral|玩了一会儿就趴下了', 3, NOW());

-- 3. 补充体重历史记录，让趋势图有多个数据点
INSERT INTO cat_health_records (cat_id, record_type, record_value, record_date, note, staff_id, created_at) VALUES
(1, 'WEIGHT', '4.3kg', '2026-01-15', '体检称重', 3, NOW()),
(1, 'WEIGHT', '4.5kg', '2026-03-20', '体重稳定', 3, NOW()),
(2, 'WEIGHT', '6.8kg', '2026-01-10', '体检称重', 3, NOW()),
(2, 'WEIGHT', '7.0kg', '2026-04-15', '体重略增', 3, NOW()),
(3, 'WEIGHT', '4.8kg', '2026-01-05', '体检称重', 3, NOW()),
(3, 'WEIGHT', '5.0kg', '2026-04-01', '体重稳定', 3, NOW()),
(4, 'WEIGHT', '3.5kg', '2026-02-14', '初到称重', 3, NOW()),
(4, 'WEIGHT', '3.8kg', '2026-05-01', '体重增长中', 3, NOW()),
(5, 'WEIGHT', '6.3kg', '2026-02-01', '体检称重', 3, NOW()),
(5, 'WEIGHT', '6.5kg', '2026-05-10', '体重稳定', 3, NOW());
