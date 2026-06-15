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
- JDK 17 路径：`C:\Users\刘璐\.jdks\ms-17.0.19`
- Maven 路径：`C:\Users\刘璐\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd`
- 编译命令：`JAVA_HOME="C:/Users/刘璐/.jdks/ms-17.0.19" mvn.cmd compile -q`（在 backend 目录下）

## M1 用户与会员模块状态
- 微信一键登录：已完成（含 user_roles 写入）
- 手机号+密码注册：已完成（验证码存 Redis，沙箱模式直接返回给前端；注册页有角色选择 UI，写 user_roles 表）
- 手机号密码登录：已完成（BCrypt 校验）
- 实名认证：后端+前端均已完成
- 会员等级/积分：已完成
- 密码加密：jBCrypt 0.4（pom.xml 已添加）

## 角色与权限体系（user_roles 表）
- `roles` 表：1=顾客, 2=店员, 3=店长, 4=总部运营, 5=猫咪管家
- `user_roles` 表结构：(user_id, role_id) 复合主键 + store_id 可选字段
- 注册时必须写 user_roles 表（之前遗漏已修复）
- 非顾客角色默认分配 storeId=1（课设兜底），顾客 storeId 为 null
- 前端注册页和登录页都有 5 角色选择 UI

## 店员工作台开发状态
- **P0 已完成**：枚举 Bug 修复、GET/POST 接口补齐（orders/accept/dispatch）、前端 API 对接、全量枚举 CAST 修复
- **P1 已完成**：POST /api/staff/order/progress（状态推进 CONFIRMED→MAKING→SERVING→COMPLETED）、GET /api/staff/refunds、POST /api/staff/refund/review、前端退款审核 Tab、V003 迁移（CLEANING 枚举）
- **P2 已完成**：通知中心系统（M-1~M-7 接口 + 底部导航改造 + 前端通知 Tab）
- **P3 待完成**：恢复 /api/staff/** 认证与角色校验、FR23 操作日志 AOP
