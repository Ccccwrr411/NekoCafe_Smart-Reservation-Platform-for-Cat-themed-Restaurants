# NekoCafé 猫咖智能预约平台 — 前后端接口契约

> 最后更新：2026-06-26（基于后端 16 个 Controller 实际代码同步）
> 前端项目：NekoCafe Smart Reservation Platform
> 文档用途：前后端接口对齐基准

---

## 一、通用约定

### 1.1 Base URL

```
开发环境：http://172.20.10.3:8081
生产环境：待定（上线前替换）
```

### 1.2 认证方式

除 `/api/auth/**` 和 `/api/queue/status` 外，所有接口需在请求头携带 Token：

```
Authorization: Bearer <token>
```

Token 由登录接口返回，前端存储在 `wx.getStorageSync('token')`。后端通过 `AuthInterceptor` 从 Token 解析 `userId` 并注入 `request.setAttribute("userId")`。

### 1.3 统一响应格式

```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 0 = 成功，非 0 = 业务错误码 |
| message | String | 提示信息 |
| data | T | 业务数据（具体结构见各接口） |

**错误码约定**：

| code | 含义 |
|------|------|
| 0 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 / Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 1.4 Content-Type

请求体统一使用 `application/json`。文件上传接口使用 `multipart/form-data`。

### 1.5 静态资源

所有图片由后端作为静态文件提供，接口返回的 `imageUrl` / `avatarUrl` 字段为相对路径（如 `/uploads/avatars/1_abc12345.jpg`），前端拼接 Base URL 后使用。

---

## 二、接口清单

共 **52 个接口**，按业务模块分组。标注 🆕 的为本次同步新增。

---

### 模块 A：认证 (AuthController — `/api/auth`)

#### A-1. 微信登录

```
POST /api/auth/login
```

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| code | body | string | 是 | wx.login() 返回的临时 code |

**请求示例**：

```json
{ "code": "0a3xYzGa1b2cDeFgHiJkLmNoPqRsTuV" }
```

**响应 data**（LoginVO）：

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "userInfo": {
    "id": 1001, "nickName": "猫咖爱好者", "avatarUrl": "/uploads/avatars/default.png",
    "phone": "138****8888", "memberLevel": "银卡会员", "points": 320
  }
}
```

---

#### A-2. 🆕 微信快捷登录

```
POST /api/auth/wx-login
```

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| code | body | string | 是 | wx.login() code |

**响应 data**：同 A-1（LoginVO）。

> 与 A-1 的区别：A-1 走完整的 OAuth 流程（获取用户信息），A-2 仅用 code 查 openid 快速登录。

---

#### A-3. 发送验证码（沙箱模式）

```
POST /api/auth/send-code
```

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| phone | body | string | 是 | 11 位手机号 |

**响应 data**：

```json
{ "code": 0, "data": { "code": "123456" } }
```

> 沙箱模式下验证码直接返回。生产环境 `nekocafe.sandbox.enabled=false` 后走真实短信。

---

#### A-4. 手机号注册

```
POST /api/auth/register
```

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| phone | body | string | 是 | 手机号 |
| password | body | string | 是 | 密码 |
| code | body | string | 是 | 短信验证码 |

**响应 data**：同 A-1（LoginVO）。

---

#### A-5. 手机号密码登录

```
POST /api/auth/login/phone
```

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| phone | body | string | 是 | 手机号 |
| password | body | string | 是 | 密码 |

**响应 data**：同 A-1（LoginVO）。

---

### 模块 B：门店 (StoreController)

#### B-1. 门店列表

```
GET /api/stores
```

无参数。

**响应 data**：`List<StoreVO>`

