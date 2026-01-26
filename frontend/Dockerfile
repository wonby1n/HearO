# 1. 빌드 단계 (Build Stage)
FROM node:20-alpine AS build-stage
WORKDIR /app

# 캐싱을 위해 패키지 파일만 먼저 복사
COPY package*.json ./

# npm install 보다 더 빠르고 안전한 ci 사용
RUN npm ci

# 소스 복사 및 빌드
COPY . .
RUN npm run build

# 2. 배포 단계 (Production Stage)
FROM nginx:stable-alpine as production-stage

# 타임존 설정 (로그 확인용)
ENV TZ=Asia/Seoul

# 빌드된 결과물(dist)을 Nginx가 서빙할 위치로 복사
COPY --from=build-stage /app/dist /usr/share/nginx/html

# [핵심] SPA 라우팅 처리를 위한 Nginx 설정 파일 교체
COPY default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]