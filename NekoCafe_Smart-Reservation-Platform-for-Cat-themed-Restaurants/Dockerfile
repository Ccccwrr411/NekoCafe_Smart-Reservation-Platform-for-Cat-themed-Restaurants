# ============================================
# NekoCafé 后端 Dockerfile
# 用于 GitHub Actions 自动构建 Docker 镜像
# ============================================

# ---- 阶段1: Maven 构建 ----
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app
COPY backend/pom.xml .
COPY backend/settings.xml /root/.m2/settings.xml
RUN mvn dependency:go-offline -B || true

COPY backend/src ./src
RUN mvn clean package -Dmaven.test.skip=true -B

# ---- 阶段2: JRE 运行 ----
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="nekocafe-team"
LABEL description="NekoCafé 猫咖管理系统后端"

RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

WORKDIR /app
COPY --from=builder /app/target/nekocafe-server.jar app.jar
RUN mkdir -p /app/uploads

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/healthz || exit 1

ENV JAVA_OPTS="-Xms256m -Xmx768m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
