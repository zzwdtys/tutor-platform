FROM maven:3.8-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
# 云托管用 cloud 配置，数据库通过环境变量传入
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.profiles.active=cloud \
  --spring.datasource.url=${MYSQL_URL} \
  --spring.datasource.username=${MYSQL_USER} \
  --spring.datasource.password=${MYSQL_PASS} \
  --server.base-url=${BASE_URL}"]
