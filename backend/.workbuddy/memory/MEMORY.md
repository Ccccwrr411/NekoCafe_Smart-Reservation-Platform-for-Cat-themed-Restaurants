# NekoCafé 项目长期记忆

## 项目概况
- **项目名**：NekoCafé 猫咖智能预约平台（软件工程课设）
- **后端技术栈**：Spring Boot 3.5.14 + MyBatis + PostgreSQL + Redis + JWT
- **数据库地址**：82.157.130.254:5432/nekocafe
- **端口**：8081
- **包名前缀**：cn.edu.bjfu.nekocafe

## 框架现状（2026-06-10）
- 框架骨架已搭建完整，所有 Controller 已存在
- 数据库实体（entity）和 Mapper 均由 MyBatis Generator 生成
- **已完成 ServiceImpl**：A（Auth+User）、D（Coupon+Cat+Review）
- **待完成 ServiceImpl**：B（Store+Table+Menu）、C（Order）、E（Queue+Staff+Recommend）

## 已知 DB 缺陷（需要改表结构）
- `cat_profiles` 表无 `store_id` 字段 → listCats 无法按门店筛选（代码已加 TODO）
- `reservations` 表无 `has_review` 字段 → 用 reviews 表 countByExample 反查替代
- ~~`users` 表无 email/openid 字段~~ → **已修复（2026-06-11）**：DB 已加 email/openid，后端已同步适配

## users 表认证字段说明（2026-06-11 更新）
- **openid**：存微信 openid（课设版用 wx.login() 的 code 代替）
- **email**：用户绑定邮箱（可为 null）
- **phone**：真实手机号（可为 null，不再存 code）
- 登录查询：`andOpenidEqualTo(code)`，注册时 `user.setOpenid(code)`

## 关键设计约定
- orderId 格式：`"ORD" + reservationId`（补零到10位）
- durationMin（分钟）→ duration（小时）：除以60
- userId 来源：所有需要认证的接口从 `request.getAttribute("userId")` 获取（由拦截器注入）
- MemberExt.level 等级映射：1→普通会员, 2→银卡会员, 3→金卡会员, 4→黑卡会员
- Stores.status 映射：1→"open", 0→"closed"
- 图片 URL 格式：`http://host/uploads/{type}/{filename}`

## 接口分组
- A: 认证（1个）
- B: 门店（1个）  
- C: 桌位（1个）
- D: 菜单（1个）
- E: 订单/预约（7个）
- F: 用户（2个）
- G: 优惠券/促销（4个）
- H: AI推荐（1个）
- I: 猫咪档案（2个）
- J: 排队（2个）
- K/L: 看板/店员（3个）
- M: 评价（1个）
