# NekoCafé 猫咖智能预约平台 — 前后端接口契约

> 最后更新：2026-06-03
> 前端项目：NekoCafe_Smart Reservation Platform
> 文档用途：交给后端同学，按此文档实现所有接口

---

## 一、通用约定

### 1.1 Base URL

```
开发环境：http://172.20.10.3:8081
生产环境：待定（上线前替换）
```

### 1.2 认证方式

所有接口（除登录外）需在请求头携带 Token：

```
Authorization: Bearer <token>
```

Token 由登录接口返回，前端存储在 `wx.getStorageSync('token')`。

### 1.3 统一响应格式

所有接口返回 JSON，结构如下：

```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 0 = 成功，非 0 = 业务错误码 |
| message | string | 提示信息 |
| data | any | 业务数据（具体结构见各接口） |

**错误码约定**：

| code | 含义 |
|------|------|
| 0 | 成功 |
| 401 | 未登录 / Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 400 | 参数错误 |
| 500 | 服务器内部错误 |

### 1.4 Content-Type

请求体统一使用 `application/json`。

### 1.5 静态资源（图片）

所有图片由后端作为静态文件提供，接口返回的 `imageUrl` 字段均为完整 URL，前端直接放入 `<image>` 组件的 `src` 属性使用。

**图片存放规范**：后端需在静态资源目录下建立以下子目录，并按 ID 命名文件：

| 目录 | 路径模式 | 用途 | 数量 |
|------|----------|------|------|
| `uploads/stores/` | `store_{id}.jpg` | 门店封面图 | 5 |
| `uploads/menu/` | `item_{id}.jpg` | 菜品图片 | 16 |
| `uploads/cats/` | `cat_{id}.jpg` | 猫咪列表照片 | 5 |
| `uploads/cats/` | `cat_{id}_detail.jpg` | 猫咪详情大图 | 5 |
| `uploads/banners/` | `banner_{id}.jpg` | 首页轮播 Banner | 3 |
| `uploads/avatars/` | `default.png` | 用户默认头像 | 1 |

**图片规格建议**：

| 类型 | 建议尺寸 | 建议格式 | 建议大小 |
|------|----------|----------|----------|
| 门店封面 | 750×400 px | JPG/WebP | < 200 KB |
| 菜品图片 | 400×400 px | JPG/WebP | < 100 KB |
| 猫咪照片 | 400×400 px | JPG/WebP | < 150 KB |
| 猫咪详情 | 800×800 px | JPG/WebP | < 300 KB |
| Banner | 750×300 px | JPG/WebP | < 200 KB |
| 头像 | 200×200 px | PNG | < 50 KB |

---

## 二、接口清单

共 **26 个接口**，按业务模块分组。

---

### 模块 A：认证

#### A-1. 微信登录

```
POST /api/auth/login
```

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| code | body | string | 是 | wx.login() 返回的临时 code |

**请求示例**：

```json
{
  "code": "0a3xYzGa1b2cDeFgHiJkLmNoPqRsTuV"
}
```

**响应 data**：

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "userInfo": {
    "id": 1001,
    "nickName": "猫咖爱好者",
    "avatarUrl": "https://...",
    "phone": "138****8888",
    "memberLevel": "银卡会员",
    "points": 320
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| token | string | JWT Token，后续请求需携带 |
| userInfo.id | int | 用户 ID |
| userInfo.nickName | string | 微信昵称 |
| userInfo.avatarUrl | string | 微信头像 |
| userInfo.phone | string | 脱敏手机号 |
| userInfo.memberLevel | string | 会员等级 |
| userInfo.points | int | 当前积分 |

---

### 模块 B：门店

#### B-1. 门店列表

```
GET /api/stores
```

无参数。

**响应 data**（数组）：

```json
[
  {
    "id": 1,
    "name": "NekoCafé 朝阳店",
    "address": "朝阳区三里屯太古里南区 B1-01",
    "distance": 1.2,
    "lat": 39.9325,
    "lng": 116.4551,
    "avgPrice": 68,
    "rating": 4.8,
    "catCount": 12,
    "imageUrl": "https://...",
    "tags": ["环境好", "猫咪多", "适合拍照"],
    "openTime": "10:00 - 22:00",
    "status": "open"
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
| imageUrl | string | 门店封面图（后端静态资源 URL，见 1.5） |
| tags | string[] | 标签 |
| openTime | string | 营业时间 |
| status | string | open=营业中 / closed=休息中 |

---

### 模块 C：桌位

#### C-1. 桌位列表

```
GET /api/tables?storeId={storeId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | int | 是 | 门店 ID |

**响应 data**（数组）：

```json
[
  {
    "id": 101,
    "name": "A1",
    "type": "双人桌",
    "capacity": 2,
    "status": "available",
    "catType": "布偶猫",
    "catName": "奶油",
    "price": 0
  }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 桌位 ID |
| name | string | 桌号 |
| type | string | 桌型：双人桌/四人桌/包间/吧台位 |
| capacity | int | 容量（人数） |
| status | string | available=可预订 / booked=已预订 / maintenance=维护中 |
| catType | string | 对应猫咪品种 |
| catName | string | 对应猫咪名字 |
| price | int | 附加费用（元，包间通常有） |

---

### 模块 D：菜单

#### D-1. 菜品列表

```
GET /api/menu?storeId={storeId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | int | 是 | 门店 ID |

**响应 data**：

```json
{
  "categories": [
    { "id": 1, "name": "招牌饮品", "icon": "☕" }
  ],
  "items": [
    {
      "id": 201,
      "categoryId": 1,
      "name": "猫爪拿铁",
      "price": 38,
      "imageUrl": "https://...",
      "desc": "布偶猫爪造型，每天限量30杯",
      "sales": 328,
      "rating": 4.9,
      "isHot": true,
      "isNew": false
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| categories[].id | int | 分类 ID |
| categories[].name | string | 分类名 |
| categories[].icon | string | 分类图标 |
| items[].id | int | 菜品 ID |
| items[].categoryId | int | 所属分类 ID |
| items[].name | string | 菜品名称 |
| items[].price | int | 价格（元） |
| items[].imageUrl | string | 菜品图片（后端静态资源 URL，见 1.5） |
| items[].desc | string | 描述 |
| items[].sales | int | 累计销量 |
| items[].rating | float | 评分 |
| items[].isHot | bool | 是否热销 |
| items[].isNew | bool | 是否新品 |

---

### 模块 E：订单

#### E-1. 订单列表

```
GET /api/orders?userId={userId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | int | 是 | 用户 ID |

**响应 data**（数组）：

```json
[
  {
    "id": "ORD20260601001",
    "storeId": 1,
    "storeName": "NekoCafé 朝阳店",
    "tableId": 101,
    "tableName": "A1双人桌",
    "reserveDate": "2026-06-01",
    "reserveTime": "14:00",
    "duration": 2,
    "persons": 2,
    "status": "completed",
    "totalAmount": 146,
    "createTime": "2026-05-30 10:22:15",
    "items": [
      { "name": "猫爪拿铁", "qty": 2, "price": 38 }
    ],
    "hasReview": true
  }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 订单号 |
| storeId | int | 门店 ID |
| storeName | string | 门店名称 |
| tableId | int | 桌位 ID |
| tableName | string | 桌位名称 |
| reserveDate | string | 预约日期（YYYY-MM-DD） |
| reserveTime | string | 预约时间（HH:mm） |
| duration | int | 时长（小时） |
| persons | int | 人数 |
| status | string | pending=待支付 / confirmed=已确认 / completed=已完成 / cancelled=已取消 / refunding=退款中 / refunded=已退款 |
| totalAmount | int | 订单总额（元） |
| createTime | string | 创建时间 |
| items | array | 菜品明细 |
| items[].name | string | 菜品名 |
| items[].qty | int | 数量 |
| items[].price | int | 单价（元） |
| hasReview | bool | 是否已评价 |

#### E-2. 提交订单

```
POST /api/order/submit
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | int | 是 | 门店 ID |
| items | array | 是 | 购物车内容 |
| items[].menuId | int | 是 | 菜品 ID |
| items[].name | string | 是 | 菜品名 |
| items[].price | int | 是 | 单价 |
| items[].qty | int | 是 | 数量 |
| totalAmount | int | 是 | 原始总价（优惠前） |
| finalAmount | int | 是 | 实付金额（优惠后） |
| discount | int | 是 | 优惠金额 |
| couponIds | string[] | 否 | 使用的优惠券 ID 列表 |
| remark | string | 否 | 备注 |
| tableId | int | 否 | 关联桌位 ID（预约点单场景） |

**响应 data**：

```json
{
  "orderId": "ORD20260603003",
  "totalAmount": 146,
  "finalAmount": 126,
  "payInfo": {
    "timeStamp": "1717400000",
    "nonceStr": "abc123def456",
    "package": "prepay_id=wx1234567890abcdef",
    "signType": "RSA",
    "paySign": "base64_signature_string"
  }
}
```

> **注意**：`payInfo` 需调用微信支付统一下单接口获取真实参数，前端用此参数调起 `wx.requestPayment`。

#### E-3. 订单详情

```
GET /api/order/detail?orderId={orderId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | string | 是 | 订单号 |

**响应 data**：

```json
{
  "id": "ORD20260603001",
  "storeId": 1,
  "storeName": "NekoCafé 朝阳店",
  "tableId": 101,
  "tableName": "A1双人桌",
  "reserveDate": "2026-06-05",
  "reserveTime": "14:00",
  "duration": 2,
  "persons": 2,
  "status": "confirmed",
  "totalAmount": 146,
  "discountAmount": 20,
  "finalAmount": 126,
  "createTime": "2026-06-03 09:10:00",
  "payTime": "2026-06-03 09:10:30",
  "payType": "微信支付",
  "remark": "靠窗位置",
  "items": [
    { "name": "猫爪拿铁", "qty": 2, "price": 38 }
  ],
  "timeline": [
    { "time": "2026-06-03 09:10:00", "title": "订单创建", "desc": "用户提交预约" },
    { "time": "2026-06-03 09:10:30", "title": "支付成功", "desc": "微信支付 ¥126.00" }
  ],
  "canCancel": true,
  "canReschedule": true,
  "canRefund": true,
  "hasReview": false
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| discountAmount | int | 优惠金额 |
| finalAmount | int | 实付金额 |
| payTime | string | 支付时间 |
| payType | string | 支付方式 |
| remark | string | 备注 |
| timeline | array | 订单流转时间线 |
| canCancel | bool | 是否可取消 |
| canReschedule | bool | 是否可改约 |
| canRefund | bool | 是否可退款 |
| hasReview | bool | 是否已评价 |

#### E-4. 取消订单

```
POST /api/order/cancel
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | string | 是 | 订单号 |

**响应 data**：

```json
{
  "status": "cancelled",
  "refundAmount": 126
}
```

#### E-5. 改约

```
POST /api/order/reschedule
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | string | 是 | 订单号 |
| newReserveDate | string | 是 | 新预约日期 |
| newReserveTime | string | 是 | 新预约时间 |

**响应 data**：

```json
{
  "orderId": "ORD20260603001",
  "newReserveDate": "2026-06-06",
  "newReserveTime": "15:00"
}
```

#### E-6. 申请退款

```
POST /api/order/refund
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | string | 是 | 订单号 |

**响应 data**：

```json
{
  "refundId": "REF20260603001",
  "refundAmount": 126,
  "status": "processing"
}
```

#### E-7. 提交预约（纯预约，无点单）

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

**响应 data**：

```json
{
  "orderId": "ORD20260603002",
  "status": "confirmed"
}
```

---

### 模块 F：用户

#### F-1. 用户信息

```
GET /api/user/profile
```

无参数（后端从 Token 解析 userId）。

**响应 data**：

```json
{
  "id": 1001,
  "nickName": "猫咖爱好者",
  "avatarUrl": "https://...",
  "phone": "138****8888",
  "memberLevel": "银卡会员",
  "memberLevelIcon": "🥈",
  "points": 320,
  "pointsToNext": 680,
  "nextLevel": "金卡会员",
  "totalOrders": 12,
  "totalSpent": 896,
  "couponCount": 3,
  "favoriteStores": [1, 4],
  "joinDate": "2025-09-01"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 用户 ID |
| nickName | string | 昵称 |
| avatarUrl | string | 头像 URL（后端静态资源，见 1.5） |
| phone | string | 脱敏手机号 |
| memberLevel | string | 会员等级 |
| memberLevelIcon | string | 等级图标 |
| points | int | 当前积分 |
| pointsToNext | int | 距下一级还需积分 |
| nextLevel | string | 下一级名称 |
| totalOrders | int | 累计订单数 |
| totalSpent | int | 累计消费（元） |
| couponCount | int | 可用优惠券数量 |
| favoriteStores | int[] | 收藏门店 ID |
| joinDate | string | 注册日期 |

#### F-2. 实名认证

```
POST /api/user/realname
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| realName | string | 是 | 真实姓名 |
| idCard | string | 是 | 身份证号 |

**响应 data**：

```json
{
  "verified": true,
  "realName": "张三",
  "idCardMask": "110101********1234"
}
```

---

### 模块 G：优惠券与促销

#### G-1. 优惠券列表（我的优惠券）

```
GET /api/coupons?userId={userId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | int | 是 | 用户 ID |

**响应 data**（数组）：

```json
[
  {
    "id": "CPN001",
    "name": "新人专享 8折券",
    "type": "discount",
    "value": 0.8,
    "maxDiscount": 20,
    "minAmount": 50,
    "expireDate": "2026-07-31",
    "status": "unused",
    "stackable": true,
    "ruleId": "RULE_DISCOUNT_20"
  }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 优惠券 ID |
| name | string | 优惠券名称 |
| type | string | discount=折扣券 / cashback=满减券 / freebie=赠品券 |
| value | float | 面值（折扣券填折扣率如 0.8，满减券填金额，赠品券填等值金额） |
| maxDiscount | int | 折扣券最大减免额（元） |
| minAmount | int | 最低消费门槛（元） |
| expireDate | string | 过期日期 |
| status | string | unused=未使用 / used=已使用 / expired=已过期 |
| stackable | bool | 是否可叠加 |
| ruleId | string | 关联规则 ID |

#### G-2. 可用优惠券（下单时获取）

```
GET /api/coupons/available?storeId={storeId}&amount={amount}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | int | 是 | 门店 ID |
| amount | int | 是 | 订单原始总额（元） |

**响应 data**：结构同 G-1，额外增加 `saving` 字段表示预估节省金额。

#### G-3. 促销活动规则

```
GET /api/promotions/rules
```

无参数。

**响应 data**：

```json
{
  "activePromotions": [
    {
      "id": "PROMO001",
      "name": "工作日满减",
      "type": "cashback",
      "rule": "满150减15",
      "desc": "周一至周五可用，可与折扣券叠加",
      "minAmount": 150,
      "value": 15,
      "stackable": true
    }
  ],
  "stackingRules": {
    "enabled": true,
    "maxStackCount": 2,
    "rules": [
      "平台满减活动 + 优惠券可叠加使用",
      "两个满减类优惠不可叠加，系统自动选最优",
      "折扣券与满减券可叠加，折扣券先计算",
      "赠品券不可与其他优惠叠加"
    ]
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| activePromotions | array | 当前生效的促销活动 |
| activePromotions[].id | string | 活动 ID |
| activePromotions[].type | string | cashback=满减 |
| activePromotions[].rule | string | 规则描述文本 |
| activePromotions[].desc | string | 详细说明 |
| activePromotions[].minAmount | int | 满减门槛（元） |
| activePromotions[].value | int | 减免金额（元） |
| activePromotions[].stackable | bool | 是否可叠加 |
| stackingRules.enabled | bool | 是否启用叠加 |
| stackingRules.maxStackCount | int | 最大叠加数 |
| stackingRules.rules | string[] | 叠加规则说明 |

#### G-4. 优惠计算（可选：前端本地计算或调后端）

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
  "originalAmount": 146,
  "appliedPromotions": [
    { "name": "新人专享 8折券", "type": "discount", "saved": 20 }
  ],
  "totalDiscount": 20,
  "finalAmount": 126,
  "breakdown": [
    { "label": "商品原价", "amount": 146 },
    { "label": "新人专享 8折券", "amount": -20, "type": "discount" },
    { "label": "平台满减（未满足门槛）", "amount": 0, "type": "skipped" }
  ]
}
```

> **当前前端状态**：优惠计算在前端 `order.js` 的 `calcDiscount()` 纯函数中本地完成，未调用此后端接口。建议后端实现此接口后，前端改为调用后端计算，以确保优惠逻辑一致、防止前端篡改。

---

### 模块 H：AI 推荐

#### H-1. 个性化推荐

```
GET /api/recommend?userId={userId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | int | 是 | 用户 ID |

**响应 data**：

```json
{
  "reason": "根据您的偏好和猫咪性格匹配，为您推荐",
  "tables": [
    {
      "id": 101,
      "name": "A1",
      "type": "双人桌",
      "catName": "奶油",
      "catBreed": "布偶猫",
      "catPersonality": ["粘人", "爱睡觉", "不抓人"],
      "matchScore": 98,
      "matchReason": "您偏好温顺粘人的猫咪，奶油性格完美匹配"
    }
  ],
  "dishes": [
    {
      "id": 201,
      "name": "猫爪拿铁",
      "price": 38,
      "imageUrl": "https://...",
      "reason": "您的最爱，累计点了6次",
      "isHot": true
    }
  ],
  "userProfile": {
    "favoriteBreeds": ["布偶猫", "英短"],
    "favoritePersonalities": ["粘人", "温柔"],
    "tastePreference": ["咖啡", "甜品"],
    "visitFrequency": "每周1-2次"
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| reason | string | 推荐理由概述 |
| tables | array | 推荐桌位（按猫咪匹配度） |
| tables[].matchScore | int | 匹配度（0-100） |
| tables[].matchReason | string | 匹配理由 |
| dishes | array | 推荐菜品 |
| dishes[].reason | string | 推荐理由 |
| userProfile | object | 用户画像（可选，展示用） |

---

### 模块 I：猫咪档案

#### I-1. 猫咪列表

```
GET /api/cats?storeId={storeId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | int | 是 | 门店 ID |

**响应 data**（数组）：

```json
[
  {
    "id": 1,
    "name": "奶油",
    "breed": "布偶猫",
    "age": 2,
    "gender": "母",
    "imageUrl": "https://...",
    "weight": 4.2,
    "desc": "温顺爱撒娇，最受欢迎的网红猫",
    "personality": ["粘人", "爱睡觉", "不抓人"],
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
| gender | string | 性别：公/母 |
| imageUrl | string | 猫咪照片（后端静态资源 URL，见 1.5） |
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
| catId | int | 是 | 猫咪 ID |

**响应 data**：

```json
{
  "id": 1,
  "name": "奶油",
  "breed": "布偶猫",
  "age": 2,
  "gender": "母",
  "imageUrl": "https://...",
  "desc": "温顺爱撒娇，最受欢迎的网红猫...",
  "personality": ["粘人", "爱睡觉", "不抓人"],
  "currentWeight": 4.2,
  "idealWeight": { "min": 3.5, "max": 5.0 },
  "weightHistory": {
    "labels": ["1月", "2月", "3月", "4月", "5月", "6月"],
    "values": [3.9, 4.0, 4.1, 4.3, 4.2, 4.2]
  },
  "vaccines": [
    { "name": "猫三联", "date": "2025-12-15", "nextDue": "2026-12-15", "status": "valid" }
  ],
  "interactions": [
    { "date": "06-03", "type": "客人撸猫", "desc": "被客人抱了30分钟", "mood": "happy" }
  ],
  "healthScore": 95,
  "healthAdvice": "奶油目前整体健康状态良好。建议下月安排一次体检。"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| currentWeight | float | 当前体重 |
| idealWeight | object | 理想体重范围 |
| weightHistory | object | 体重变化记录（近6月） |
| vaccines | array | 疫苗记录 |
| vaccines[].status | string | valid=有效 / expiring=即将过期 / expired=已过期 |
| interactions | array | 近期互动记录 |
| interactions[].mood | string | happy/neutral/grumpy |
| healthScore | int | 健康评分（0-100） |
| healthAdvice | string | 健康建议 |

---

### 模块 J：排队

#### J-1. 排队状态

```
GET /api/queue/status?storeId={storeId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | int | 是 | 门店 ID |

**响应 data**：

```json
{
  "storeId": 1,
  "waitingCount": 4,
  "avgWaitMinutes": 15,
  "currentNumber": 12,
  "myNumber": null,
  "myWaitMinutes": 0,
  "queueList": [
    { "number": 13, "persons": 2, "type": "双人桌", "ahead": 0 }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| waitingCount | int | 当前等待人数 |
| avgWaitMinutes | int | 平均等待时间（分钟） |
| currentNumber | int | 当前叫号 |
| myNumber | int|null | 我的号码（null=未取号） |
| myWaitMinutes | int | 我的预计等待时间 |
| queueList | array | 排队列表 |
| queueList[].number | int | 号码 |
| queueList[].persons | int | 人数 |
| queueList[].type | string | 桌型 |
| queueList[].ahead | int | 前方等待人数 |

#### J-2. 取号

```
POST /api/queue/take
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | int | 是 | 门店 ID |
| persons | int | 是 | 人数 |
| type | string | 是 | 桌型（如"双人桌"） |

**响应 data**：

```json
{
  "number": 17,
  "persons": 2,
  "type": "双人桌",
  "ahead": 4,
  "estWaitMinutes": 20
}
```

---

### 模块 K：数据看板

#### K-1. 运营指标

```
GET /api/dashboard/metrics?storeId={storeId}&range={range}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | int | 是 | 门店 ID |
| range | string | 是 | 时间范围：7d / 30d / 90d |

**响应 data**：

```json
{
  "storeId": 1,
  "range": "7d",
  "spaceEfficiency": {
    "labels": ["05-28", "05-29", "05-30", "05-31", "06-01", "06-02", "06-03"],
    "values": [85, 92, 78, 105, 120, 98, 88]
  },
  "turnoverRate": {
    "labels": ["05-28", "05-29", "05-30", "05-31", "06-01", "06-02", "06-03"],
    "values": [2.1, 2.4, 1.8, 3.0, 3.5, 2.8, 2.3]
  },
  "repurchaseRate": {
    "labels": ["普通会员", "银卡会员", "金卡会员", "黑卡会员"],
    "values": [28, 45, 62, 78]
  },
  "todayOverview": {
    "revenue": 8650,
    "orderCount": 42,
    "newMembers": 5,
    "avgOrderValue": 206
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| spaceEfficiency | object | 坪效趋势（labels=日期, values=元/m²/天） |
| turnoverRate | object | 翻台率趋势（labels=日期, values=翻台次数） |
| repurchaseRate | object | 会员复购率（labels=会员等级, values=百分比） |
| todayOverview.revenue | int | 今日营收（元） |
| todayOverview.orderCount | int | 今日订单数 |
| todayOverview.newMembers | int | 今日新增会员 |
| todayOverview.avgOrderValue | int | 今日客单价（元） |

---

### 模块 L：店员后台

#### L-1. 桌位状态

```
GET /api/staff/tables?storeId={storeId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | int | 是 | 门店 ID |

**响应 data**（数组）：

```json
[
  {
    "id": 101,
    "name": "A1",
    "type": "双人桌",
    "status": "occupied",
    "customer": "猫咖爱好者",
    "arriveTime": "14:00",
    "estLeaveTime": "16:00",
    "catType": "布偶猫"
  }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| status | string | available=空闲 / occupied=使用中 / booked=已预约 / cleaning=打扫中 / maintenance=维护中 |
| customer | string | 当前顾客（无则"-"） |
| arriveTime | string | 到店时间 |
| estLeaveTime | string | 预计离店时间 |

#### L-2. 异常告警

```
GET /api/staff/alerts?storeId={storeId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeId | int | 是 | 门店 ID |

**响应 data**（数组）：

```json
[
  {
    "id": "ALT001",
    "level": "warning",
    "type": "overstay",
    "title": "B2 超时未离店",
    "desc": "橘猫爱好者已超预约时长30分钟",
    "time": "16:00"
  }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 告警 ID |
| level | string | critical=严重 / warning=警告 / info=提示 |
| type | string | overstay=超时 / no_show=未到 / equipment=设备 / low_stock=库存 |
| title | string | 告警标题 |
| desc | string | 告警描述 |
| time | string | 告警时间 |

---

### 模块 M：评价

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

**响应 data**：

```json
{
  "reviewId": "REV20260603001",
  "status": "published",
  "pointsEarned": 10
}
```

---

## 三、接口汇总速查表

| # | 方法 | 路径 | 模块 | 说明 |
|---|------|------|------|------|
| 1 | POST | `/api/auth/login` | 认证 | 微信登录 |
| 2 | GET | `/api/stores` | 门店 | 门店列表 |
| 3 | GET | `/api/tables` | 桌位 | 桌位列表 |
| 4 | GET | `/api/menu` | 菜单 | 菜品列表 |
| 5 | GET | `/api/orders` | 订单 | 订单列表 |
| 6 | POST | `/api/order/submit` | 订单 | 提交订单 |
| 7 | GET | `/api/order/detail` | 订单 | 订单详情 |
| 8 | POST | `/api/order/cancel` | 订单 | 取消订单 |
| 9 | POST | `/api/order/reschedule` | 订单 | 改约 |
| 10 | POST | `/api/order/refund` | 订单 | 申请退款 |
| 11 | POST | `/api/reservation/create` | 预约 | 提交预约 |
| 12 | GET | `/api/user/profile` | 用户 | 用户信息 |
| 13 | POST | `/api/user/realname` | 用户 | 实名认证 |
| 14 | GET | `/api/coupons` | 优惠券 | 我的优惠券 |
| 15 | GET | `/api/coupons/available` | 优惠券 | 可用优惠券 |
| 16 | GET | `/api/promotions/rules` | 促销 | 活动规则 |
| 17 | POST | `/api/promotions/calculate` | 促销 | 优惠试算 |
| 18 | GET | `/api/recommend` | AI | 个性化推荐 |
| 19 | GET | `/api/cats` | 猫咪 | 猫咪列表 |
| 20 | GET | `/api/cats/detail` | 猫咪 | 猫咪详情 |
| 21 | GET | `/api/queue/status` | 排队 | 排队状态 |
| 22 | POST | `/api/queue/take` | 排队 | 取号 |
| 23 | GET | `/api/dashboard/metrics` | 数据 | 运营看板 |
| 24 | GET | `/api/staff/tables` | 店员 | 桌位管理 |
| 25 | GET | `/api/staff/alerts` | 店员 | 异常告警 |
| 26 | POST | `/api/review/submit` | 评价 | 提交评价 |

- **GET 接口**：17 个
- **POST 接口**：9 个
- **PUT/DELETE**：0 个

---

## 四、后端实现注意事项

1. **认证拦截**：除 `/api/auth/login` 外，所有接口需校验 Token 有效性。
2. **userId 获取**：`/api/user/profile` 等接口的 userId 应从 Token 中解析，不应信任前端传入。
3. **优惠计算**：建议后端实现 `/api/promotions/calculate` 后，前端改为调后端计算（当前前端本地算），防止客户端篡改优惠金额。
4. **微信支付**：`/api/order/submit` 的 `payInfo` 需调微信支付「统一下单」接口获取真实签名参数。
5. **实名认证**：建议接入身份证实名验证服务（如阿里云盾、腾讯云实名认证）。
6. **排队系统**：需考虑 WebSocket 或轮询机制实现实时排队状态更新。
7. **数据看板**：坪效、翻台率、复购率等指标可按天预计算存入缓存，避免实时聚合。
8. **时间格式**：日期统一 `YYYY-MM-DD`，时间统一 `HH:mm`，日期时间统一 `YYYY-MM-DD HH:mm:ss`。
9. **静态图片资源**：详见 1.5 节。需在服务器 `uploads/` 目录下准备 35 张图片，前端 imageUrl 均指向此路径。开发环境图片根路径为 `http://172.20.10.3:8081/uploads/`。
