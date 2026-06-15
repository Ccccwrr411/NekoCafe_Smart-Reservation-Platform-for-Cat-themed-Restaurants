-- V003: 为 status_enum 新增 CLEANING 值（桌位清洁中状态）
-- 用途：订单完成后桌位从 OCCUPIED → CLEANING（清洁中），清洁完毕再切回 IDLE
ALTER TYPE status_enum ADD VALUE IF NOT EXISTS 'CLEANING';
