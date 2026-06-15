import http from 'k6/http';
import { check, sleep } from 'k6';

// 1. 定义压测阶段（模拟 1000 并发）
export const options = {
    stages: [
        { duration: '30s', target: 200 },  // 30秒内逐渐激增到 200 个并发用户
        { duration: '1m', target: 1000 },  // 1分钟内冲刺到 1000 个并发用户（核心压测阶段）
        { duration: '30s', target: 0 },    // 30秒内逐渐降至 0，收尾
    ],
    thresholds: {
        http_req_duration: ['p(95)<350'], // 硬性验收指标：95% 的请求响应时间必须小于 350ms
        http_req_failed: ['rate<0.01'],   // 错误率必须小于 1%
    },
};

// 2. 模拟每个用户的前置动作（比如登录获取 JWT Token）
export function setup() {
    const loginUrl = 'http://host.docker.internal:8081/localhost:8081/api/auth/login';
    const payload = JSON.stringify({ phone: '13800000000', code: '1234' });
    const params = { headers: { 'Content-Type': 'application/json' } };

    const res = http.post(loginUrl, payload, params);
    const token = res.json('data.token'); // 假设你的接口返回格式是 { data: { token: 'xxx' } }
    return { token: token };
}

// 3. 核心并发执行的测试场景
export default function (data) {
    const url = 'http://host.docker.internal:8081/localhost:8081/api/reservations/create';

    // 构造请求体：模拟用户选择猫咪主题餐厅的桌位
    const payload = JSON.stringify({
        shopId: 1,
        tableId: 12,
        reserveTime: '2026-05-20 18:00:00',
        catPreferences: ['温顺', '活泼'] // 选做功能：同行猫咪性格标签
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${data.token}`, // 带上鉴权 Token
        },
    };

    // 发起 POST 请求
    const response = http.post(url, payload, params);

    // 断言检查：HTTP 状态码是否为 200 或 201
    check(response, {
        'is status 200/201': (r) => r.status === 200 || r.status === 201,
    });

    // 模拟真实用户操作间隔，每秒请求一次
    sleep(1);
}
