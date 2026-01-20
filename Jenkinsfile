pipeline {
    agent any
    stages {
        stage('Git Clone') {
            steps {
                git branch: 'main', credentialsId: 'gitlab-auth', url: 'https://gitlab.com/니네프로젝트주소.git'
            }
        }
        
        stage('Build & Run') {
            steps {
                sh '''
                # 1. 백엔드 이미지 빌드
                cd backend
                docker build -t my-backend .
                
                # 2. 프론트엔드 이미지 빌드
                cd ../frontend
                docker build -t my-frontend .
                
                # 3. 기존 컨테이너 정리 (있으면 삭제)
                docker rm -f backend-container || true
                docker rm -f frontend-container || true
                
                # 4. 백엔드 실행 
                docker run -d --name backend-container \
                --network host \
                -e DB_URL=jdbc:postgresql://localhost:5432/mydb \
                my-backend
                
                # 5. 프론트엔드 실행
                docker run -d --name frontend-container \
                --network host \
                my-frontend
                '''
            }
        }
    }
}