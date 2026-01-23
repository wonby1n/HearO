# 기존에 사용하던 젠킨스 이미지 기반
FROM jenkins/jenkins:lts

# 프로그램 설치를 위해 루트 권한으로 전환
USER root

# 1. 패키지 목록 업데이트, rsync 및 curl 설치 (도커 컴포즈 다운로드를 위해 curl 추가)
RUN apt-get update && \
    apt-get install -y rsync curl && \
    rm -rf /var/lib/apt/lists/*

# 2. 도커 컴포즈(docker-compose) 다운로드 및 실행 권한 부여 (추가된 핵심 부분! ⭐)
RUN curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose \
    && chmod +x /usr/local/bin/docker-compose

# 3. 설치 완료 후 다시 jenkins 유저로 복구
USER jenkins