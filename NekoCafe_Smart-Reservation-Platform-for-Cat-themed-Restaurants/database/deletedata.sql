-- 删除所有表中的数据 (使用 TRUNCATE，比 DELETE 更快，且会自动重置自增计数器)
TRUNCATE TABLE users, roles, user_roles, member_ext,
               stores, tables, table_status,
               reservations, dishes, store_dishes, order_items, payments, refund_records,
               cat_profiles, cat_health_records,
               promotions, user_coupons, coupon_usage, queue,
               store_daily_stats, points_log, staff_shifts, staff_schedules, shift_exceptions, operation_logs, reviews
RESTART IDENTITY CASCADE;