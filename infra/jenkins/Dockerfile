# 기존에 사용하던 젠킨스 이미지 기반
FROM jenkins/jenkins:lts

# 설치를 위해 루트 권한으로 전환
USER root

# 패키지 목록 업데이트 및 rsync 설치
RUN apt-get update && \
    apt-get install -y rsync && \
    rm -rf /var/lib/apt/lists/*

# 설치 완료 후 다시 jenkins 유저로 복구
USER jenkins