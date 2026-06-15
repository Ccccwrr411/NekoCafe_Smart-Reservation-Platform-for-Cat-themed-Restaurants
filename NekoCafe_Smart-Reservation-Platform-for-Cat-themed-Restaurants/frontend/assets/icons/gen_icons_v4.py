"""
TabBar 图标生成 - 粗线条版本
确保每个图标内容足够粗、足够明显
"""
import struct, zlib, os

OUT = r"D:\tmp\NekoCafe_Smart Reservation Platform\assets\icons"
SZ = 81
TP = (0, 0, 0, 0)

def make_png(px):
    def chunk(name, data):
        c = struct.pack(">I", len(data)) + name + data
        return c + struct.pack(">I", zlib.crc32(c[4:]) & 0xFFFFFFFF)
    ihdr = struct.pack(">IIBBBBB", SZ, SZ, 8, 6, 0, 0, 0)
    raw = b""
    for y in range(SZ):
        raw += b"\x00"
        for x in range(SZ):
            r, g, b, a = px[y][x]
            raw += bytes([r, g, b, a])
    idat = zlib.compress(raw, 9)
    return b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", idat) + chunk(b"IEND", b"")

def blank():
    return [[TP for _ in range(SZ)] for _ in range(SZ)]

def sp(px, x, y, color):
    if 0 <= x < SZ and 0 <= y < SZ:
        px[y][x] = color

def fr(px, x1, y1, x2, y2, color):
    for y in range(max(0,y1), min(SZ,y2+1)):
        for x in range(max(0,x1), min(SZ,x2+1)):
            px[y][x] = color

def thick_line_h(px, x1, x2, y, t, color):
    """水平粗线，厚度 t"""
    for dy in range(-t//2, t//2+1):
        for x in range(x1, x2+1):
            sp(px, x, y+dy, color)

def thick_line_v(px, x, y1, y2, t, color):
    """垂直粗线，厚度 t"""
    for dx in range(-t//2, t//2+1):
        for y in range(y1, y2+1):
            sp(px, x+dx, y, color)

def circle_fill(px, cx, cy, r, color):
    for y in range(max(0,cy-r), min(SZ,cy+r+1)):
        for x in range(max(0,cx-r), min(SZ,cx+r+1)):
            if (x-cx)**2 + (y-cy)**2 <= r*r:
                px[y][x] = color

def circle_outline(px, cx, cy, r, t, color):
    """空心圆，线宽 t"""
    for y in range(max(0,cy-r-t), min(SZ,cy+r+t+1)):
        for x in range(max(0,cx-r-t), min(SZ,cx+r+t+1)):
            d = ((x-cx)**2 + (y-cy)**2)**0.5
            if r - t/2 <= d <= r + t/2:
                px[y][x] = color

def triangle_fill(px, x1, y1, x2, y2, x3, y3, color):
    """填充三角形"""
    min_y = max(0, min(y1,y2,y3))
    max_y = min(SZ-1, max(y1,y2,y3))
    for y in range(min_y, max_y+1):
        xs = []
        for (ax, ay, bx, by) in [(x1,y1,x2,y2),(x2,y2,x3,y3),(x3,y3,x1,y1)]:
            if ay == by:
                if y == ay: xs.append(ax)
            else:
                t = (y - ay) / (by - ay)
                if 0 <= t <= 1:
                    xs.append(int(ax + t * (bx - ax)))
        if len(xs) >= 2:
            xs.sort()
            for x in range(max(0,xs[0]), min(SZ,xs[-1]+1)):
                sp(px, x, y, color)

# ── 首页：粗房子 ──
def draw_home(col):
    px = blank()
    # 屋顶：粗三角（用填充）
    triangle_fill(px, 40, 10, 12, 42, 68, 42, col)
    # 房身：粗边框
    fr(px, 18, 42, 62, 68, col)    # 外框（先填实）
    fr(px, 22, 46, 58, 64, TP)     # 内部挖空
    # 门：保留门洞
    fr(px, 33, 48, 48, 68, TP)
    # 门框线
    thick_line_v(px, 33, 48, 68, 3, col)
    thick_line_v(px, 48, 48, 68, 3, col)
    thick_line_h(px, 33, 48, 48, 3, col)
    return px

# ── 预约：粗日历 ──
def draw_reserve(col):
    px = blank()
    # 外框：粗线
    thickness = 4
    for t in range(thickness):
        fr(px, 14+t, 18+t, 67-t, 18+t, col)   # 上
        fr(px, 14+t, 68-t, 67-t, 68-t, col)     # 下
        fr(px, 14+t, 18+t, 14+t, 68-t, col)     # 左
        fr(px, 67-t, 18+t, 67-t, 68-t, col)     # 右
    # 顶部横条（实心）
    fr(px, 14, 18, 67, 30, col)
    # 挂环（两个圆）
    circle_fill(px, 27, 15, 5, col)
    circle_fill(px, 54, 15, 5, col)
    # 日期点（大一点）
    for row in range(3):
        for j in range(3):
            x0 = 22 + j*16
            y0 = 38 + row*11
            fr(px, x0, y0, x0+7, y0+7, col)
    return px

# ── 点单：粗碗+筷子 ──
def draw_menu(col):
    px = blank()
    # 碗：粗半圆
    for y in range(36, 69):
        progress = (y - 36) / 32.0
        hw = int(22 * ((1 - (progress - 0.5)**2 * 3.8)) ** 0.5) if y < 52 else int(22 * max(0, 1 - ((y-52)/17.0)**2)**0.5)
        if hw < 3: hw = 3
        for x in range(40 - hw, 40 + hw + 1):
            # 只画边缘（线宽4）
            if hw - 4 <= (x - 40) <= hw or y >= 66:
                sp(px, x, y, col)
    # 碗口粗线
    thick_line_h(px, 14, 66, 35, 5, col)
    # 筷子（粗）
    thick_line_v(px, 28, 8, 36, 4, col)
    thick_line_v(px, 38, 8, 36, 4, col)
    thick_line_v(px, 48, 8, 36, 4, col)
    return px

# ── 我的：粗人像 ──
def draw_profile(col):
    px = blank()
    # 头：实心圆
    circle_fill(px, 40, 24, 14, col)
    # 身体：实心梯形
    for y in range(46, 71):
        progress = (y - 46) / 24.0
        hw = int(10 + progress * 24)
        for x in range(40 - hw, 40 + hw + 1):
            sp(px, x, y, col)
    return px

GRAY   = (153, 153, 153, 255)
ORANGE = (201, 126,  90, 255)

funcs = [("home", draw_home), ("reserve", draw_reserve), ("menu", draw_menu), ("profile", draw_profile)]

for name, fn in funcs:
    px = fn(GRAY)
    p = os.path.join(OUT, f"{name}.png")
    with open(p, "wb") as f:
        f.write(make_png(px))
    print(f"✅ {name}.png ({os.path.getsize(p)} B)")

    px = fn(ORANGE)
    p = os.path.join(OUT, f"{name}_active.png")
    with open(p, "wb") as f:
        f.write(make_png(px))
    print(f"✅ {name}_active.png ({os.path.getsize(p)} B)")

print("\n完成！请在微信开发者工具中重新编译预览。")
