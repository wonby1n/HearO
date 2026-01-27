pipeline {
    agent any

    environment {
        // --- 1. 공통 설정 ---
        GIT_CRED_ID = 'gitlab'
        DOCKER_CRED_ID = 'docker'
        SSH_CRED_ID = 'ssh'
        
        GIT_REPO_URL = 'https://lab.ssafy.com/s14-webmobile1-sub1/S14P11E106.git'
        SERVER_IP = '13.125.88.103'
        SERVER_USER = 'ubuntu'
        
        // 경로 설정
        // 어차피 infra 폴더 안에 다 있으니까 경로 하나로 통일해서 쓰자
        BASE_PATH = '/home/ubuntu/infra'
        
        // --- 2. 이미지 이름 ---
        BACKEND_IMAGE = 'hjh1248/hearo-backend'
        FRONTEND_IMAGE = 'hjh1248/hearo-frontend'
    }

    stages {
        // [1단계] 코드 가져오기
        stage('Git Checkout') {
            steps {
                git branch: 'main', credentialsId: "${GIT_CRED_ID}", url: "${GIT_REPO_URL}"
            }
        }

        // [2단계] 인프라 먼저 설정! (네 말대로 여기서 먼저 셋팅)
        stage('Infra Setup') {
            // infra 폴더가 바뀌었을 때만 실행 (안 바뀌었으면 기존 파일 믿고 패스)
            when { changeset "infra/**" }
            steps {
                sshagent(credentials: ["${SSH_CRED_ID}"]) {
                    // 1. 필요한 파일들 한 방에 전송
                    // Nginx, Jenkins 폴더, 그리고 모든 yaml 파일들 (prod, infra 등등)
                    sh "scp -r -o StrictHostKeyChecking=no ./infra/nginx ${SERVER_USER}@${SERVER_IP}:${BASE_PATH}/"
                    sh "scp -r -o StrictHostKeyChecking=no ./infra/jenkins ${SERVER_USER}@${SERVER_IP}:${BASE_PATH}/"
                    sh "scp -o StrictHostKeyChecking=no ./infra/*.yaml ${SERVER_USER}@${SERVER_IP}:${BASE_PATH}/"
                    
                    // 2. 인프라 컨테이너(Nginx, Jenkins 등) 최신화
                    sh """
                        ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} '
                            cd ${BASE_PATH}
                            echo "--- 🛠 인프라(Nginx/설정) 업데이트 ---"
                            # 인프라용 컴포즈 실행
                            docker-compose -f docker-compose-infra.yaml up -d --build
                            docker image prune -f
                        '
                    """
                }
            }
        }

        // [3단계] 앱 배포 (인프라 셋팅 끝났으니 맘 놓고 병렬 실행)
        stage('App Deploy') {
            parallel {
                
                // ==================== [Backend] ====================
                stage('Backend') {
                    when { changeset "backend/**" }
                    steps {
                        dir('backend') {
                            script {
                                docker.withRegistry('', "${DOCKER_CRED_ID}") {
                                    def customImage = docker.build("${BACKEND_IMAGE}:latest")
                                    customImage.push()
                                }
                            }
                        }
                        // scp 필요 없음! 이미 2단계나, 혹은 이전에 전송된 파일 사용
                        sshagent(credentials: ["${SSH_CRED_ID}"]) {
                            sh """
                                ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} '
                                    cd ${BASE_PATH}
                                    echo "--- 🚀 백엔드 배포 ---"
                                    # 파일 전송 없이 바로 도커 명령!
                                    docker-compose -f docker-compose-prod.yaml pull backend
                                    docker-compose -f docker-compose-prod.yaml up -d backend
                                    docker image prune -f
                                '
                            """
                        }
                    }
                }

                // ==================== [Frontend] ====================
                stage('Frontend') {
                    when { changeset "frontend/**" }
                    steps {
                        dir('frontend') {
                            script {
                                docker.withRegistry('', "${DOCKER_CRED_ID}") {
                                    def customImage = docker.build("${FRONTEND_IMAGE}:latest")
                                    customImage.push()
                                }
                            }
                        }
                        sshagent(credentials: ["${SSH_CRED_ID}"]) {
                            sh """
                                ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} '
                                    cd ${BASE_PATH}
                                    echo "--- 🚀 프론트엔드 배포 ---"
                                    docker-compose -f docker-compose-prod.yaml pull frontend
                                    docker-compose -f docker-compose-prod.yaml up -d frontend
                                    docker image prune -f
                                '
                            """
                        }
                    }
                }
            }
        }
    }
}