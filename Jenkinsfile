// 1. 배포된 항목들을 담을 전역 리스트 정의
def deployLog = []

pipeline {
    agent any

    environment {
        // 🔒 크리덴셜 ID (Jenkins 관리 페이지에서 설정한 ID와 일치해야 함)
        GIT_CRED_ID = 'gitlab'
        DOCKER_CRED_ID = 'docker'
        SSH_CRED_ID = 'ssh'
        
        // 🌍 서버 및 저장소 정보
        GIT_REPO_URL = 'https://lab.ssafy.com/s14-webmobile1-sub1/S14P11E106.git'
        SERVER_IP = '13.125.88.103'
        SERVER_USER = 'ubuntu'
        BASE_PATH = '/home/ubuntu/infra'
        
        // 🐳 도커 이미지 이름
        BACKEND_IMAGE = 'hjh1248/hearo-backend'
        FRONTEND_IMAGE = 'hjh1248/hearo-frontend'
        
        // 📢 메터모스트 웹훅 URL
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
                script { deployLog.add("🛠️ 인프라") }
                sshagent(credentials: ["${SSH_CRED_ID}"]) {
                    // 파일 전송
                    sh "scp -r -o StrictHostKeyChecking=no ./infra/nginx ${SERVER_USER}@${SERVER_IP}:${BASE_PATH}/"
                    sh "scp -r -o StrictHostKeyChecking=no ./infra/jenkins ${SERVER_USER}@${SERVER_IP}:${BASE_PATH}/"
                    sh "scp -o StrictHostKeyChecking=no ./infra/*.yaml ${SERVER_USER}@${SERVER_IP}:${BASE_PATH}/"
                    
                    // 원격 실행
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
                        script { deployLog.add("🚀 백엔드") }
                        
                        // 빌드 및 푸시
                        dir('backend') {
                            script {
                                docker.withRegistry('', "${DOCKER_CRED_ID}") {
                                    docker.build("${BACKEND_IMAGE}:latest").push()
                                }
                            }
                        }
                        
                        // 배포
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
                        script { deployLog.add("✨ 프론트엔드") }
                        
                        // 빌드 및 푸시
                        dir('frontend') {
                            script {
                                docker.withRegistry('', "${DOCKER_CRED_ID}") {
                                    docker.build("${FRONTEND_IMAGE}:latest").push()
                                }
                            }
                        }
                        
                        // 배포
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

    post {
        success {
            script {
                // 변경 사항이 있을 때만 알림 전송
                if (deployLog.size() > 0) {
                    // Git 정보 추출
                    def commitMsg = sh(script: "git log -1 --pretty=%B", returnStdout: true).trim()
                    def commitAuthor = sh(script: "git log -1 --pretty=%an", returnStdout: true).trim()
                    def commitMerger = sh(script: "git log -1 --pretty=%cn", returnStdout: true).trim()
                    
                    def deployContent = deployLog.join(', ')
                    
                    sendMattermostAttachment(
                        "#2ecc71", // 초록색 (성공)
                        "✅ 배포 성공 (Deployed: ${deployContent})",
                        commitMsg,
                        commitAuthor,
                        commitMerger,
                        env.BUILD_URL,
                        "" // 에러 로그 없음
                    )
                } else {
                    echo "변경 사항이 없어 알림을 건너뜁니다."
                }
            }
        }

        failure {
            script {
                def commitAuthor = sh(script: "git log -1 --pretty=%an", returnStdout: true).trim()
                
                // 🔥 [스마트 에러 로그 추출]
                // 1. 넉넉하게 뒤에서 200줄 가져옴 (Script Approval 필요할 수 있음)
                def logs = currentBuild.rawBuild.getLog(200)
                
                // 2. 에러 키워드 정의
                def errorKeywords = ["error", "exception", "fail", "fatal", "denied", "panic"]
                
                // 3. 에러 발생 위치 찾기
                def firstErrorIndex = -1
                for (int i = 0; i < logs.size(); i++) {
                    if (errorKeywords.any { logs[i].toLowerCase().contains(it) }) {
                        firstErrorIndex = i
                        break
                    }
                }

                def errorMsg = ""
                if (firstErrorIndex != -1) {
                    // 에러 시점부터 끝까지 추출
                    def rawErrorLogs = logs[firstErrorIndex..-1]
                    
                    // 너무 길면 30줄로 자름
                    if (rawErrorLogs.size() > 30) {
                        rawErrorLogs = rawErrorLogs.take(30)
                        rawErrorLogs.add("\n... (로그가 너무 길어 중략됨. 전체 로그 확인 필요) ...")
                    }
                    errorMsg = rawErrorLogs.join("\n")
                } else {
                    // 키워드를 못 찾았으면 마지막 15줄 출력
                    errorMsg = logs.takeRight(15).join("\n")
                }

                sendMattermostAttachment(
                    "#ff0000", // 빨간색 (실패)
                    "🚨 배포 실패 (Build Failed)",
                    "에러 발생 지점부터 로그를 출력합니다.",
                    commitAuthor,
                    "Unknown",
                    env.BUILD_URL,
                    errorMsg 
                )
            }
        }
    }
}

/**
 * Mattermost Attachments 전송 함수
 */
def sendMattermostAttachment(String color, String title, String text, String author, String merger, String link, String errorLog) {
    // 1. 텍스트 이스케이프 처리
    def safeText = text.replace('"', '\\"').replace('\n', '\\n')
    
    // 2. 에러 로그 블록 처리
    def logContent = ""
    if (errorLog && errorLog.trim() != "") {
        def safeLog = errorLog.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n')
        logContent = ", { \"title\": \"🔥 Error Log (Trace)\", \"value\": \"```\\n${safeLog}\\n```\", \"short\": false }"
    }

    // 3. JSON 페이로드 조립
    def payload = """
    {
        "attachments": [
            {
                "color": "${color}",
                "title": "${title}",
                "title_link": "${link}",
                "text": "#### 📜 Commit Info\\n${safeText}",
                "fields": [
                    {
                        "short": true,
                        "title": "👨‍💻 Author",
                        "value": "${author}"
                    },
                    {
                        "short": true,
                        "title": "🔨 Merger",
                        "value": "${merger}"
                    }
                    ${logContent} 
                ],
                "footer": "Jenkins Build #${env.BUILD_NUMBER}",
                "footer_icon": "https://www.jenkins.io/images/logos/jenkins/jenkins.png"
            }
        ]
    }
    """
    
    // 4. 전송
    sh "curl -X POST -H 'Content-Type: application/json' -d '${payload}' ${MM_WEBHOOK}"
}