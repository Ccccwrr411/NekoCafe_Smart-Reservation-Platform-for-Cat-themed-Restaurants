# NekoCafe 项目长期记忆

## 项目概况
- 毕业设计项目：NekoCafe 智慧餐饮预约平台（猫咪主题餐厅）
- 技术栈：Java / Spring Boot / MyBatis / PostgreSQL / Redis / 微信小程序
- 后端包名：`cn.edu.bjfu.nekocafe`
- 后端端口：8081

## 数据库关键信息
- 数据库：PostgreSQL，连接 `82.157.130.254:5432/postgres`
- **枚举类型定义（大写）**：
  - `reservation_status`: BOOKED, CONFIRMED, MAKING, SERVING, COMPLETED, CANCEL_BOOKING, CANCEL_ORDER, REFUNDING
  - `refund_status_enum`: REQUEST_CANCEL, REQUEST_REFUND, REJECTED, COMPLETED
  - `status_enum`（桌台 table_status）: IDLE, RESERVED, OCCUPIED, CLEANING
  - `usage_status_enum`: ACTIVE, USED
- **非枚举 varchar 字段**：`payments.status`、`queue.status` 是普通 varchar，大小写不限
- `users.phone` 字段是 varchar(20)，`users.openid` 无长度限制
- MyBatis Example 的 WHERE 子句无法自动处理 PostgreSQL 枚举与 varchar 的类型转换，需写专用 SQL + `::枚举类型名` 显式 CAST
- MyBatis 的 INSERT/UPDATE 同样需要 CAST：所有枚举列赋值改为 `#{param}::枚举类型名`，不能只用 `jdbcType=VARCHAR`

## 用户偏好
- 后端开发者，习惯 Java 生态
- 偏好结构化表格展示技术信息
- 时间字段偏好 `java.util.Date`
- 开发时序：修复代码错误 → 编译 → 按顺序测试 API

## 编译环境
- JDK 17 路径：`/c/Users/lsf36/.jdks/ms-17.0.19`
- 编译命令：`export JAVA_HOME="/c/Users/lsf36/.jdks/ms-17.0.19" && cd backend && mvn compile -q`

## 店员工作台开发状态
- **P0 已完成**：枚举 Bug 修复、GET/POST 接口补齐（orders/accept/dispatch）、前端 API 对接、全量枚举 CAST 修复
- **P1 已完成**：POST /api/staff/order/progress（状态推进 CONFIRMED→MAKING→SERVING→COMPLETED）、GET /api/staff/refunds、POST /api/staff/refund/review、前端退款审核 Tab、V003 迁移（CLEANING 枚举）
- **P2 已完成**：通知中心系统（M-1~M-7 接口 + 底部导航改造 + 前端通知 Tab）
- **P3 待完成**：恢复 /api/staff/** 认证与角色校验、FR23 操作日志 AOP
