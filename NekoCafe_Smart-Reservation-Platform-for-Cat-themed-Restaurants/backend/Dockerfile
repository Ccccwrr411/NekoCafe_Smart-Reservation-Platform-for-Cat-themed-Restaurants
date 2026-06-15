# ============================================
# NekoCafé 后端 Dockerfile — 多阶段构建
# 阶段1: Maven 构建  →  阶段2: JRE17 运行
# ============================================

# ---- 阶段1: 构建阶段 ----
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 先复制 pom.xml，利用 Docker 缓存层加速依赖下载
COPY pom.xml .
COPY settings.xml /root/.m2/settings.xml
RUN mvn dependency:go-offline -B || true

# 复制源码并构建
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true -B

# ---- 阶段2: 运行阶段 ----
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="nekocafe-team"
LABEL description="NekoCafé 猫咖管理系统后端服务"

RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

WORKDIR /app

# 从构建阶段复制 jar 包
COPY --from=builder /app/target/nekocafe-server.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploads

# 暴露端口（与 application.yaml 中 server.port 一致）
EXPOSE 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/healthz || exit 1

# JVM 参数 + 启动命令
ENV JAVA_OPTS="-Xms256m -Xmx768m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
