-- V002: 为 reservation_status 枚举新增 MAKING 和 SERVING 值
-- 支持店员工作台完整的订单状态流转：BOOKED → CONFIRMED → MAKING → SERVING → COMPLETED

ALTER TYPE reservation_status ADD VALUE IF NOT EXISTS 'MAKING';
ALTER TYPE reservation_status ADD VALUE IF NOT EXISTS 'SERVING';
