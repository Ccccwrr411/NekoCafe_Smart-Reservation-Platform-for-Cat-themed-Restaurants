-- ============================================================
-- 优惠券测试数据 - 用户 user_id = 121
-- 使用现有 promotions 表数据，不插入新的 promo 规则
-- ============================================================

-- 给用户 121 发放优惠券（引用已有 promotions）
INSERT INTO public.user_coupons (user_id, promo_id, status, expire_time, created_at)
VALUES
  -- 未使用的券（4张）
  (121, 1, 'unused', '2026-12-31 23:59:59', NOW()),   -- promo_id=1 新客开业全场8折 (DISCOUNT)
  (121, 2, 'unused', '2026-12-31 23:59:59', NOW()),   -- promo_id=2 满100减20 (VOUCHER)
  (121, 6, 'unused', '2026-07-31 23:59:59', NOW()),   -- promo_id=6 会员日全场7折 (DISCOUNT)
  (121, 5, 'unused', '2026-06-15 23:59:59', NOW()),   -- promo_id=5 VIP无门槛10元券 (VOUCHER)

  -- 已使用的券（1张，测试状态显示）
  (121, 2, 'used', '2026-12-31 23:59:59', NOW()),   -- promo_id=2 满100减20 已使用

  -- 已过期的券（1张，测试状态显示）
  (121, 5, 'expired', '2025-01-01 00:00:00', NOW()); -- promo_id=5 无门槛10元券 已过期

-- ============================================================
-- 验证插入结果
-- ============================================================
SELECT uc.coupon_id, uc.user_id, uc.promo_id, p.name AS promo_name,
       p.type AS promo_type, uc.status,
       TO_CHAR(uc.expire_time, 'YYYY-MM-DD HH24:MI') AS expire_time
FROM public.user_coupons uc
LEFT JOIN public.promotions p ON uc.promo_id = p.promo_id
WHERE uc.user_id = 121
ORDER BY uc.coupon_id;
