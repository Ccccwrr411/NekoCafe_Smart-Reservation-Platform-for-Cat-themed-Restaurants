"""
生成 NekoCafe TabBar 图标 - 简化版，确保图标内容清晰可见
用法：python gen_icons_v3.py
"""
import struct, zlib, os

OUT = r"D:\tmp\NekoCafe_Smart Reservation Platform\assets\icons"
SZ = 81

GRAY   = (153, 153, 153, 255)
ORANGE = (201, 126,  90, 255)
TP     = (  0,   0,   0,   0)

def make_png(pixels):
    """pixels: SZ×SZ list of (r,g,b,a) tuples"""
    def chunk(name, data):
        c = struct.pack(">I", len(data)) + name + data
        return c + struct.pack(">I", zlib.crc32(c[4:]) & 0xFFFFFFFF)
    # IHDR
    ihdr = struct.pack(">IIBBBBB", SZ, SZ, 8, 6, 0, 0, 0)
    # raw image data
    raw = b""
    for y in range(SZ):
        raw += b"\x00"
        for x in range(SZ):
            r, g, b, a = pixels[y][x]
            raw += bytes([r, g, b, a])
    idat = zlib.compress(raw, 9)
    sig = b"\x89PNG\r\n\x1a\n"
    return sig + chunk(b"IHDR", ihdr) + chunk(b"IDAT", idat) + chunk(b"IEND", b"")

def blank():
    return [[TP for _ in range(SZ)] for _ in range(SZ)]

def set_px(px, x, y, color):
    if 0 <= x < SZ and 0 <= y < SZ:
        px[y][x] = color

def fill_rect(px, x1, y1, x2, y2, color):
    for y in range(max(0,y1), min(SZ,y2+1)):
        for x in range(max(0,x1), min(SZ,x2+1)):
            px[y][x] = color

def circle(px, cx, cy, r, color, fill=False):
    for y in range(max(0,cy-r), min(SZ,cy+r+1)):
        for x in range(max(0,cx-r), min(SZ,cx+r+1)):
            d2 = (x-cx)**2 + (y-cy)**2
            if d2 <= r*r:
                if fill or (r*r - d2) < (r-3)**2:
                    px[y][x] = color

# ── 首页：实心房子 ──
def draw_home(col):
    px = blank()
    # 屋顶：实心三角
    for y in range(10, 42):
        half = int((y - 10) * 0.75)
        for x in range(40 - half, 40 + half + 1):
            set_px(px, x, y, col)
    # 房身
    fill_rect(px, 16, 42, 64, 70, col)
    # 门（透明）
    for y in range(48, 71):
        for x in range(34, 49):
            px[y][x] = TP
    # 门框
    for y in range(48, 71):
        set_px(px, 34, y, col)
        set_px(px, 48, y, col)
    for x in range(34, 49):
        set_px(px, x, 48, col)
    return px

# ── 预约：实心日历 ──
def draw_reserve(col):
    px = blank()
    # 主体
    fill_rect(px, 14, 20, 66, 68, col)
    fill_rect(px, 18, 24, 62, 64, TP)
    # 顶部横条
    fill_rect(px, 14, 20, 66, 30, col)
    # 挂环
    fill_rect(px, 25, 12, 31, 22, col)
    fill_rect(px, 50, 12, 56, 22, col)
    # 日期点 3×3
    for row in range(3):
        for j in range(3):
            x0 = 22 + j * 13
            y0 = 36 + row * 11
            fill_rect(px, x0, y0, x0+4, y0+4, col)
    return px

# ── 点单：实心碗 ──
def draw_menu(col):
    px = blank()
    # 碗身（实心半圆）
    for y in range(36, 69):
        progress = (y - 36) / 32.0
        hw = int(24 * (1 - (progress - 0.5)**2 * 4)) if y < 52 else int(24 * (1 - ((y-52)/16.0)**2))
        if hw < 4: hw = 4
        for x in range(40 - hw, 40 + hw + 1):
            set_px(px, x, y, col)
    # 碗口
    fill_rect(px, 12, 34, 68, 38, col)
    # 筷子
    for y in range(8, 36):
        set_px(px, 27, y, col)
        set_px(px, 35, y, col)
        set_px(px, 43, y, col)
    return px

# ── 我的：人像 ──
def draw_profile(col):
    px = blank()
    # 头（实心圆）
    circle(px, 40, 24, 14, col, fill=True)
    # 身体（实心梯形）
    for y in range(46, 71):
        progress = (y - 46) / 24.0
        hw = int(10 + progress * 22)
        for x in range(40 - hw, 40 + hw + 1):
            set_px(px, x, y, col)
    return px

funcs = [
    ("home",    draw_home),
    ("reserve", draw_reserve),
    ("menu",    draw_menu),
    ("profile", draw_profile),
]

for name, fn in funcs:
    px = fn(GRAY)
    path = os.path.join(OUT, f"{name}.png")
    with open(path, "wb") as f:
        f.write(make_png(px))
    print(f"✅ {name}.png  ({os.path.getsize(path)} bytes)")

    px = fn(ORANGE)
    path = os.path.join(OUT, f"{name}_active.png")
    with open(path, "wb") as f:
        f.write(make_png(px))
    print(f"✅ {name}_active.png  ({os.path.getsize(path)} bytes)")

print("\n全部完成！")
