
---

# 🧠 HEARO Backend

WebRTC 기반 상담 보조 시스템 백엔드 서버
(Spring Boot 3 + JPA + PostgreSQL + Redis + LiveKit)

---

## ✅ 실행 환경

* Java 17
* Spring Boot 3.5.x
* PostgreSQL
* Redis
* LiveKit Server (WebRTC)

---
## ⚠️ 주의사항

* `application.yaml`+`application-*.yaml`에 비밀값을 직접 작성하지 말 것
* dev 설정으로 운영 서버 실행 금지
* 운영 DB에서 `ddl-auto=update/create` 사용 금지

---
## ✅ 실행 전 필수 환경변수

본 프로젝트는 **환경변수 기반 설정 방식**을 사용합니다.
`application.yml`에는 민감한 값이 없으며, 모든 비밀 정보는 env로 주입해야 합니다.
다만 아무것도 없는 첫시작이라면 실행 프로필 정보, DB 관련 정보만 주입하면 되고, 
구동시 local8080에서 로그인 창이 뜬다면 log에 뜨는 security password를 붙여넣기 하시면 됩니다.
id : user , password : {log에 있는 password}
---

## 🔹 공통 (dev / prod 공통)

| 환경변수                     | 설명                       |
| ------------------------ | ------------------------ |
| `SPRING_PROFILES_ACTIVE` | 실행 프로필 (`dev` 또는 `prod`) |
| `DB_URL`                 | PostgreSQL JDBC URL      |
| `DB_USERNAME`            | DB 계정                    |
| `DB_PASSWORD`            | DB 비밀번호                  |
| `REDIS_HOST`             | Redis 서버 호스트             |
| `REDIS_PORT`             | Redis 포트 (보통 6379)       |
| `JWT_SECRET`             | JWT 서명용 시크릿 키            |
| `LIVEKIT_URL`            | LiveKit WebSocket 주소     |
| `LIVEKIT_API_KEY`        | LiveKit API Key          |
| `LIVEKIT_API_SECRET`     | LiveKit API Secret       |

---

## 🔹 개발 환경 (dev)

### 최소 설정 예시

```text
SPRING_PROFILES_ACTIVE=dev

DB_URL=jdbc:postgresql://localhost:5432/hearo
DB_USERNAME=postgres
DB_PASSWORD=1234

REDIS_HOST=localhost
REDIS_PORT=6379

JWT_SECRET=local-dev-secret

LIVEKIT_URL=ws://localhost:7880
LIVEKIT_API_KEY=xxxx
LIVEKIT_API_SECRET=yyyy
```

### 동작 특성

* JPA `ddl-auto = update`
* SQL 로그 출력
* Swagger ON
* 로컬 PostgreSQL / Redis 사용

---

## 🔹 운영 환경 (prod)

### 최소 설정 예시

```text
SPRING_PROFILES_ACTIVE=prod

DB_URL=jdbc:postgresql://<rds-host>:5432/hearo
DB_USERNAME=hearo
DB_PASSWORD=********

REDIS_HOST=<redis-host>
REDIS_PORT=6379
REDIS_PASSWORD=********

JWT_SECRET=********

LIVEKIT_URL=wss://<livekit-host>:7880
LIVEKIT_API_KEY=xxxx
LIVEKIT_API_SECRET=yyyy
```

### 동작 특성

* JPA `ddl-auto = validate` (스키마 변경 차단)
* SQL 로그 비활성화
* Swagger OFF
* 보안/성능 중심 설정

---

## ✅ IntelliJ에서 환경변수 설정 방법

1. 우측 상단 ▶ **Run / Edit Configurations**
2. Spring Boot Application 선택
3. **Environment variables** 클릭
4. 아래 형식으로 입력
- '+'버튼 눌러서 추가하면 됨

```text
SPRING_PROFILES_ACTIVE=dev;
DB_URL=jdbc:postgresql://localhost:5432/hearo;
DB_USERNAME=postgres;
DB_PASSWORD=1234;
REDIS_HOST=localhost;
REDIS_PORT=6379;
JWT_SECRET=local-dev-secret;
LIVEKIT_URL=ws://localhost:7880;
LIVEKIT_API_KEY=xxxx;
LIVEKIT_API_SECRET=yyyy
```

5. Apply → Run

👉 각 팀원은 자기 로컬 DB/Redis 환경에 맞게 값만 바꾸면 됨

---

## ✅ Redis 사용 목적

Redis는 다음 용도로 사용됩니다.

* JWT 블랙리스트 (로그아웃 토큰 차단)
* 상담 대기열 관리
* 상담원 상태 (온라인 / 통화중 등)
* 실시간 세션 상태 캐시

운영 환경에서는 반드시 인증 설정된 Redis 사용을 권장합니다.

---

## ✅ JPA 운영 정책

| 환경   | ddl-auto                      |
| ---- | ----------------------------- |
| dev  | `update` (엔티티 변경 시 테이블 자동 반영) |
| prod | `validate` (스키마 변경 차단)        |

운영 환경에서 테이블 변경은 **Flyway / Liquibase 기반 마이그레이션 방식 권장**.

---

## ✅ Swagger(OpenAPI)

| 환경   | 상태  |
| ---- | --- |
| dev  | ON  |
| prod | OFF |

운영 서버에서는 API 명세 노출 방지를 위해 비활성화됩니다.

---

## 📦 build.gradle 주요 의존성 설명

```gradle
implementation 'org.springframework.boot:spring-boot-starter-web'
```

* REST API 서버 (Controller, JSON 처리)

```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
```

* JPA + Hibernate + DB 연동

```gradle
runtimeOnly 'org.postgresql:postgresql'
```

* PostgreSQL JDBC 드라이버

```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
```

* JWT 인증, 인증 필터, 보안 설정

```gradle
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

* `@NotNull`, `@Email` 같은 DTO 유효성 검사

```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

* 헬스체크, 모니터링 엔드포인트 (/actuator/health 등)

```gradle
developmentOnly 'org.springframework.boot:spring-boot-devtools'
```

* 로컬 개발 시 자동 리로드

```gradle
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

* Getter/Setter/Builder 자동 생성

```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
```

* JUnit + Mockito 테스트 환경

```gradle
testImplementation 'org.springframework.security:spring-security-test'
```

* 인증/인가 테스트 지원

---

## 🚀 실행 순서 (개발 기준)

1. PostgreSQL 실행
2. Redis 실행

   ```bash
   docker run -d -p 6379:6379 redis:7
   ```
3. LiveKit 서버 실행
4. IntelliJ에서 env 설정 후 Spring Boot 실행

---


