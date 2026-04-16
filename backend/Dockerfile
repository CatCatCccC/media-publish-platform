# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 先复制 pom.xml 预下载依赖（利用缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并打包
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ENV TZ=Asia/Shanghai
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
