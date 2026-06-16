/*
 * NekoCafé 预约/下单 高并发压测脚本 (k6)
 * 目标：高峰期 1000 并发预约，核心接口 P95 <= 350ms。
 *
 * 安装 k6: https://k6.io/docs/get-started/installation/
 * 运行示例（1000 VU 持续 1 分钟）：
 *   k6 run -e BASE_URL=http://localhost:8081 -e TOKEN=<JWT> loadtest/k6-reservation.js
 *
 * 重要：本服务把所有异常都包成 HTTP 200 + body.code（见 GlobalExceptionHandler），
 *      所以「是否真的成功」只能看响应里的 code，不能只看 HTTP 状态。脚本据此判定：
 *        code === 0   → 下单成功
 *        code !== 0 且 != 500/401/403 → 业务拒绝（如时段被占），属正常行为
 *        code === 500 / HTTP != 200   → 真·失败（server_error 指标会记录）
 *
 * 护栏（thresholds，跑完直接看 PASS/FAIL，杜绝「假达标」）：
 *   - reserve 接口 P95 < 350ms
 *   - reserve_server_error rate < 1%（链路真坏会触发，光看延迟低≠正常）
 *   - reserve_success rate > SUCCESS_RATE_MIN（防止「全 401/全报错但延迟很低」误判达标）
 *   - http_req_failed rate < 5%（传输层 / 非 200）
 *
 * 关键 env：
 *   BASE_URL, TOKEN(必填), STORE_ID, TABLE_BASE, TABLE_COUNT
 *   FIXED_SLOT=true        → 所有 VU 命中同一 date+time；配合 TABLE_COUNT=1 制造
 *                            真实「同桌同时段」超卖竞争（验证防超卖时用）。
 *   SUCCESS_RATE_MIN=0.90  → 成功率护栏下限；做同桌冲突测试时设 0 关闭该护栏。
 *
 * 验「同桌防超卖」：
 *   k6 run -e BASE_URL=... -e TOKEN=... -e TABLE_COUNT=1 -e FIXED_SLOT=true -e SUCCESS_RATE_MIN=0 \
 *     loadtest/k6-reservation.js
 *   预期：只有 1 笔 code===0，其余被业务拒绝；server_error≈0；DB 中该时段无重复占位。
 *
 * 不要直接压生产远程库；建议指向 staging 或本地起的 PostgreSQL/Redis/RabbitMQ。
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import exec from 'k6/execution';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const TOKEN = __ENV.TOKEN || '';
const STORE_ID = parseInt(__ENV.STORE_ID || '1');
const TABLE_BASE = parseInt(__ENV.TABLE_BASE || '1');
const TABLE_COUNT = parseInt(__ENV.TABLE_COUNT || '200');
const FIXED_SLOT = (__ENV.FIXED_SLOT || 'false').toLowerCase() === 'true';
const SUCCESS_RATE_MIN = __ENV.SUCCESS_RATE_MIN || '0.90';

// 固定时段模式下，所有 VU 命中未来第 7 天的 12:00（在 init 阶段算一次）
const FIXED_DATE = fmtDate(new Date(Date.now() + 7 * 86400000));

const reserveLatency = new Trend('reserve_latency', true);
const bookedRate = new Rate('reserve_success');        // code === 0
const serverErrRate = new Rate('reserve_server_error'); // HTTP != 200 或 code === 500

export const options = {
  scenarios: {
    peak_reservation: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 1000 }, // 爬坡到 1000 并发
        { duration: '60s', target: 1000 }, // 维持 1000 并发
        { duration: '10s', target: 0 },    // 收尾
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    // 核心 KPI：预约接口 P95 <= 350ms
    'http_req_duration{endpoint:reserve}': ['p(95)<350'],
    'reserve_latency': ['p(95)<350'],
    // 防「假达标」：真·失败必须极低，真·成功必须达标
    'reserve_server_error': ['rate<0.01'],
    'reserve_success': [`rate>${SUCCESS_RATE_MIN}`],
    'http_req_failed': ['rate<0.05'],
  },
};

function fmtDate(d) {
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

function futureDateTime() {
  if (FIXED_SLOT) {
    // 所有 VU 同一时段 → 配合 TABLE_COUNT=1 形成真实「同桌同时段」竞争
    return { date: FIXED_DATE, time: '12:00' };
  }
  // 随机分散到未来 30 天内的整点/半点，降低同桌同时段冲突（贴近真实业务）
  const d = new Date(Date.now() + (1 + Math.floor(Math.random() * 30)) * 86400000);
  const hh = String(10 + Math.floor(Math.random() * 10)).padStart(2, '0'); // 10:00-19:00
  const min = Math.random() < 0.5 ? '00' : '30';
  return { date: fmtDate(d), time: `${hh}:${min}` };
}

function authHeaders() {
  return {
    'Content-Type': 'application/json',
    Authorization: TOKEN ? `Bearer ${TOKEN}` : '',
  };
}

function parseCode(res) {
  try { return res.json('code'); } catch (e) { return undefined; }
}

// 预检：在 1000 VU 爬坡前先打一发，token 失效 / 服务不可达 / 服务端报错就直接中止，
// 避免「全程报错但延迟低 → 误判 PASS」。业务拒绝（时段被占）不算失败，放行。
export function setup() {
  if (!TOKEN) {
    exec.test.abort('缺少 TOKEN：请用 -e TOKEN=<有效JWT> 运行');
  }
  const dt = futureDateTime();
  // 用一个远离压测区间的探针桌位，避免污染被压测的桌位
  const probeTableId = TABLE_BASE + TABLE_COUNT + 100000;
  const res = http.post(`${BASE_URL}/api/reservation/create`, JSON.stringify({
    storeId: STORE_ID,
    tableId: probeTableId,
    reserveDate: dt.date,
    reserveTime: dt.time,
    duration: 2,
    persons: 2,
  }), { headers: authHeaders(), tags: { endpoint: 'preflight' } });

  if (res.status !== 200) {
    exec.test.abort(`预检失败：HTTP ${res.status}（服务不可达？）`);
  }
  const code = parseCode(res);
  if (code === 401 || code === 403) {
    exec.test.abort(`预检失败：鉴权 code=${code}（TOKEN 失效或无权限？）`);
  }
  if (code === 500) {
    exec.test.abort(`预检失败：服务端 code=500 → ${res.json('msg')}`);
  }
  console.log(`预检通过：HTTP 200, code=${code}（0=下单成功，非0=业务拒绝但链路正常）；FIXED_SLOT=${FIXED_SLOT}`);
}

export default function () {
  const headers = authHeaders();
  const tableId = TABLE_BASE + Math.floor(Math.random() * TABLE_COUNT);
  const dt = futureDateTime();

  const payload = JSON.stringify({
    storeId: STORE_ID,
    tableId: tableId,
    reserveDate: dt.date,
    reserveTime: dt.time,
    duration: 2,
    persons: 2,
  });

  const res = http.post(`${BASE_URL}/api/reservation/create`, payload, {
    headers,
    tags: { endpoint: 'reserve' },
  });

  reserveLatency.add(res.timings.duration);
  const code = parseCode(res);

  // 真·失败：HTTP 非 200，或业务 code=500（服务端异常）
  serverErrRate.add(res.status !== 200 || code === 500);
  // 真·成功：业务 code===0（时段被占等业务拒绝不计入成功，但也不是失败）
  bookedRate.add(code === 0);

  check(res, {
    'status 200': (r) => r.status === 200,
    'no server error': () => code !== 500,
  });

  sleep(Math.random() * 0.5);
}