```json
[
  {
    "id": 1, "name": "NekoCafé 朝阳店", "address": "朝阳区三里屯太古里南区 B1-01",
    "distance": 1.2, "lat": 39.9325, "lng": 116.4551,
    "avgPrice": 68, "rating": 4.8, "catCount": 12,
    "imageUrl": "/uploads/stores/store_1.jpg",
    "tags": ["环境好", "猫咪多", "适合拍照"],
    "openTime": "10:00 - 22:00", "status": "open"
  }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 门店 ID |
| name | string | 门店名称 |
| address | string | 详细地址 |
| distance | float | 距离（km） |
| lat | float | 纬度 |
| lng | float | 经度 |
| avgPrice | int | 人均消费（元） |
| rating | float | 评分（1-5） |
| catCount | int | 猫咪数量 |
| imageUrl | string | 门店封面图 |
| tags | string[] | 标签 |
| openTime | string | 营业时间 |
| status | string | open=营业中 / closed=休息中 |

---

### 模块 C：桌位 (TableController)

#### C-1. 桌位列表

```
GET /api/tables?storeId={storeId}&reserveDate={date}&reserveTime={time}&duration={hours}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | Integer | 是 | 门店 ID |
| reserveDate | String | 否 | 预约日期（YYYY-MM-DD），传入时检查时段冲突 |
| reserveTime | String | 否 | 预约时间（HH:mm） |
| duration | Integer | 否 | 时长（小时） |

**响应 data**：`List<TableVO>`

