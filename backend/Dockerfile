# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ENV TZ=Asia/Shanghai
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
