// 1. 배포된 항목들을 담을 전역 리스트 정의
def deployLog = []

pipeline {
    agent any

    environment {
        GIT_CRED_ID = 'gitlab'
        DOCKER_CRED_ID = 'docker' // 빌드할 때 필요할 수 있어서 남겨둠 (Base Image pull 등)
        SSH_CRED_ID = 'ssh'
        GIT_REPO_URL = 'https://lab.ssafy.com/s14-webmobile1-sub1/S14P11E106.git'
        SERVER_IP = '13.125.88.103'
        SERVER_USER = 'ubuntu'
        BASE_PATH = '/home/ubuntu/infra'
        
        // ⭐️ 중요: docker-compose-prod.yaml에 적힌 image 이름과 똑같아야 함
        BACKEND_IMAGE = 'hjh1248/hearo-backend'
        FRONTEND_IMAGE = 'hjh1248/hearo-frontend'
        
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
                // ==========================
                // 🚀 백엔드 파이프라인 (이미지 파일 전송 방식)
                // ==========================
                stage('Backend') {
                    when { changeset "backend/**" }
                    steps {
                        script { deployLog.add("🚀 백엔드") }
                        
                        // 1. 젠킨스 내부에서 빌드 & 압축
                        dir('backend') {
                            script {
                                echo "🔨 백엔드 이미지 빌드 중..."
                                sh "docker build -t ${BACKEND_IMAGE}:latest ."
                                
                                echo "📦 이미지 압축 중 (tar.gz)..."
                                sh "docker save ${BACKEND_IMAGE}:latest | gzip > backend.tar.gz"
                            }
                        }

                        // 2. 서버로 전송 및 실행
                        sshagent(credentials: ["${SSH_CRED_ID}"]) {
                            // 압축 파일 전송
                            sh "scp -o StrictHostKeyChecking=no ./backend/backend.tar.gz ${SERVER_USER}@${SERVER_IP}:${BASE_PATH}/"
                            
                            sh """
                                ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} '
                                    cd ${BASE_PATH}
                                    
                                    # (1) 이미지 로드
                                    echo "📥 백엔드 이미지 로드 중..."
                                    gunzip -c backend.tar.gz | docker load
                                    rm -f backend.tar.gz # 용량 확보를 위해 바로 삭제
                                    
                                    # (2) 컨테이너 재시작 (이미지 로드했으니 pull 없이 up)
                                    docker-compose -f docker-compose-prod.yaml up -d backend
                                    docker image prune -f
                                    
                                    # (3) 헬스 체크
                                    echo "⏳ 백엔드 앱 구동 대기 중..."
                                    sleep 20
                                    LOGS=\$(docker-compose -f docker-compose-prod.yaml logs --tail=100 backend 2>&1)

                                    if echo "\$LOGS" | grep -iE "Error|Exception|Fail"; then
                                        echo "🚨 에러 감지됨!"
                                        echo "\$LOGS" 
                                        exit 1
                                    else
                                        echo "✅ 정상 구동 확인"
                                    fi
                                '
                            """
                        }
                    }
                }

                // ==========================
                // ✨ 프론트엔드 파이프라인 (이미지 파일 전송 방식)
                // ==========================
                stage('Frontend') {
                    when { changeset "frontend/**" }
                    steps {
                        script { deployLog.add("✨ 프론트엔드") }
                        
                        // 1. 젠킨스 내부에서 빌드 & 압축
                        dir('frontend') {
                            script {
                                echo "🔨 프론트엔드 이미지 빌드 중..."
                                sh "docker build -t ${FRONTEND_IMAGE}:latest ."
                                
                                echo "📦 이미지 압축 중 (tar.gz)..."
                                sh "docker save ${FRONTEND_IMAGE}:latest | gzip > frontend.tar.gz"
                            }
                        }

                        // 2. 서버로 전송 및 실행
                        sshagent(credentials: ["${SSH_CRED_ID}"]) {
                            sh "scp -o StrictHostKeyChecking=no ./frontend/frontend.tar.gz ${SERVER_USER}@${SERVER_IP}:${BASE_PATH}/"
                            
                            sh """
                                ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} '
                                    cd ${BASE_PATH}
                                    
                                    # (1) 이미지 로드
                                    echo "📥 프론트엔드 이미지 로드 중..."
                                    gunzip -c frontend.tar.gz | docker load
                                    rm -f frontend.tar.gz
                                    
                                    # (2) 컨테이너 재시작
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
                if (deployLog.size() > 0) {
                    def gitData = getGitData()
                    def deployContent = deployLog.join(', ')
                    
                    sendMattermostAttachment(
                        "#2ecc71", 
                        "✅ 배포 성공 (Deployed: ${deployContent})",
                        gitData.msg, gitData.author, gitData.merger, env.BUILD_URL, ""
                    )
                } else {
                    echo "변경 사항이 없어 알림을 건너뜁니다."
                }
            }
        }

        failure {
            script {
                def gitData = getGitData()
                def errorMsg = getErrorLogs()

                sendMattermostAttachment(
                    "#ff0000", 
                    "🚨 배포 실패 (Build Failed)",
                    gitData.msg, gitData.author, gitData.merger, env.BUILD_URL, errorMsg
                )
            }
        }
    }
}

/**
 * 🧹 Git 정보 추출 함수
 */
def getGitData() {
    def merger = sh(script: "git log -1 --pretty=%cn", returnStdout: true).trim()
    def author = ""
    def message = ""
    
    def isMerge = sh(script: "git rev-parse -q --verify HEAD^2", returnStatus: true) == 0

    if (isMerge) {
        author = sh(script: "git log -1 --pretty=%an HEAD^2", returnStdout: true).trim()
        def rawLog = sh(script: "git log --no-merges --pretty=format:'• %s - %an' HEAD^1..HEAD", returnStdout: true).trim()
        def logLines = rawLog.split("\n")
        if (logLines.size() > 10) {
            message = logLines.take(10).join("\n") + "\n... (외 ${logLines.size() - 10}개의 커밋)"
        } else {
            message = rawLog
        }
    } else {
        author = sh(script: "git log -1 --pretty=%an", returnStdout: true).trim()
        message = sh(script: "git log -1 --pretty=format:'• %s - %an'", returnStdout: true).trim()
    }
    
    return [msg: message, author: author, merger: merger]
}

/**
 * 🔥 에러 로그 추출 함수
 */
def getErrorLogs() {
    def logs = currentBuild.rawBuild.getLog(200)
    def errorKeywords = ["error", "exception", "fail", "fatal", "denied", "panic"]
    
    def firstErrorIndex = -1
    for (int i = 0; i < logs.size(); i++) {
        if (errorKeywords.any { logs[i].toLowerCase().contains(it) }) {
            firstErrorIndex = i
            break
        }
    }

    if (firstErrorIndex != -1) {
        def rawErrorLogs = logs[firstErrorIndex..-1]
        if (rawErrorLogs.size() > 30) {
            return rawErrorLogs.take(30).join("\n") + "\n... (로그 중략) ..."
        }
        return rawErrorLogs.join("\n")
    } else {
        return logs.takeRight(15).join("\n")
    }
}

/**
 * 📢 Mattermost 전송 함수
 */
def sendMattermostAttachment(String color, String title, String text, String author, String merger, String link, String errorLog) {
    def safeText = text.replace('"', '\\"').replace('\n', '\\n')
    
    def logContent = ""
    if (errorLog && errorLog.trim() != "") {
        def safeLog = errorLog.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n')
        logContent = ", { \"title\": \"🔥 Error Log (Trace)\", \"value\": \"```\\n${safeLog}\\n```\", \"short\": false }"
    }

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
                        "title": "👨‍💻 Author (Dev)",
                        "value": "${author}"
                    },
                    {
                        "short": true,
                        "title": "🔨 Merger (Approver)",
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
    sh "curl -X POST -H 'Content-Type: application/json' -d '${payload}' ${MM_WEBHOOK}"
}