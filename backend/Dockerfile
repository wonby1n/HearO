# 빌드 단계 (Gradle)
FROM krmp-d2hub-idock.9rum.cc/goorm/gradle:7.6.1-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

# 실행 단계 (가볍게)
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]