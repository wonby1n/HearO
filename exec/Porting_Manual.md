# 포팅매뉴얼

## 1. Gitlab 소스 클론 이후 빌드 및 배포할 수 있도록 정리한 문서

## 프로젝트 개요

- 프로젝트 이름: HearO
- 팀명: 인사불성
- 한 줄 소개: AI 기반 상담원 보호 및 업무 지원 통화 서비스
- 배포 주소: [http://i14e106.p.ssafy.io/](http://i14e106.p.ssafy.io/)

## 배포 시나리오

### Phase 1. 사전 환경 구성 (Pre-requisites)

**1. Docker & Docker Compose 설치**

```bash
# 1. 패키지 업데이트 및 필수 도구 설치
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg lsb-release

# 2. Docker 공식 GPG 키 추가
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# 3. Repository 설정
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 4. 설치 및 권한 부여
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo usermod -aG docker $USER
newgrp docker
```

**2. 프로젝트 클론 (Git Clone)**

```bash
cd /home/ubuntu
git clone https://lab.ssafy.com/s14-webmobile1-sub1/S14P11E106.git
cd hearo
```

---

### Phase 2. 환경 설정 (Configuration)

**1. 통합 환경 변수 파일 생성**
보안상 Git에 포함되지 않은 비밀번호와 키 값을 설정합니다.

```bash
# infra 폴더에 .env 생성
vi infra/.env
```

> **[Action]** 1.b의 `.env example`의 내용을 복사하여 붙여넣고, 값을 채워넣습니다.
> 

**2. Docker 네트워크 생성 (필수)**
인프라 컨테이너와 앱 컨테이너 간 통신을 위한 브리지 네트워크를 생성합니다.

```bash
docker network create ssafy-network
```

---

### Phase 3. 인프라 배포 (Infrastructure Deployment)

**1. 기반 서비스 실행**
DB, Redis, Jenkins, LiveKit 등 인프라 서비스를 먼저 구동합니다.\

```bash
cd infra
docker-compose -f docker-compose-infra.yml up -d --build
```

**2. 구동 확인 (Health Check)**
모든 컨테이너가 `Up` 상태인지 확인합니다.

```bash
docker ps
# postgres-db, redis, livekit-server, jenkins-server 등이 보여야 함
```

---

### Phase 4. 데이터베이스 초기화 (DB Restore)

**1. 초기 데이터 적재**
PostgreSQL 컨테이너가 정상 실행된 후(약 10초 후), 덤프 파일을 리스토어합니다.

```bash
# 프로젝트 루트로 이동
cd .. 

# 덤프 파일 주입 (비밀번호 입력 불필요 - postgres 유저 사용)
cat exec/hearo_dump.sql | docker exec -i postgres-db psql -U postgres -d hearo
```

---

### Phase 5. 애플리케이션 배포 (Application Deployment)

**1. 서비스 실행**
Backend(Spring), Frontend(Vue), AI(FastAPI), Nginx를 실행합니다.

```bash
cd infra
docker-compose -f docker-compose-prod.yaml up -d --build
```

---

### Phase 6. SSL 인증서 발급 (HTTPS Setup)

**1. Certbot 실행**
Nginx가 실행 중인 상태에서 SSL 인증서를 발급받습니다.

```bash
# Certbot 컨테이너 실행 (1회성)
docker-compose -f docker-compose-app.yml run --rm certbot certonly --webroot --webroot-path=/var/www/certbot -d i14p106.p.ssafy.io
```

**2. Nginx 재시작**
발급된 인증서를 적용하기 위해 Nginx를 재시작합니다.

```bash
docker restart nginx
```

---

### Phase 7. 일렉트론 앱 설치

https://drive.google.com/file/d/1aV5lkNDKQMJVryiT2DNo-hWWKoEYQoYs/view?usp=sharing
해당 주소에서 파일 다운로드 후 압축을 풀고 HearO Setup 0.0.0.exe 파일을 실행하여 구동합니다.

### Phase 8. 최종 접속 확인 (Verification)

브라우저를 열고 다음 주소로 접속하여 정상 작동을 확인합니다.

1. **상담원 메인 화면:** 일렉트론 실행
2. **고객 화면:** [https://i14e106.p.ssafy.io/client?productId=1&manufacturedAt=2025-01-01&warrantyEndsAt=2027-01-01](https://i14e106.p.ssafy.io/client?productId=1&manufacturedAt=2025-01-01&warrantyEndsAt=2027-01-01)

![image.png](images/image.png)

QR 접속 가능.

1. **Jenkins:** `https://i14p106.p.ssafy.io/jenkins`

### a. 사용한 JVM, 웹서버, WAS 제품 등의 종류와 설정 값, 버전 기재

| **구분** | **제품/도구명** | **버전 (Version)** | **설정 값 / 비고** |
| --- | --- | --- | --- |
| **IDE (Backend)** | IntelliJ IDEA | `[2021.3.1]` | Ultimate Edition |
| **IDE (Frontend)** | VS Code | `[1.108.2]` |  |
| **JVM (Java)** | OpenJDK (Temurin) | **17.0.17** | Docker Image: `eclipse-temurin:17-jdk-alpine` |
| **Web Server** | Nginx | **1.28.1 (Stable)** | Reverse Proxy, SSL 적용 (`/infra/nginx/default.conf` 참조) |
| **WAS** | Apache Tomcat | **10.1.50** | Spring Boot 3.1.2 내장 톰캣 (Embedded) |
| **Framework** | Spring Boot | **3.5.10** | Build: Gradle 8.14.3 |
| **Runtime** | Node.js | **v18.16.0** | NPM v9.5.1 |
| **Library** | Vue.js | **3.5.26** | Build: Vite 7.3.1 |
| **Database** | PostgreSQL | **15.15** | Port: 5432 (External: 8432) |
| **OS** | Ubuntu (EC2) | **24.04.3 LTS** | Kernel: `[6.14.0-1018-aws]` |

### b. 빌드 시 사용되는 환경 변수 등의 내용 상세 기재

.env example

```
# ==========================================
# HearO Project Environment Variables Sample
# 작성일: 2026.02.08
# ==========================================

# 1. Database (PostgreSQL)
# DB_USER: DB 접속 계정
DB_USER=postgres
# DB_PASSWORD: [필수] DB 비밀번호를 입력하세요
DB_PASSWORD=your_db_password_here
# DB_NAME: 사용할 데이터베이스 이름
DB_NAME=hearo_db
DB_DDL_AUTO=validate
# DB_URL: Docker 내부 통신용 URL (postgres 컨테이너 이름 사용)
DB_URL=jdbc:postgresql://postgres-db:5432/hearo_db?ssl=false
JPA_DDL_AUTO=validate

# 2. Redis
# REDIS_PASSWORD: [필수] Redis 비밀번호
REDIS_PASSWORD=your_redis_password_here
REDIS_PORT=6379
# REDIS_HOST: Docker Compose 서비스명 (redis)
REDIS_HOST=redis

# 3. LiveKit (화상 통화)
# LIVEKIT_API_KEY: LiveKit 프로젝트 API Key
LIVEKIT_API_KEY=your_livekit_api_key
# LIVEKIT_API_SECRET: [필수] LiveKit Secret Key (길고 복잡한 문자열)
LIVEKIT_API_SECRET=your_livekit_secret_key
# LIVEKIT_IP: 서버의 공인 IP 또는 내부 IP
LIVEKIT_IP=your_server_public_ip
# LIVEKIT_URL: 클라이언트가 접속할 WebSocket URL (wss://도메인/livekit)
LIVEKIT_URL=wss://your_domain.com/livekit

# 4. Backend Config
SPRING_PROFILES_ACTIVE=prod
# JWT 만료 시간 (ms)
JWT_ACCESS_TOKEN_EXPIRY_MS=18000000

# 5. Docker Permission
# 호스트 머신의 docker 그룹 ID (터미널에 `getent group docker` 입력 후 확인)
DOCKER_GID=your_docker_gid

# 6. ChromaDB (Vector DB)
# Docker Compose 서비스명 (chroma-db) 사용
CHROMA_DB_URL=http://chroma-db:8000

# 7. GMS (SSAFY OpenAI Proxy)
# GMS_URL: SSAFY 제공 GMS 프록시 URL
GMS_URL=https://gms.ssafy.io/gmsapi/api.openai.com/v1
# GMS_KEY: [필수] 발급받은 GMS API Key 입력
GMS_KEY=your_gms_api_key_here
```

### c. 배포 시 특이사항

현재 상담원 측 프론트엔드는 AI서버와 함께 일렉트론으로 포장된 상태입니다. 
https://drive.google.com/file/d/1aV5lkNDKQMJVryiT2DNo-hWWKoEYQoYs/view?usp=sharing
해당 주소에서 파일 다운로드 후 압축을 풀고 HearO Setup 0.0.0.exe 파일을 실행하여 구동합니다.

현재 docker-compose 파일이 **`docker-compose-infra.yaml`**, **`docker-compose-prod.yaml`**로 나뉘어 있는 상태입니다. 
데이터베이스와 메시지 브로커(Redis)의 안정적인 구동을 보장하기 위해, **인프라 컨테이너(`docker-compose-infra.yml`)를 먼저 실행**하여 Health Check가 완료된 후, **애플리케이션 컨테이너(`docker-compose-app.yml`)를 실행**하는 순차적 배포 방식을 권장합니다.

### d. DB 접속 정보 등 프로젝트(ERD)에 활용되는 주요 계정 및 프로퍼티가 정의된 파일 목록

모든 환경변수는 infra/ 경로의 .env 파일로 관리됩니다. 1.b의 .env example을 참고해주세요.

1. 인프라 및 배포 설정

| **경로 (Path)** | **파일명** | **용도 및 설명** | **비고** |
| --- | --- | --- | --- |
| `infra/` | **docker-compose-infra.yml** | **기반 서비스 실행** - DB(PostgreSQL), Redis, Jenkins, LiveKit 등 인프라 컨테이너 구동 | 선행 실행 권장 |
| `infra/` | **docker-compose-prod.yml** | **애플리케이션 실행** - Backend, Frontend, Nginx(Web Server) 컨테이너 구동 | `prod` 프로필 적용 |
| `infra/nginx/` | **default.conf** | **메인 웹 서버 설정** - 리버스 프록시, SSL 인증서 경로, WebSocket 타임아웃, 포트 포워딩 설정 | Host Nginx |
| `infra/jenkins/` | **Dockerfile** | **Jenkins 커스텀 이미지** - Docker in Docker(DinD) 환경 구성을 위한 Docker CLI 설치 포함 | CI/CD용 |
| `infra/` | **livekit.yaml** | **화상 서버 설정** - LiveKit 포트(TCP/UDP), API Key, 로깅 레벨 설정 | WebRTC |
| `root/` | **Jenkinsfile** | **CI/CD 파이프라인 스크립트** - Gitlab Webhook 트리거 시 빌드, 테스트, 배포 자동화 단계 정의 | Groovy Script |
1. 백엔드 설정

| **경로 (Path)** | **파일명** | **용도 및 설명** | **비고** |
| --- | --- | --- | --- |
| `backend/` | **Dockerfile** | **Spring Boot 이미지 빌드** - JDK 17 기반, Gradle 빌드, JAR 파일 실행 정의 | Multi-stage Build |
| `backend/src/main/resources/` | **application.yaml** | **공통 설정** - 모든 환경에서 공통으로 적용되는 JPA, Jackson, Log 설정 |  |
| `backend/src/main/resources/` | **application-dev.yaml** | **개발 환경 설정** - 로컬 개발 시 사용하는 H2/MySQL DB 설정, 디버그 모드 | `dev` 프로필 |
| `backend/src/main/resources/` | **application-prod.yaml** | **운영 환경 설정** - Docker 환경 변수(`${DB_URL}` 등)를 참조하도록 구성된 배포용 설정 | `prod` 프로필 |
1. 프론트엔드 설정

| **경로 (Path)** | **파일명** | **용도 및 설명** | **비고** |
| --- | --- | --- | --- |
| `frontend/` | **Dockerfile** | **Vue.js 이미지 빌드** - Node.js 빌드 후 Nginx(Alpine)로 정적 파일 서빙 | Multi-stage Build |
| `frontend/` | **default.conf** | **내부 웹 서버 설정** - SPA(Single Page Application) 라우팅 지원(`try_files`)을 위한 설정 | Container Nginx |

## 2. 프로젝트에서 사용하는 외부 서비스 정보를 정리한 문서

1. 소셜 인증, 포톤 클라우드, 코드 컴파일 등에 활용 된 ‘외부 서비스’가입 및 활용에 필요한 정보

**OpenAI API (필수)**

- **용도:**
    - **RAG (검색 증강 생성):** 상담 매뉴얼 PDF 임베딩 (`text-embedding-3-small`)
    - **AI 챗봇:** 상담원 질의응답 및 문맥 분석 (`gpt-3.5-turbo` 또는 `gpt-4`)
- **공식 사이트:** [https://platform.openai.com/](https://platform.openai.com/)
- **필요한 키 (Environment Variables):**
    - `OPENAI_API_KEY`: `sk-...` 로 시작하는 Secret Key
- **발급 및 설정 방법:**
    1. OpenAI Platform 회원가입 및 로그인.
    2. **API Keys** 메뉴에서 `Create new secret key` 클릭 후 발급.
    3. 발급된 키를 `infra/.env` 파일의 `OPENAI_API_KEY` 변수에 입력.

## 3. DB 덤프 파일 최신본

경로: /exec/hearo_dump.sql

복구 방법

```
cat hearo_dump.sql | docker exec -i postgres-db psql -U hearo_user -d hearo_db
```

## 4. 시연 시나리오

![image.png](images/image%201.png)

![image.png](images/image%202.png)

![image.png](images/image%203.png)

![image.png](images/image%204.png)

![image.png](images/image%205.png)

![image.png](images/image%206.png)

![image.png](images/image%207.png)

![image.png](images/image%208.png)

![image.png](images/image%209.png)

![image.png](images/image%2010.png)

![image.png](images/image%2011.png)

![image.png](images/image%2012.png)

![image.png](images/image%2013.png)

![image.png](images/image%2014.png)

![image.png](images/image%2015.png)

![image.png](images/image%2016.png)

![image.png](images/image%2017.png)

![image.png](images/image%2018.png)

![image.png](images/image%2019.png)

![image.png](images/image%2020.png)

![image.png](images/image%2021.png)