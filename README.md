# 项目名 ：NekoCafé 智慧餐饮预约平台____________

> 为猫主题咖啡馆打造的微信小程序智能预约与管理系统，集预约、排队、点单、AI 推荐、营销活动于一体。
【系统架构图】D-12_项目源代码仓库\docs\images\0003_系统架构图（新）.png

>项目目录结构
NekoCafe/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/cn/edu/bjfu/nekocafe/
│   │   ├── annotation/         # 自定义注解
│   │   ├── aspect/             # AOP 切面
│   │   ├── common/             # 通用类
│   │   ├── config/             # 配置类
│   │   ├── controller/         # 控制器 (16个)
│   │   ├── dto/                # 数据传输对象
│   │   ├── entity/             # 实体类
│   │   ├── exception/          # 异常处理
│   │   ├── interceptor/        # 拦截器
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── mq/                 # 消息队列
│   │   ├── service/            # 业务逻辑
│   │   ├── util/               # 工具类
│   │   └── vo/                 # 视图对象
│   ├── src/main/resources/     # 配置文件
│   ├── pom.xml                 # Maven 依赖
│   └── Dockerfile              # 后端镜像构建
├── frontend/                   # 微信小程序前端
│   ├── pages/                  # 24 个页面模块
│   ├── components/             # 公共组件
│   ├── libs/                   # 第三方库 (腾讯地图 SDK)
│   ├── assets/                 # 图标 & 静态资源
│   ├── app.js / app.json       # 小程序入口
│   └── project.config.json     # 小程序配置
├── db/
│   ├── migrations/             # Flyway 迁移脚本 (6个)
│   ├── fixes/                  # 数据修复脚本
│   └── test_data/              # 测试数据
├── docs/
│   ├── adr/                    # 架构决策记录
│   └── images/                 # 架构图 & 截图
├── tests/                      # 性能测试脚本 (k6)
├── docker-compose.yml          # Docker Compose 编排
├── prometheus.yml              # Prometheus 采集配置
├── Makefile                    # 常用命令快捷方式
└── README.md                   # 本文件



## 前置依赖

- Docker Desktop 24+
- 微信开发者工具 （仅前端开发需要）


## 一键启动
1. 克隆项目

git clone https://github.com/Ccccwrr411/NekoCafe_Smart-Reservation-Platform-for-Cat-themed-Restaurants.git
cd NekoCafe_Smart-Reservation-Platform-for-Cat-themed-Restaurants

2. 拉取镜像
docker pull yuuui2026/nekocafe:latest

3. 一键启动
docker compose up -d

4. 验证
curl http://localhost:8081/actuator/health     # 预期返回：{"status":"UP"}

5. 打开小程序前端：下载微信开发者工具，导入项目根目录下的 frontend/ 文件夹，在 app.js 的 globalData.baseUrl 中配置后端地址（本地开发使用 http://127.0.0.1:8081）。在开发者工具“详情 → 本地设置”中勾选“不校验合法域名”，点击“编译”即可在模拟器中预览。


## 团队

- 组长：_蓝舒芬_____15879711237@qq.com
