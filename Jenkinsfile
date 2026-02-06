// 1. 배포된 항목들을 담을 전역 리스트 정의
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
                    // 인프라 설정 파일들 전송
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
                // 🚀 백엔드 (소스 전송 -> 서버 빌드)
                // ==========================
                stage('Backend') {
                    when { changeset "backend/**" }
                    steps {
                        script { deployLog.add("🚀 백엔드") }
                        sshagent(credentials: ["${SSH_CRED_ID}"]) {
                            // 1. 소스 코드 폴더 통째로 전송 (이미지 아님!)
                            // 기존에 있던 폴더랑 섞이지 않게 덮어씌움
                            sh "scp -r -o StrictHostKeyChecking=no ./backend ${SERVER_USER}@${SERVER_IP}:${BASE_PATH}/"
                            
                            // 2. 서버에서 빌드 및 실행
                            sh """
                                ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} '
                                    cd ${BASE_PATH}
                                    
                                    # --build 옵션: 소스가 바뀌었으니 이미지를 새로 빌드해서 띄워라!
                                    echo "🔨 백엔드 빌드 및 배포 시작..."
                                    docker-compose -f docker-compose-prod.yaml up -d --build backend
                                    docker image prune -f
                                    
                                    # 3. 로그 확인 (헬스 체크)
                                    echo "⏳ 백엔드 구동 대기 중..."
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
                // ✨ 프론트엔드 (소스 전송 -> 서버 빌드)
                // ==========================
                stage('Frontend') {
                    when { changeset "frontend/**" }
                    steps {
                        script { deployLog.add("✨ 프론트엔드") }
                        sshagent(credentials: ["${SSH_CRED_ID}"]) {
                            // 1. 소스 코드 전송
                            sh "scp -r -o StrictHostKeyChecking=no ./frontend ${SERVER_USER}@${SERVER_IP}:${BASE_PATH}/"
                            
                            // 2. 서버에서 빌드 및 실행
                            sh """
                                ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} '
                                    cd ${BASE_PATH}
                                    
                                    echo "🔨 프론트엔드 빌드 및 배포 시작..."
                                    docker-compose -f docker-compose-prod.yaml up -d --build frontend
                                    docker image prune -f
                                '
                            """
                        }
                    }
                }
            }
        }
    }

    // post 블록 (알림)은 그대로 유지
    post {
        success {
            script {
                if (deployLog.size() > 0) {
                    def gitData = getGitData()
                    def deployContent = deployLog.join(', ')
                    sendMattermostAttachment("#2ecc71", "✅ 배포 성공 (Deployed: ${deployContent})", gitData.msg, gitData.author, gitData.merger, env.BUILD_URL, "")
                } else {
                    echo "변경 사항이 없어 알림을 건너뜁니다."
                }
            }
        }
        failure {
            script {
                def gitData = getGitData()
                def errorMsg = getErrorLogs()
                sendMattermostAttachment("#ff0000", "🚨 배포 실패 (Build Failed)", gitData.msg, gitData.author, gitData.merger, env.BUILD_URL, errorMsg)
            }
        }
    }
}

// 아래 Git 정보 함수들은 그대로 두면 돼!
def getGitData() {
    def merger = sh(script: "git log -1 --pretty=%cn", returnStdout: true).trim()
    def author = ""
    def message = ""
    def isMerge = sh(script: "git rev-parse -q --verify HEAD^2", returnStatus: true) == 0

    if (isMerge) {
        author = sh(script: "git log -1 --pretty=%an HEAD^2", returnStdout: true).trim()
        def rawLog = sh(script: "git log --no-merges --pretty=format:'• %s - %an' HEAD^1..HEAD", returnStdout: true).trim()
        def logLines = rawLog.split("\n")
        if (logLines.size() > 10) { message = logLines.take(10).join("\n") + "\n... (외 ${logLines.size() - 10}개의 커밋)" } else { message = rawLog }
    } else {
        author = sh(script: "git log -1 --pretty=%an", returnStdout: true).trim()
        message = sh(script: "git log -1 --pretty=format:'• %s - %an'", returnStdout: true).trim()
    }
    return [msg: message, author: author, merger: merger]
}

def getErrorLogs() {
    def logs = currentBuild.rawBuild.getLog(200)
    def errorKeywords = ["error", "exception", "fail", "fatal", "denied", "panic"]
    def firstErrorIndex = -1
    for (int i = 0; i < logs.size(); i++) { if (errorKeywords.any { logs[i].toLowerCase().contains(it) }) { firstErrorIndex = i; break } }
    if (firstErrorIndex != -1) {
        def rawErrorLogs = logs[firstErrorIndex..-1]
        if (rawErrorLogs.size() > 30) { return rawErrorLogs.take(30).join("\n") + "\n... (로그 중략) ..." }
        return rawErrorLogs.join("\n")
    } else { return logs.takeRight(15).join("\n") }
}

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
                "color": "${color}", "title": "${title}", "title_link": "${link}",
                "text": "#### 📜 Commit Info\\n${safeText}",
                "fields": [
                    { "short": true, "title": "👨‍💻 Author (Dev)", "value": "${author}" },
                    { "short": true, "title": "🔨 Merger (Approver)", "value": "${merger}" }
                    ${logContent} 
                ],
                "footer": "Jenkins Build #${env.BUILD_NUMBER}", "footer_icon": "https://www.jenkins.io/images/logos/jenkins/jenkins.png"
            }
        ]
    }
    """
    sh "curl -X POST -H 'Content-Type: application/json' -d '${payload}' ${MM_WEBHOOK}"
}