# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre

# 安装 Python + Playwright + 浏览器依赖
RUN apt-get update && apt-get install -y     python3 python3-pip python3-venv     libnss3 libnspr4 libatk1.0-0 libatk-bridge2.0-0 libcups2 libdrm2     libxkbcommon0 libxcomposite1 libxdamage1 libxfixes3 libxrandr2     libgbm1 libasound2t64 libpango-1.0-0 libcairo2 libatspi2.0-0 libxshmfence1     && python3 -m venv /opt/venv     && /opt/venv/bin/pip install --no-cache-dir playwright     && /opt/venv/bin/playwright install chromium     && apt-get clean     && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ENV PLAYWRIGHT_BROWSERS_PATH=/root/.cache/ms-playwright
ENV TZ=Asia/Shanghai
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
