"""
生成 NekoCafé TabBar 图标（极简线条风格 PNG）
普通态：灰色 #999999
激活态：暖橙 #C97E5A
"""
import struct, zlib, os

OUTPUT_DIR = r"D:\tmp\NekoCafe_Smart Reservation Platform\assets\icons"
SIZE = 81   # 微信 TabBar 建议 81×81

# ── 颜色 ────────────────────────────────────────────
GRAY   = (153, 153, 153, 255)   # #999
ORANGE = (201, 126,  90, 255)   # #C97E5A
TRANSPARENT = (0, 0, 0, 0)

def make_png(pixels):
    """将 SIZE×SIZE 的 RGBA 像素列表写成 PNG bytes"""
    def chunk(name, data):
        c = struct.pack(">I", len(data)) + name + data
        return c + struct.pack(">I", zlib.crc32(c[4:]) & 0xFFFFFFFF)

    raw = b""
    for row in pixels:
        raw += b"\x00"   # filter type None
        for px in row:
            raw += bytes(px)

    ihdr = struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0)
    idat = zlib.compress(raw, 9)

    return (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", ihdr)
        + chunk(b"IDAT", idat)
        + chunk(b"IEND", b"")
    )

def empty_canvas():
    return [[list(TRANSPARENT) for _ in range(SIZE)] for _ in range(SIZE)]

def set_px(canvas, x, y, color):
    if 0 <= x < SIZE and 0 <= y < SIZE:
        canvas[y][x] = list(color)

def fill_rect(canvas, x1, y1, x2, y2, color):
    for y in range(y1, y2+1):
        for x in range(x1, x2+1):
            set_px(canvas, x, y, color)

def draw_circle(canvas, cx, cy, r, color, thickness=3):
    """绘制空心圆"""
    for y in range(cy-r-1, cy+r+2):
        for x in range(cx-r-1, cx+r+2):
            d = ((x-cx)**2 + (y-cy)**2)**0.5
            if r - thickness <= d <= r:
                set_px(canvas, x, y, color)

def draw_arc(canvas, cx, cy, r, start_deg, end_deg, color, thickness=3):
    import math
    for deg in range(int(start_deg), int(end_deg)):
        rad = math.radians(deg)
        for dr in range(-thickness//2, thickness//2+1):
            rx = int(round(cx + (r+dr) * math.cos(rad)))
            ry = int(round(cy + (r+dr) * math.sin(rad)))
            set_px(canvas, rx, ry, color)

def save(canvas, path):
    pixels = [[(c[0], c[1], c[2], c[3]) for c in row] for row in canvas]
    with open(path, "wb") as f:
        f.write(make_png(pixels))

# ════════════════════════════════════════════════════════
# 1. 首页图标（小屋形状）
# ════════════════════════════════════════════════════════
def icon_home(color):
    c = empty_canvas()
    # 屋顶三角（斜线）
    mid = SIZE // 2
    for i in range(30):
        # 左斜
        fill_rect(c, mid-i-3, 18+i, mid-i+3, 18+i, color)
        # 右斜
        fill_rect(c, mid+i-3, 18+i, mid+i+3, 18+i, color)
    # 房身矩形
    fill_rect(c, 22, 47, 59, 62, color)
    # 门
    fill_rect(c, 33, 50, 48, 62, TRANSPARENT)
    fill_rect(c, 35, 52, 46, 62, color)
    return c

# ════════════════════════════════════════════════════════
# 2. 预约图标（日历）
# ════════════════════════════════════════════════════════
def icon_reserve(color):
    c = empty_canvas()
    T = 3
    # 外框
    fill_rect(c, 14, 18, 67, 65, color)
    fill_rect(c, 14+T, 18+T, 67-T, 65-T, TRANSPARENT)
    # 顶部栏
    fill_rect(c, 14, 18, 67, 28, color)
    # 钉子
    fill_rect(c, 25, 13, 29, 23, color)
    fill_rect(c, 52, 13, 56, 23, color)
    # 网格点（3×3）
    for row in range(3):
        for col in range(3):
            x = 22 + col * 16
            y = 35 + row * 10
            fill_rect(c, x, y, x+5, y+5, color)
    return c

# ════════════════════════════════════════════════════════
# 3. 点单图标（碗+筷子）
# ════════════════════════════════════════════════════════
def icon_menu(color):
    c = empty_canvas()
    # 碗（下半圆）
    cx, cy, r = 40, 50, 24
    for y in range(cy-r, cy+r+1):
        for x in range(cx-r, cx+r+1):
            d = ((x-cx)**2 + (y-cy)**2)**0.5
            if d <= r and y >= cy-2:
                if d >= r-4:
                    set_px(c, x, y, color)
    # 碗口横线
    fill_rect(c, 16, 27, 64, 31, color)
    # 筷子
    for i in range(3):
        fill_rect(c, 27+i*8, 10, 29+i*8, 26, color)
    return c

# ════════════════════════════════════════════════════════
# 4. 我的图标（人形）
# ════════════════════════════════════════════════════════
def icon_profile(color):
    c = empty_canvas()
    # 头部圆
    draw_circle(c, 40, 24, 12, color, thickness=4)
    fill_rect(c, 30, 14, 50, 34, color)
    fill_rect(c, 32, 16, 48, 32, TRANSPARENT)
    # 实心头
    for y in range(13, 36):
        for x in range(29, 52):
            d = ((x-40)**2 + (y-24)**2)**0.5
            if d <= 12:
                set_px(c, x, y, color)
    # 身体（梯形）
    for y in range(42, 66):
        width = 12 + (y - 42) // 2
        x1 = 40 - width
        x2 = 40 + width
        for x in range(x1, x2+1):
            set_px(c, x, y, color)
    return c

# ════════════════════════════════════════════════════════
# 生成 8 个文件
# ════════════════════════════════════════════════════════
icons = {
    "home":    icon_home,
    "reserve": icon_reserve,
    "menu":    icon_menu,
    "profile": icon_profile,
}

for name, fn in icons.items():
    # 普通态（灰色）
    normal_path = os.path.join(OUTPUT_DIR, f"{name}.png")
    save(fn(GRAY), normal_path)
    print(f"✅ {name}.png")

    # 激活态（橙色）
    active_path = os.path.join(OUTPUT_DIR, f"{name}_active.png")
    save(fn(ORANGE), active_path)
    print(f"✅ {name}_active.png")

print("\n全部图标生成完毕！")
