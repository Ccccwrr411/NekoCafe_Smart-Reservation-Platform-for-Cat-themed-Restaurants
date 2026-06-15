# NekoCafe Spring Boot 集成 Prometheus + Grafana 监控文档

---

## 一、架构总览

```
Spring Boot :8081
    │  /actuator/prometheus  暴露指标
    ▼
Prometheus :9090  每15s采集一次指标
    │
    ▼
Grafana :3000  可视化仪表盘（模板 ID: 19004）
```

---

## 二、代码改动清单

### 2.1 `backend/pom.xml` — 添加依赖

在 `<dependencies>` 中添加：

```xml
<!-- Actuator 暴露监控端点 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Micrometer 桥接 Prometheus -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 2.2 `backend/src/main/resources/application.yaml` — 暴露端点

在文件末尾添加：

```yaml
# Actuator + Prometheus 监控端点
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: nekocafe
```

### 2.3 `docker-compose.yml` — 添加服务

在 `services:` 下追加 Prometheus 和 Grafana 两个服务，并在 `volumes:` 下追加对应数据卷：

```yaml
services:
  # ... 原有的 backend、db、redis ...

  prometheus:
    image: prom/prometheus:latest
    container_name: nekocafe-prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus

  grafana:
    image: grafana/grafana:latest
    container_name: nekocafe-grafana
    restart: unless-stopped
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_USER: admin
      GF_SECURITY_ADMIN_PASSWORD: admin
    volumes:
      - grafana_data:/var/lib/grafana

volumes:
  # ... 原有 volumes ...
  prometheus_data:
    driver: local
  grafana_data:
    driver: local
```

### 2.4 `prometheus.yml` — 新建 Prometheus 配置

项目根目录创建：

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'nekocafe-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['backend:8081']
```

> **注意**：`targets` 里用 docker-compose 服务名 `backend`，不是外网 IP。

---

## 三、部署步骤

### 3.1 拉取镜像并启动

```bash
docker compose pull
docker compose up -d
```

### 3.2 验证服务状态

| 地址 | 说明 |
|------|------|
| `http://localhost:8081/actuator/prometheus` | Spring Boot 指标端点，应返回大量文本数据 |
| `http://localhost:9090` | Prometheus 管理界面 |
| `http://localhost:3000` | Grafana 登录页 |

### 3.3 验证 Prometheus 采集

1. 打开 `http://localhost:9090`
2. 点击顶部 **Status** → **Targets**
3. 确认 `nekocafe-backend` 状态为 **绿色 UP**

---

## 四、Grafana 配置

### 4.1 登录

- 地址：`http://localhost:3000`
- 账号：`admin`
- 密码：`admin`

### 4.2 添加 Prometheus 数据源

1. 左侧菜单 → **Connections** → **Data sources**
2. 点右上角蓝色 **Add data source**
3. 选择 **Prometheus**
4. URL 填写：`http://prometheus:9090`
5. 点底部 **Save & test**，看到绿色 ✅ 提示即成功

### 4.3 导入仪表盘

> ⚠️ 模板 4701 需要 Loki 数据源（本项目中未配置），不可用。**推荐使用 19004**，仅需 Prometheus。

1. 左侧菜单 → **Dashboards** → 右上角 **New** → **Import**
2. 在输入框中填写：**`19004`**
3. 点 **Load**
4. 下拉选择之前添加的 Prometheus 数据源（如 `prometheus-1`）
5. 点 **Import**

### 4.4 可选：其他兼容模板

| 模板 ID | 说明 |
|---------|------|
| `19004` | ✅ 推荐，Spring Boot 3.x 专用，仅需 Prometheus |
| `17175` | Spring Boot 3.x 备选 |
| `12900` | 通用 JVM 监控，兼容性好 |
| `4701` | ❌ 不推荐，需要 Loki 数据源 |

---

## 五、面板展示内容

导入 19004 后可看到以下监控指标：

| 分类 | 指标 |
|------|------|
| **JVM 内存** | 堆内存使用量/最大值、非堆内存、各区使用占比 |
| **JVM 线程** | 活跃线程数、守护线程数、峰值线程数 |
| **GC** | Young GC / Full GC 次数与耗时 |
| **CPU** | 进程 CPU 使用率、系统负载 |
| **HTTP 请求** | QPS、P50/P95/P99 响应时间、请求量趋势 |
| **错误统计** | 4xx / 5xx 错误数量 |
| **数据库连接池** | HikariCP 活跃/空闲/等待连接数 |

---

## 六、常见问题排查

| 问题 | 原因 | 解决 |
|------|------|------|
| Grafana 面板 N/A / No data | 模板与 Spring Boot 版本不匹配 | 使用 19004 模板 |
| 导入提示缺少 Loki | 模板依赖 Loki 数据源 | 换用 19004 模板 |
| Prometheus Targets 红色 DOWN | backend 未正确暴露端点 | 检查 `application.yaml` 中 management 配置 |
| Prometheus 连接不上 backend | 网络不通 | `prometheus.yml` 中 targets 必须用服务名 `backend:8081` |

---

## 七、完整文件结构（仅监控相关）

```
项目根目录/
├── prometheus.yml              # Prometheus 采集配置
├── docker-compose.yml          # 新增 prometheus + grafana 服务
├── backend/
│   ├── pom.xml                 # 新增 2 个依赖
│   └── src/main/resources/
│       └── application.yaml    # 新增 management 配置块
└── docs/
    └── Prometheus-Grafana-监控集成文档.md  # 本文档
```

---

> 📅 创建日期：2026-06-15
> 📝 适用版本：Spring Boot 3.x + Prometheus + Grafana + Docker Compose
