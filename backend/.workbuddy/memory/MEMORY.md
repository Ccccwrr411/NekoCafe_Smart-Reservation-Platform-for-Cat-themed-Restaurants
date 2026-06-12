# NekoCafé 项目长期记忆

## 项目概况
- **项目名**：NekoCafé 猫咖智能预约平台（软件工程课设）
- **后端技术栈**：Spring Boot 3.5.14 + MyBatis + PostgreSQL + Redis + JWT
- **数据库地址**：82.157.130.254:5432/nekocafe
- **端口**：8081
- **包名前缀**：cn.edu.bjfu.nekocafe

## 框架现状（2026-06-04）
- 框架骨架已搭建完整，所有 Controller / Service / ServiceImpl 均为空壳
- ServiceImpl 中有详细 TODO 注释和实现要点
- JwtUtil / AuthInterceptor 尚未实现（最优先完成）
- 数据库实体（entity）和 Mapper 均由 MyBatis Generator 生成，已存在

## 关键设计约定
- orderId 格式：`"ORD" + reservationId`（补零到10位）
- durationMin（分钟）→ duration（小时）：除以60
- userId 来源：所有需要认证的接口从 `request.getAttribute("userId")` 获取（由拦截器注入）
- MemberExt.level 等级映射：1→普通会员, 2→银卡会员, 3→金卡会员, 4→黑卡会员
- Stores.status 映射：1→"open", 0→"closed"
- 图片 URL：直接从数据库字段读取完整 OSS URL（如 `https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/...`），**禁止后端拼接路径**；stores.image_url / dishes.image_url / cat_profiles.avatar_url / users.avatar_url；涉及 Redis 缓存的图片数据需注意缓存 key 版本管理

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
