const fs = require('fs');
const zlib = require('zlib');
const path = require('path');

const SIZE = 81;
const OUT = path.resolve(__dirname);

const GRAY   = [153, 153, 153, 255];
const ORANGE = [201, 126,  90, 255];
const CLEAR  = [0, 0, 0, 0];

function pngChunk(type, data) {
  const len = Buffer.allocUnsafe(4);
  len.writeUInt32BE(data.length, 0);
  const t = Buffer.from(type, 'ascii');
  const crc = Buffer.allocUnsafe(4);
  crc.writeUInt32BE(zlib.crc32(Buffer.concat([t, data])), 0);
  return Buffer.concat([len, t, data, crc]);
}

function makePNG(pixels) {
  // pixels: SIZE x SIZE array of [r,g,b,a]
  const sig = Buffer.from([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]);
  const ihdrData = Buffer.allocUnsafe(13);
  ihdrData.writeUInt32BE(SIZE, 0);
  ihdrData.writeUInt32BE(SIZE, 4);
  ihdrData.writeUInt8(8, 8);   // bit depth
  ihdrData.writeUInt8(6, 9);   // RGBA
  ihdrData.writeUInt8(0, 10);  // compression
  ihdrData.writeUInt8(0, 11);  // filter
  ihdrData.writeUInt8(0, 12);  // interlace

  const raw = Buffer.allocUnsafe(SIZE * (1 + SIZE * 4));
  let p = 0;
  for (let y = 0; y < SIZE; y++) {
    raw[p++] = 0; // filter: None
    for (let x = 0; x < SIZE; x++) {
      const c = pixels[y][x];
      raw[p++] = c[0];
      raw[p++] = c[1];
      raw[p++] = c[2];
      raw[p++] = c[3];
    }
  }

  const idatData = zlib.deflateSync(raw, { level: 9 });
  return Buffer.concat([
    sig,
    pngChunk('IHDR', ihdrData),
    pngChunk('IDAT', idatData),
    pngChunk('IEND', Buffer.alloc(0))
  ]);
}

function canvas() {
  return Array.from({ length: SIZE }, () =>
    Array.from({ length: SIZE }, () => [...CLEAR])
  );
}

function setPx(c, x, y, col) {
  if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) c[y][x] = [...col];
}

function fillRect(c, x1, y1, x2, y2, col) {
  for (let y = Math.max(0, y1); y <= Math.min(SIZE - 1, y2); y++) {
    for (let x = Math.max(0, x1); x <= Math.min(SIZE - 1, x2); x++) {
      c[y][x] = [...col];
    }
  }
}

function drawCircle(c, cx, cy, r, col) {
  for (let y = Math.max(0, cy - r); y <= Math.min(SIZE - 1, cy + r); y++) {
    for (let x = Math.max(0, cx - r); x <= Math.min(SIZE - 1, cx + r); x++) {
      const d = Math.sqrt((x - cx) ** 2 + (y - cy) ** 2);
      if (d <= r) c[y][x] = [...col];
    }
  }
}

// ─── 图标绘制 ──────────────────────────────────────────

function iconHome(col) {
  const c = canvas();
  const m = SIZE >> 1;
  // 屋顶三角
  for (let i = 0; i < 28; i++) {
    fillRect(c, m - i - 2, 18 + i, m - i + 2, 18 + i + 2, col); // 左斜
    fillRect(c, m + i - 2, 18 + i, m + i + 2, 18 + i + 2, col); // 右斜
  }
  // 房身
  fillRect(c, 22, 46, 59, 64, col);
  // 门洞
  fillRect(c, 33, 50, 48, 64, CLEAR);
  fillRect(c, 35, 50, 46, 64, col);
  return c;
}

function iconReserve(col) {
  const c = canvas();
  // 外框
  fillRect(c, 14, 18, 67, 65, col);
  fillRect(c, 18, 22, 63, 61, CLEAR);
  // 顶部条
  fillRect(c, 14, 18, 67, 30, col);
  // 挂钉
  fillRect(c, 24, 12, 30, 22, col);
  fillRect(c, 51, 12, 57, 22, col);
  // 网格点
  for (let row = 0; row < 3; row++) {
    for (let col = 0; col < 3; col++) {
      const x = 22 + col * 15;
      const y = 36 + row * 10;
      fillRect(c, x, y, x + 5, y + 5, col);
    }
  }
  return c;
}

function iconMenu(col) {
  const c = canvas();
  // 碗（下半圆）
  const cx = 40, cy = 52, r = 22;
  for (let y = Math.max(0, cy - r); y <= Math.min(SIZE - 1, cy + r); y++) {
    for (let x = Math.max(0, cx - r); x <= Math.min(SIZE - 1, cx + r); x++) {
      const d = Math.sqrt((x - cx) ** 2 + (y - cy) ** 2);
      if (d <= r && y >= cy - 4) {
        if (d >= r - 4) c[y][x] = [...col];
      }
    }
  }
  // 碗口
  fillRect(c, 18, 26, 62, 30, col);
  // 筷子
  for (let i = 0; i < 3; i++) {
    fillRect(c, 26 + i * 8, 8, 28 + i * 8, 24, col);
  }
  return c;
}

function iconProfile(col) {
  const c = canvas();
  // 头
  drawCircle(c, 40, 25, 13, col);
  // 身体梯形
  for (let y = 42; y < 66; y++) {
    const w = 10 + ((y - 42) >> 1);
    for (let x = 40 - w; x <= 40 + w; x++) {
      setPx(c, x, y, col);
    }
  }
  return c;
}

const icons = {
  home:    iconHome,
  reserve: iconReserve,
  menu:    iconMenu,
  profile: iconProfile,
};

for (const [name, fn] of Object.entries(icons)) {
  // 普通态（灰色）
  fs.writeFileSync(path.join(OUT, `${name}.png`), makePNG(fn(GRAY)));
  console.log(`✅ ${name}.png`);
  // 激活态（橙色）
  fs.writeFileSync(path.join(OUT, `${name}_active.png`), makePNG(fn(ORANGE)));
  console.log(`✅ ${name}_active.png`);
}

console.log('\n🐱 全部 TabBar 图标生成完毕！');
