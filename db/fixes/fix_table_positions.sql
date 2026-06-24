-- ============================================================
-- 修复所有门店桌位坐标 v2 — 全量动态网格布局
-- 用法: set PGCLIENTENCODING=UTF8 && psql -h 82.157.130.254 -U admin_user -d postgres -f "fix_table_positions.sql"
-- ============================================================

SET client_encoding TO 'UTF8';

-- ============================================================
-- 核心逻辑：按 store_id 分组，每家门店独立网格排列
-- 双人桌(cap<=2): 120x100, 四人桌(cap=3,4): 140x110, 五人以上: 160x120
-- 列间距 165rpx, 行间距 145rpx, 边距 20rpx
-- 容器约 700rpx宽 x 520rpx可用高
-- ============================================================

DO $$
DECLARE
    rec RECORD;
    tbls RECORD;
    idx INT;
    cols INT;
    col INT;
    row INT;
    w INT;
    h INT;
BEGIN
    -- 遍历每个门店
    FOR rec IN SELECT DISTINCT store_id FROM tables ORDER BY store_id LOOP
        -- 计算该门店的列数：根据桌位数自动决定
        -- <=3桌: n列; 4-6桌: 3列; 7+桌: 4列
        SELECT COUNT(*) INTO cols FROM tables WHERE store_id = rec.store_id;
        IF cols <= 3 THEN
            cols := cols;
        ELSIF cols <= 6 THEN
            cols := 3;
        ELSE
            cols := 4;
        END IF;

        idx := 0;

        -- 遍历该门店的每张桌，按 table_id 排序保证稳定
        FOR tbls IN
            SELECT table_id, capacity
            FROM tables
            WHERE store_id = rec.store_id
            ORDER BY table_id
        LOOP
            col := idx % cols;
            row := idx / cols;

            -- 尺寸按容量分档
            IF tbls.capacity IS NULL OR tbls.capacity <= 2 THEN
                w := 120; h := 100;
            ELSIF tbls.capacity <= 4 THEN
                w := 140; h := 110;
            ELSE
                w := 160; h := 120;
            END IF;

            UPDATE tables SET
                top   = 20 + row * 145,
                "left"  = 20 + col * 165,
                width = w,
                height = h
            WHERE table_id = tbls.table_id;

            idx := idx + 1;
        END LOOP;
    END LOOP;
END $$;

-- ============================================================
-- 验证结果：按门店打印坐标
-- ============================================================
SELECT
    s.name AS store_name,
    t.store_id,
    t.table_id,
    t.table_no,
    t.capacity,
    ROUND(t.top::numeric) AS top,
    ROUND(t."left"::numeric) AS left_px,
    t.width,
    t.height
FROM tables t
LEFT JOIN stores s ON s.store_id = t.store_id
ORDER BY t.store_id, t.table_id;
