# 1. 빌드 단계 (Builder)
FROM eclipse-temurin:17-jre-alpine AS builder
WORKDIR /app

# 의존성 캐싱
# 소스 코드 전체를 복사하기 전에, 빌드 설정 파일만 먼저 복사함
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 실행 권한 부여
RUN chmod +x ./gradlew

# 라이브러리만 미리 다운로드 (소스 코드가 바뀌어도 이 단계는 캐싱됨)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 & 빌드
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# 2. 실행 단계 (Runner)
FROM openjdk:17-jdk-slim
WORKDIR /app

# 타임존 설정
ENV TZ=Asia/Seoul

# 빌드 결과물(jar)만 쏙 가져오기
COPY --from=builder /app/build/libs/*.jar app.jar

# 프로파일 및 실행 옵션 유연화
# 실행 시점에 환경변수나 옵션을 더 잘 먹게 하기 위한 설정
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]