```json
[
  {
    "id": 101, "name": "A1", "type": "双人桌", "capacity": 2,
    "status": "available", "catType": "布偶猫", "catName": "奶油", "price": 0
  }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 桌位 ID |
| name | string | 桌号 |
| type | string | 双人桌/四人桌/包间/吧台位 |
| capacity | int | 容量（人数） |
| status | string | available=可预订 / booked=已预订 / maintenance=维护中 |
| catType | string | 对应猫咪品种 |
| catName | string | 对应猫咪名字 |
| price | int | 附加费用（元，包间通常有） |

---

### 模块 D：菜单 (MenuController)

#### D-1. 菜品列表

```
GET /api/menu?storeId={storeId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | Integer | 是 | 门店 ID |

**响应 data**：`MenuVO`

```json
{
  "categories": [{ "id": 1, "name": "招牌饮品", "icon": "☕" }],
  "items": [
    {
      "id": 201, "categoryId": 1, "name": "猫爪拿铁", "price": 38,
      "imageUrl": "/uploads/menu/item_201.jpg",
      "desc": "布偶猫爪造型，每天限量30杯",
      "sales": 328, "rating": 4.9, "isHot": true, "isNew": false
    }
  ]
}
```

---

### 模块 E：订单与预约 (OrderController)

#### E-1. 订单列表

```
GET /api/orders?userId={userId}&status={status}&keyword={keyword}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 否 | 用户 ID（不传则从 Token 获取） |
| status | String | 否 | 按状态筛选 |
| keyword | String | 否 | 搜索关键词 |

**响应 data**：`List<OrderVO>`

```json
[
  {
    "id": "ORD20260601001", "storeId": 1, "storeName": "NekoCafé 朝阳店",
    "tableId": 101, "tableName": "A1双人桌",
    "reserveDate": "2026-06-01", "reserveTime": "14:00",
    "duration": 2, "persons": 2,
    "status": "completed", "totalAmount": 146,
    "createTime": "2026-05-30 10:22:15",
    "items": [{ "name": "猫爪拿铁", "qty": 2, "price": 38 }],
    "hasReview": true
  }
]
```

#### E-2. 提交订单（含点单）

```
POST /api/order/submit
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long(Query) | 否 | 用户 ID（不传从 Token 取） |
| storeId | int(body) | 是 | 门店 ID |
| items | array(body) | 是 | 购物车内容 |
| items[].menuId | int | 是 | 菜品 ID |
| items[].name | string | 是 | 菜品名 |
| items[].price | int | 是 | 单价 |
| items[].qty | int | 是 | 数量 |
| totalAmount | int | 是 | 原始总价 |
| finalAmount | int | 是 | 实付金额（优惠后） |
| discount | int | 是 | 优惠金额 |
| couponIds | string[] | 否 | 使用的优惠券 ID |
| remark | string | 否 | 备注 |
| tableId | int | 否 | 关联桌位 ID |

**响应 data**：

```json
{
  "orderId": "ORD20260603003",
  "totalAmount": 146, "finalAmount": 126,
  "payInfo": { "timeStamp": "...", "nonceStr": "...", "package": "...", "signType": "RSA", "paySign": "..." }
}
```

#### E-3. 订单详情

```
GET /api/order/detail?orderId={orderId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | String | 是 | 订单号 |

**响应 data**：`OrderVO`（含 `items`、`timeline`、`canCancel`、`canReschedule`、`canRefund`、`hasReview`）

#### E-4. 取消订单

```
POST /api/order/cancel
```

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| userId | Query | Long | 否 | 从 Token 获取 |
| orderId | body | String | 是 | 订单号 |

**响应 data**：`{ "status": "cancelled", "refundAmount": 126 }`

#### E-5. 改约

```
POST /api/order/reschedule
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | String(body) | 是 | 订单号 |
| newReserveDate | String(body) | 是 | 新预约日期 |
| newReserveTime | String(body) | 是 | 新预约时间 |

**响应 data**：`{ "orderId": "...", "newReserveDate": "...", "newReserveTime": "..." }`

> **注意**：当前后端注释标注此接口"已废弃"，建议取消后重新预约。实际仍可用但推荐使用 E-10 重新激活。

#### E-6. 申请退款

```
POST /api/order/refund
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | String(body) | 是 | 订单号 |
| refundReason | String(body) | 否 | 退款原因 |

**响应 data**：`{ "refundId": "...", "refundAmount": 126, "status": "processing" }`

#### E-7. 纯预约（无点单）

```
POST /api/reservation/create
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | int | 是 | 门店 ID |
| tableId | int | 是 | 桌位 ID |
| reserveDate | string | 是 | 预约日期（YYYY-MM-DD） |
| reserveTime | string | 是 | 预约时间（HH:mm） |
| persons | int | 是 | 人数 |
| duration | int | 是 | 时长（小时） |

**响应 data**：`{ "orderId": "ORD20260603002", "status": "confirmed" }`

#### E-8. 🆕 确认支付（沙箱模式）

```
POST /api/order/confirm-payment
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | String(body) | 是 | 订单号 |

**响应 data**：`{ "orderId": "...", "status": "PAID" }`

> 沙箱模式下用户点击"模拟支付成功"后调用。生产环境走真实微信支付回调。

#### E-9. 🆕 当前预约列表

```
GET /api/reservation/current
```

无参数（userId 从 Token 获取）。返回用户当前 BOOKED 状态的预约列表，供点单页面选择。

**响应 data**：`List<CurrentReservationVO>`

#### E-10. 🆕 重新激活已取消订单

```
POST /api/order/reactivate
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | String(body) | 是 | 已取消的订单号 |

**响应 data**：`{ "orderId": "...", "status": "BOOKED" }`

> 将 CANCEL_ORDER 状态的预约恢复为 BOOKED。

---

### 模块 F：用户 (UserController — `/api/user`)

#### F-1. 用户信息

```
GET /api/user/profile
```

无参数（userId 从 Token 解析）。

**响应 data**：`UserProfileVO`

```json
{
  "id": 1001, "nickName": "猫咖爱好者", "avatarUrl": "/uploads/avatars/1_abc.jpg",
  "phone": "138****8888", "memberLevel": "银卡会员",
  "points": 320, "pointsToNext": 680, "nextLevel": "金卡会员",
  "totalOrders": 12, "totalSpent": 896, "couponCount": 3,
  "favoriteStores": [1, 4], "joinDate": "2025-09-01"
}
```

#### F-2. 实名认证

```
POST /api/user/realname
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| realName | String(body) | 是 | 真实姓名 |
| idCard | String(body) | 是 | 身份证号 |

**响应 data**：`{ "verified": true, "realName": "张三", "idCardMask": "110101********1234" }`

#### F-3. 🆕 更新个人信息

```
POST /api/user/profile
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickName | String(body) | 否 | 新昵称 |
| avatarUrl | String(body) | 否 | 新头像 URL |
| phone | String(body) | 否 | 新手机号 |
| email | String(body) | 否 | 新邮箱 |

**响应 data**：`UserProfileVO`（更新后的完整用户信息）

#### F-4. 🆕 修改密码

```
POST /api/user/change-password
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String(body) | 是 | 旧密码 |
| newPassword | String(body) | 是 | 新密码 |

**响应 data**：`{ "success": true }`

#### F-5. 🆕 上传头像

```
POST /api/user/upload-avatar
Content-Type: multipart/form-data
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 图片文件（JPG/PNG/WebP，≤2MB） |

**响应 data**：`{ "avatarUrl": "/uploads/avatars/1_abc12345.jpg", "success": true }`

---

### 模块 G：优惠券与促销 (CouponController)

#### G-1. 我的优惠券

```
GET /api/coupons
```

无参数（userId 从 Token 获取）。

**响应 data**：`List<CouponVO>`

```json
[
  {
    "id": "CPN001", "name": "新人专享 8折券", "type": "discount",
    "value": 0.8, "maxDiscount": 20, "minAmount": 50,
    "expireDate": "2026-07-31", "status": "unused",
    "stackable": true, "ruleId": "RULE_DISCOUNT_20"
  }
]
```

#### G-2. 可用优惠券

```
GET /api/coupons/available?storeId={storeId}&amount={amount}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | Integer | 是 | 门店 ID |
| amount | Integer | 是 | 订单原始总额（元） |

**响应 data**：同 G-1，额外含 `saving` 预估节省金额。

#### G-3. 促销活动规则

```
GET /api/promotions/rules
```

**响应 data**：`Map<String, Object>`

```json
{
  "activePromotions": [
    { "id": "PROMO001", "name": "工作日满减", "type": "cashback",
      "rule": "满150减15", "desc": "周一至周五可用",
      "minAmount": 150, "value": 15, "stackable": true }
  ],
  "stackingRules": { "enabled": true, "maxStackCount": 2, "rules": ["..."] }
}
```

#### G-4. 优惠试算

```
POST /api/promotions/calculate
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | int | 是 | 门店 ID |
| amount | int | 是 | 原始金额 |
| couponIds | string[] | 否 | 选中的优惠券 ID |

**响应 data**：

```json
{
  "originalAmount": 146, "totalDiscount": 20, "finalAmount": 126,
  "appliedPromotions": [{ "name": "新人专享 8折券", "type": "discount", "saved": 20 }],
  "breakdown": [
    { "label": "商品原价", "amount": 146 },
    { "label": "新人专享 8折券", "amount": -20, "type": "discount" }
  ]
}
```

> **当前前端状态**：优惠计算在前端 `order.js` 的 `calcDiscount()` 中本地完成。建议改为调用此后端接口。

---

### 模块 H：AI 推荐 (RecommendController)

#### H-1. 个性化推荐

```
GET /api/recommend?userId={userId}&companionCount={n}&hasChild={bool}&storeId={storeId}
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| userId | Long(Query) | 否 | — | 用户 ID（开发阶段可通过 Query 传入，上线后从 Token 取） |
| companionCount | Integer | 否 | 1 | 同行人数，用于桌位匹配 |
| hasChild | Boolean | 否 | false | 是否带小孩，用于温顺猫筛选 |
| storeId | Integer | 否 | — | 门店 ID，按门店过滤推荐结果 |

**响应 data**：

```json
{
  "reason": "根据您的偏好和猫咪性格匹配，为您推荐",
  "tables": [
    { "id": 101, "name": "A1", "catName": "奶油", "catBreed": "布偶猫",
      "matchScore": 98, "matchReason": "您偏好温顺粘人的猫咪" }
  ],
  "dishes": [
    { "id": 201, "name": "猫爪拿铁", "price": 38, "reason": "您的最爱，累计点了6次" }
  ],
  "userProfile": {
    "favoriteBreeds": ["布偶猫", "英短"],
    "favoritePersonalities": ["粘人", "温柔"],
    "tastePreference": ["咖啡", "甜品"],
    "visitFrequency": "每周1-2次"
  }
}
```

---

### 模块 I：猫咪档案 (CatController)

#### I-1. 猫咪列表

```
GET /api/cats?storeId={storeId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | Integer | 否 | 门店 ID（不传返回全部，用于总部运营） |

**响应 data**：`List<CatVO>`

```json
[
  {
    "id": 1, "name": "奶油", "breed": "布偶猫", "age": 2, "gender": "母",
    "imageUrl": "/uploads/cats/cat_1.jpg", "weight": 4.2,
    "desc": "温顺爱撒娇", "personality": ["粘人", "爱睡觉", "不抓人"],
    "vaccineDue": "2026-08-15"
  }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 猫咪 ID |
| name | string | 名字 |
| breed | string | 品种 |
| age | int | 年龄（岁） |
| gender | string | 公/母 |
| imageUrl | string | 照片路径 |
| weight | float | 体重（kg） |
| desc | string | 简介 |
| personality | string[] | 性格标签 |
| vaccineDue | string | 下次疫苗日期 |

#### I-2. 猫咪详情

```
GET /api/cats/detail?catId={catId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| catId | Integer | 是 | 猫咪 ID |

**响应 data**：`CatVO`（含完整健康信息、疫苗记录等）

---

### 模块 J：排队 (QueueController — `/api/queue`)

#### J-1. 排队状态

```
GET /api/queue/status?storeId={storeId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | Integer | 是 | 门店 ID |

> 此接口在 JWT 拦截器白名单中（允许未登录访问），但已登录用户会返回 `myNumber`。

**响应 data**：`QueueStatusVO`

```json
{
  "storeId": 1, "waitingCount": 4, "avgWaitMinutes": 15,
  "currentNumber": 12, "myNumber": null, "myWaitMinutes": 0,
  "queueList": [{ "number": 13, "persons": 2, "type": "双人桌", "ahead": 0 }]
}
```

#### J-2. 取号

```
POST /api/queue/take
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | int | 是 | 门店 ID |
| persons | int | 是 | 人数 |
| type | string | 是 | 桌型 |

**响应 data**：`{ "number": 17, "persons": 2, "type": "双人桌", "ahead": 4, "estWaitMinutes": 20 }`

#### J-3. 🆕 店员叫号

```
POST /api/queue/call
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | Integer | 是 | 门店 ID |
| queueId | Long | 是 | 排队记录 ID |

**响应 data**：已叫号的排队记录。

#### J-4. 🆕 用户确认叫号

```
POST /api/queue/confirm
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| queueId | Long | 是 | 排队记录 ID |

**响应 data**：`null`（code=0）

---

### 模块 K：数据看板 (StaffController — `/api/dashboard/metrics`)

#### K-1. 运营指标

```
GET /api/dashboard/metrics?storeId={storeId}&range={range}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | Integer | 是 | 门店 ID |
| range | String | 是 | 时间范围：7d / 30d / 90d |

**响应 data**：`DashboardMetricsVO`（含坪效趋势、翻台率、复购率、今日概览）

---

### 模块 L：店员后台 (StaffController)

#### L-1. 店员端桌位状态

```
GET /api/staff/tables?storeId={storeId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | Integer | 是 | 门店 ID |

**响应 data**：

```json
[
  {
    "id": 101, "name": "A1", "type": "双人桌", "status": "occupied",
    "customer": "猫咖爱好者", "arriveTime": "14:00",
    "estLeaveTime": "16:00", "catType": "布偶猫"
  }
]
```

#### L-2. 异常告警

```
GET /api/staff/alerts?storeId={storeId}
```

**响应 data**：

```json
[
  { "id": "ALT001", "level": "warning", "type": "overstay",
    "title": "B2 超时未离店", "desc": "已超预约时长30分钟", "time": "16:00" }
]
```

#### L-3. 🆕 门店订单列表

```
GET /api/staff/orders?storeId={storeId}
```

返回指定门店的全部订单。

#### L-4. 🆕 店员接单（确认到店）

```
POST /api/staff/order/accept
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reservationId | Long | 是 | 预约 ID |

#### L-5. 🆕 桌位调度

```
POST /api/staff/table/dispatch
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| tableId | Integer | 是 | 桌位 ID |
| status | String | 是 | 目标状态（available/occupied/cleaning） |

#### L-6. 🆕 订单进度推进

```
POST /api/staff/order/progress
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reservationId | Long | 是 | 预约 ID |
| targetStatus | String | 是 | 目标状态 |

#### L-7. 🆕 退款申请列表

```
GET /api/staff/refunds?storeId={storeId}
```

#### L-8. 🆕 审核退款

```
POST /api/staff/refund/review
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refundId | Long | 是 | 退款记录 ID |
| action | String | 是 | approve / reject |
| operatorId | Long | 否 | 操作人 ID |
| rejectReason | String | 否 | 拒绝原因 |

#### L-9. 🆕 告警已知晓

```
POST /api/staff/alert/acknowledge
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exceptionId | Long | 是 | 异常记录 ID |
| operatorId | Long | 否 | 操作人 ID |

#### L-10. 🆕 解决告警

```
POST /api/staff/alert/resolve
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exceptionId | Long | 是 | 异常记录 ID |
| resolution | String | 是 | 解决方案描述 |
| operatorId | Long | 否 | 操作人 ID |

#### L-11. 🆕 考勤异常申请

```
POST /api/staff/shift-exception/submit
```

提交请假/加班/调班申请。

#### L-12. 🆕 我的考勤申请历史

```
GET /api/staff/shift-exceptions/my?storeId={storeId}&staffId={staffId}
```

#### L-13. 🆕 猫咪健康打卡

```
POST /api/staff/cat/health
```

猫咪管家专用，录入健康记录。

---

### 模块 M：评价 (ReviewController)

#### M-1. 提交评价

```
POST /api/review/submit
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | string | 是 | 订单号 |
| rating | int | 是 | 评分（1-5） |
| tags | string[] | 是 | 评价标签 |
| content | string | 否 | 评价内容 |

**响应 data**：`{ "reviewId": "REV...", "status": "published", "pointsEarned": 10 }`

#### M-2. 🆕 查看评价详情

```
GET /api/review/detail?orderId={orderId}
```

查看某订单的已有评价。

---

### 模块 N：通知中心 (NotificationController — `/api/notifications`) 🆕

#### N-1. 门店通知列表

```
GET /api/notifications/store?storeId={storeId}&page=1&size=30
```

#### N-2. 用户通知列表

```
GET /api/notifications/user?userId={userId}&page=1&size=30
```

#### N-3. 门店未读数量

```
GET /api/notifications/unread/store?storeId={storeId}
```

#### N-4. 用户未读数量

```
GET /api/notifications/unread/user?userId={userId}
```

#### N-5. 标记单条已读

```
POST /api/notifications/read
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| notificationId | Long | 是 | 通知 ID |

#### N-6. 门店全部已读

```
POST /api/notifications/read-all/store
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | Integer | 是 | 门店 ID |

#### N-7. 用户全部已读

```
POST /api/notifications/read-all/user
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户 ID |

---

### 模块 O：总部运营 (HqController — `/api/hq`，需 roleId=4) 🆕

#### O-1. 全部门店概览

```
GET /api/hq/stores-overview
```

**响应 data**：`StoresOverviewVO`

#### O-2. 触发每日统计刷新

```
POST /api/hq/stats/refresh
```

#### O-3. 活动列表（分页）

```
GET /api/hq/promotions?type={type}&isActive={bool}&page=1&pageSize=20
```

#### O-4. 创建活动

```
POST /api/hq/promotions
Body: CreatePromotionDTO
```

#### O-5. 编辑活动

```
PUT /api/hq/promotions/{promoId}
Body: CreatePromotionDTO
```

#### O-6. 启用/停用活动

```
PATCH /api/hq/promotions/{promoId}/toggle
```

#### O-7. 删除活动（软删除）

```
DELETE /api/hq/promotions/{promoId}
```

#### O-8. 给指定用户发券

```
POST /api/hq/coupons/send
Body: SendCouponDTO
```

#### O-9. 批量发券

```
POST /api/hq/coupons/batch-send
Body: BatchSendCouponDTO
```

---

### 模块 P：店长管理 (ManagerController — `/api/manager`) 🆕

#### P-1. 排班列表

```
GET /api/manager/schedules?storeId={storeId}
```

#### P-2. 班次列表

```
GET /api/manager/shifts
```

#### P-3. 异常记录

```
GET /api/manager/exceptions?storeId={storeId}
```

#### P-4. 审核异常

```
POST /api/manager/exception/review
```

#### P-5. 创建排班

```
POST /api/manager/schedule
```

#### P-6. 修改排班

```
PUT /api/manager/schedule/{scheduleId}
```

#### P-7. 搜索员工

```
GET /api/manager/staff?storeId={storeId}&keyword={keyword}
```

---

## 三、接口汇总速查表

| # | 方法 | 路径 | 模块 | 认证 |
|---|------|------|------|------|
| 1 | POST | `/api/auth/login` | 认证 | — |
| 2 | POST | `/api/auth/wx-login` | 认证 | — |
| 3 | POST | `/api/auth/send-code` | 认证 | — |
| 4 | POST | `/api/auth/register` | 认证 | — |
| 5 | POST | `/api/auth/login/phone` | 认证 | — |
| 6 | GET | `/api/stores` | 门店 | — |
| 7 | GET | `/api/tables` | 桌位 | — |
| 8 | GET | `/api/menu` | 菜单 | — |
| 9 | GET | `/api/orders` | 订单 | Token |
| 10 | POST | `/api/order/submit` | 订单 | Token |
| 11 | GET | `/api/order/detail` | 订单 | Token |
| 12 | POST | `/api/order/cancel` | 订单 | Token |
| 13 | POST | `/api/order/reschedule` | 订单 | Token |
| 14 | POST | `/api/order/refund` | 订单 | Token |
| 15 | POST | `/api/reservation/create` | 预约 | Token |
| 16 | POST | `/api/order/confirm-payment` | 订单 | Token |
| 17 | GET | `/api/reservation/current` | 预约 | Token |
| 18 | POST | `/api/order/reactivate` | 订单 | Token |
| 19 | GET | `/api/user/profile` | 用户 | Token |
| 20 | POST | `/api/user/realname` | 用户 | Token |
| 21 | POST | `/api/user/profile` | 用户 | Token |
| 22 | POST | `/api/user/change-password` | 用户 | Token |
| 23 | POST | `/api/user/upload-avatar` | 用户 | Token |
| 24 | GET | `/api/coupons` | 优惠券 | Token |
| 25 | GET | `/api/coupons/available` | 优惠券 | Token |
| 26 | GET | `/api/promotions/rules` | 促销 | — |
| 27 | POST | `/api/promotions/calculate` | 促销 | Token |
| 28 | GET | `/api/recommend` | AI推荐 | Token |
| 29 | GET | `/api/cats` | 猫咪 | — |
| 30 | GET | `/api/cats/detail` | 猫咪 | — |
| 31 | GET | `/api/queue/status` | 排队 | — |
| 32 | POST | `/api/queue/take` | 排队 | Token |
| 33 | POST | `/api/queue/call` | 排队 | 店员 |
| 34 | POST | `/api/queue/confirm` | 排队 | Token |
| 35 | GET | `/api/dashboard/metrics` | 看板 | Token |
| 36 | GET | `/api/staff/tables` | 店员 | 店员Token |
| 37 | GET | `/api/staff/alerts` | 店员 | 店员Token |
| 38 | GET | `/api/staff/orders` | 店员 | 店员Token |
| 39 | POST | `/api/staff/order/accept` | 店员 | 店员Token |
| 40 | POST | `/api/staff/table/dispatch` | 店员 | 店员Token |
| 41 | POST | `/api/staff/order/progress` | 店员 | 店员Token |
| 42 | GET | `/api/staff/refunds` | 店员 | 店员Token |
| 43 | POST | `/api/staff/refund/review` | 店员 | 店员Token |
| 44 | POST | `/api/staff/alert/acknowledge` | 店员 | 店员Token |
| 45 | POST | `/api/staff/alert/resolve` | 店员 | 店员Token |
| 46 | POST | `/api/staff/shift-exception/submit` | 店员 | Token |
| 47 | GET | `/api/staff/shift-exceptions/my` | 店员 | Token |
| 48 | POST | `/api/staff/cat/health` | 店员 | 猫咪管家 |
| 49 | POST | `/api/review/submit` | 评价 | Token |
| 50 | GET | `/api/review/detail` | 评价 | Token |
| 51-57 | — | `/api/notifications/*` | 通知 | Token |
| 58-66 | — | `/api/hq/*` | 总部 | roleId=4 |
| 67-73 | — | `/api/manager/*` | 店长 | Token |

---

## 四、本次同步变更摘要

与 2026-06-03 版（26 端点）相比，主要变化：

| 类别 | 变化 |
|------|------|
| **认证** | 新增 `/api/auth/wx-login`（微信快捷登录） |
| **用户** | 新增 `/api/user/profile`(POST 更新)、`/api/user/change-password`、`/api/user/upload-avatar` |
| **订单** | 新增 `/api/order/confirm-payment`、`/api/order/reactivate`、`/api/reservation/current` |
| **排队** | 新增 `/api/queue/call`（叫号）、`/api/queue/confirm`（确认叫号） |
| **评价** | 新增 `/api/review/detail` |
| **店员** | 新增 10 个接口（订单管理/桌位调度/退款审核/告警处置/考勤异常） |
| **通知** | 新增 7 个接口（通知中心完整 CRUD） |
| **总部** | 新增 9 个接口（门店概览/活动管理/发券） |
| **店长** | 新增 7 个接口（排班/员工/异常审核） |
| **userId 获取** | 原文档部分接口传 `userId` Query 参数，实际后端优先从 JWT Token 解析 |
