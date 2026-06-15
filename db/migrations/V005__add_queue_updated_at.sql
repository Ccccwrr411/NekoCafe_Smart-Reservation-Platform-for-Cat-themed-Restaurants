-- V005: queue 表添加 updated_at 字段
-- 用于记录每次操作的更新时间（取号、叫号、确认、过号）

ALTER TABLE public.queue
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 为已有数据填充 updated_at（用 created_at 兜底）
UPDATE public.queue SET updated_at = created_at WHERE updated_at IS NULL;
