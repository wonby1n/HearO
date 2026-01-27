// 1. 배포된 항목들을 담을 그릇 (리스트) 정의
def deployLog = []

pipeline {
    agent any

    environment {
        GIT_CRED_ID = 'gitlab'
        DOCKER_CRED_ID = 'docker'
        SSH_CRED_ID = 'ssh'
        GIT_REPO_URL = 'https://lab.ssafy.com/s14-webmobile1-sub1/S14P11E106.git'
        SERVER_IP = '13.125.88.103'
        SERVER_USER = 'ubuntu'
        BASE_PATH = '/home/ubuntu/infra'
        
        BACKEND_IMAGE = 'hjh1248/hearo-backend'
        FRONTEND_IMAGE = 'hjh1248/hearo-frontend'
        
        // 메터모스트 웹훅
        MM_WEBHOOK = 'https://meeting.ssafy.com/hooks/abhj49fbs7yh8cfp34gg3uh3do'
    }

    stages {
        stage('Git Checkout') {
            steps {
                git branch: 'main', credentialsId: "${GIT_CRED_ID}", url: "${GIT_REPO_URL}"
            }
        }

        stage('Infra Setup') {
            when { changeset "infra/**" }
            steps {
                script {
                    // 실행됐다는 건 인프라가 변경됐다는 뜻! 리스트에 추가
                    deployLog.add("🛠️ 인프라") 
                }
                sshagent(credentials: ["${SSH_CRED_ID}"]) {
                    // 파일 전송 및 인프라 업데이트 (기존 코드)
                    sh "scp -r -o StrictHostKeyChecking=no ./infra/nginx ${SERVER_USER}@${SERVER_IP}:${BASE_PATH}/"
                    sh "scp -r -o StrictHostKeyChecking=no ./infra/jenkins ${SERVER_USER}@${SERVER_IP}:${BASE_PATH}/"
                    sh "scp -o StrictHostKeyChecking=no ./infra/*.yaml ${SERVER_USER}@${SERVER_IP}:${BASE_PATH}/"
                    sh """
                        ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} '
                            cd ${BASE_PATH}
                            docker-compose -f docker-compose-infra.yaml up -d --build
                            docker image prune -f
                        '
                    """
                }
            }
        }

        stage('App Deploy') {
            parallel {
                stage('Backend') {
                    when { changeset "backend/**" }
                    steps {
                        script { deployLog.add("🚀 백엔드") } // 리스트 추가
                        
                        dir('backend') {
                            script {
                                docker.withRegistry('', "${DOCKER_CRED_ID}") {
                                    docker.build("${BACKEND_IMAGE}:latest").push()
                                }
                            }
                        }
                        sshagent(credentials: ["${SSH_CRED_ID}"]) {
                            sh """
                                ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} '
                                    cd ${BASE_PATH}
                                    docker-compose -f docker-compose-prod.yaml pull backend
                                    docker-compose -f docker-compose-prod.yaml up -d backend
                                    docker image prune -f
                                '
                            """
                        }
                    }
                }

                stage('Frontend') {
                    when { changeset "frontend/**" }
                    steps {
                        script { deployLog.add("✨ 프론트엔드") } // 리스트 추가

                        dir('frontend') {
                            script {
                                docker.withRegistry('', "${DOCKER_CRED_ID}") {
                                    docker.build("${FRONTEND_IMAGE}:latest").push()
                                }
                            }
                        }
                        sshagent(credentials: ["${SSH_CRED_ID}"]) {
                            sh """
                                ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} '
                                    cd ${BASE_PATH}
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

    // ⭐ 여기가 핵심! (모든 단계가 끝난 후 한 번만 실행)
    post {
        success {
            script {
                // 1. 변경사항이 있었는지 확인
                if (deployLog.size() > 0) {
                    // 예: "🚀 백엔드, ✨ 프론트엔드" 처럼 문자열 합치기
                    def deployContent = deployLog.join(', ')
                    
                    def message = "📢 **[배포 성공]** 이번 배포에 포함된 내용: **${deployContent}**\\n👉 <${env.BUILD_URL}|로그 보러가기>"
                    sendMattermost(message)
                } else {
                    // 변경사항이 하나도 없어서 스킵된 경우 (선택 사항: 안 보내도 됨)
                    echo "변경 사항이 없어 배포된 항목이 없습니다."
                }
            }
        }

        failure {
            script {
                def msg = "🚨 **[배포 실패]** 에러 발생! 로그를 확인하세요.\\n👉 <${env.BUILD_URL}|로그 보러가기>"
                sendMattermost(msg)
            }
        }
    }
}

// 메터모스트 전송 함수
def sendMattermost(String message) {
    // JSON 포맷에 맞게 줄바꿈 등 이스케이프 처리
    def payload = """{"text": "${message}"}"""
    sh "curl -X POST -H 'Content-Type: application/json' -d '${payload}' ${MM_WEBHOOK}"
}