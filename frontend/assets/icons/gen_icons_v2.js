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
  const sig = Buffer.from([0x89,0x50,0x4E,0x47,0x0D,0x0A,0x1A,0x0A]);
  const ihdr = Buffer.allocUnsafe(13);
  ihdr.writeUInt32BE(SIZE, 0);
  ihdr.writeUInt32BE(SIZE, 4);
  ihdr.writeUInt8(8, 8);
  ihdr.writeUInt8(6, 9);
  ihdr.writeUInt8(0,10); ihdr.writeUInt8(0,11); ihdr.writeUInt8(0,12);

  const raw = Buffer.allocUnsafe(SIZE * (1 + SIZE*4));
  let p = 0;
  for (let y = 0; y < SIZE; y++) {
    raw[p++] = 0;
    for (let x = 0; x < SIZE; x++) {
      const c = pixels[y][x];
      raw[p++] = c[0]; raw[p++] = c[1];
      raw[p++] = c[2]; raw[p++] = c[3];
    }
  }
  const idat = zlib.deflateSync(raw, {level:9});
  return Buffer.concat([sig, pngChunk('IHDR',ihdr), pngChunk('IDAT',idat), pngChunk('IEND',Buffer.alloc(0))]);
}

function canvas() {
  return Array.from({length:SIZE}, () => Array.from({length:SIZE}, () => [...CLEAR]));
}
function setPx(c,x,y,col) {
  if (x>=0&&x<SIZE&&y>=0&&y<SIZE) c[y][x] = [...col];
}
function fillRect(c,x1,y1,x2,y2,col) {
  for (let y=Math.max(0,y1); y<=Math.min(SIZE-1,y2); y++)
    for (let x=Math.max(0,x1); x<=Math.min(SIZE-1,x2); x++)
      c[y][x] = [...col];
}
function drawFilledCircle(c,cx,cy,r,col) {
  for (let y=Math.max(0,cy-r); y<=Math.min(SIZE-1,cy+r); y++)
    for (let x=Math.max(0,cx-r); x<=Math.min(SIZE-1,cx+r); x++)
      if ((x-cx)**2+(y-cy)**2 <= r*r) c[y][x] = [...col];
}

// ── 首页：房子 ──
function iconHome(col) {
  const c = canvas();
  const m = 40;
  // 屋顶（实心三角）
  for (let y = 8; y < 40; y++) {
    const half = Math.round((y - 8) * 0.7);
    for (let x = m - half; x <= m + half; x++) setPx(c, x, y, col);
  }
  // 房身
  fillRect(c, 16, 40, 64, 70, col);
  // 门（透明）
  fillRect(c, 33, 48, 48, 70, CLEAR);
  return c;
}

// ── 预约：日历 ──
function iconReserve(col) {
  const c = canvas();
  // 外框
  fillRect(c, 12, 18, 68, 68, col);
  fillRect(c, 16, 22, 64, 64, CLEAR);
  // 顶部横条
  fillRect(c, 12, 18, 68, 32, col);
  // 两个挂环
  fillRect(c, 24, 10, 30, 22, col);
  fillRect(c, 51, 10, 57, 22, col);
  // 网格：3×3 小方块
  for (let r = 0; r < 3; r++)
    for (let j = 0; j < 3; j++)
      fillRect(c, 20+j*14, 38+r*10, 26+j*14, 42+r*10, col);
  return c;
}

// ── 点单：碗+蒸汽 ──
function iconMenu(col) {
  const c = canvas();
  // 碗身（实心半圆）
  for (let y = 35; y <= 68; y++) {
    const progress = (y - 35) / 33;
    const halfW = Math.round(26 * Math.sin(Math.PI * progress));
    for (let x = 40 - halfW; x <= 40 + halfW; x++) setPx(c, x, y, col);
  }
  // 碗口
  fillRect(c, 12, 33, 68, 37, col);
  // 筷子（三条线）
  for (let i = 0; i < 3; i++) {
    const x = 26 + i * 7;
    for (let y = 8; y <= 34; y++) setPx(c, x, y, col);
  }
  return c;
}

// ── 我的：人像 ──
function iconProfile(col) {
  const c = canvas();
  // 头（实心圆）
  drawFilledCircle(c, 40, 24, 14, col);
  // 身体（半圆/梯形）
  for (let y = 46; y <= 70; y++) {
    const progress = (y - 46) / 24;
    const halfW = Math.round(12 + progress * 18);
    for (let x = 40 - halfW; x <= 40 + halfW; x++) setPx(c, x, y, col);
  }
  return c;
}

const icons = { home: iconHome, reserve: iconReserve, menu: iconMenu, profile: iconProfile };
for (const [name, fn] of Object.entries(icons)) {
  fs.writeFileSync(path.join(OUT, `${name}.png`), makePNG(fn(GRAY)));
  console.log(`✅ ${name}.png`);
  fs.writeFileSync(path.join(OUT, `${name}_active.png`), makePNG(fn(ORANGE)));
  console.log(`✅ ${name}_active.png`);
}
console.log('\n图标生成完毕！');
