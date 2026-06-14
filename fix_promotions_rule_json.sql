-- ============================================================
-- 修复 promotions.rule_json 数据格式
-- 把不规范/缺失的 rule_json 更新为标准结构
-- 执行前建议先备份：SELECT * FROM public.promotions;
-- ============================================================

-- 1) 新店开业全场8折 (promo_id=1)
UPDATE public.promotions
SET rule_json = '{"discount": 0.8, "max_discount": 20, "min_spend": 0, "stackable": true}'::jsonb
WHERE promo_id = 1;

-- 2) 满100减20 (promo_id=2)
UPDATE public.promotions
SET rule_json = '{"reduction": 20, "min_spend": 100, "stackable": false}'::jsonb
WHERE promo_id = 2;

-- 3) VIP无门槛10元券 / 撸猫单人套餐券 (promo_id=5)
UPDATE public.promotions
SET rule_json = '{"reduction": 10, "min_spend": 0, "stackable": true}'::jsonb
WHERE promo_id = 5;

-- 4) 会员日全场7折 / 周末情侣双人特惠 (promo_id=6)
UPDATE public.promotions
SET rule_json = '{"discount": 0.7, "max_discount": 30, "min_spend": 0, "stackable": true}'::jsonb
WHERE promo_id = 6;

-- ============================================================
-- 验证修复结果
-- ============================================================
SELECT promo_id, name, type, rule_json
FROM public.promotions
WHERE promo_id IN (1, 2, 5, 6)
ORDER BY promo_id;
