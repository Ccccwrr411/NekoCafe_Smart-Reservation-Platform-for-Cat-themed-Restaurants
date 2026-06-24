-- V006: 评价表新增 tags 字段 + reservation_id 唯一约束
-- 1. 新增 tags JSONB 列，存储用户选择的评价标签
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS tags JSONB DEFAULT '[]';

-- 2. 对 reservation_id 加唯一约束，确保一个订单只能评价一次（DB层保障）
CREATE UNIQUE INDEX IF NOT EXISTS uk_reviews_reservation_id ON reviews (reservation_id);
