# NekoCafé 本地容器化部署手册

> 环境: Windows 11 + Docker Desktop 4.76.0
> 数据库/Redis 使用已有容器，仅容器化后端

---

## 一、产物文件

| 文件 | 用途 |
|---|---|
| `backend/Dockerfile` | 后端多阶段构建 (Maven编译 → JRE17运行) |
| `docker-compose.yml` | 仅启动后端容器，连已有数据库 |
| `.env` | 数据库/Redis 连接配置 |
| `Makefile` | 快捷命令 |

---

## 二、配置连接信息

编辑 `.env`，确保指向你已有的数据库容器：

```env
DB_HOST=host.docker.internal   # 如果数据库也在 Docker 里，用容器名或 host.docker.internal
DB_PORT=5432
DB_NAME=nekocafe
DB_USERNAME=admin_user
DB_PASSWORD=123456789

REDIS_HOST=host.docker.internal
REDIS_PORT=6379
REDIS_PASSWORD=123456789
```

> `host.docker.internal` 让容器能访问宿主机上的其他 Docker 容器暴露的端口。
> 如果数据库容器和本项目在同一个 docker-compose 网络里，也可以改成容器名。

---

## 三、一键启动

```bash
docker compose up -d --build
```

只启动后端容器，自动连接已有的 PostgreSQL + Redis。

---

## 四、常用命令

```bash
docker compose up -d --build   # 构建并启动
docker compose down             # 停止
docker compose restart          # 重启
docker compose logs -f          # 查看日志
docker compose ps               # 查看状态
```

---

## 五、验证

```
http://localhost:8081
```
