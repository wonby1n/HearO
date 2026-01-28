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
                stage('Backend') {
                    when { changeset "backend/**" }
                    steps {
                        script { deployLog.add("🚀 백엔드") }
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
                        script { deployLog.add("✨ 프론트엔드") }
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

    post {
        success {
            script {
                if (deployLog.size() > 0) {
                    // 공통 함수로 Git 정보 가져오기
                    def gitData = getGitData()
                    def deployContent = deployLog.join(', ')
                    
                    sendMattermostAttachment(
                        "#2ecc71", // 초록색
                        "✅ 배포 성공 (Deployed: ${deployContent})",
                        gitData.msg,    // 커밋 내역 리스트
                        gitData.author, // MR 올린 사람
                        gitData.merger, // 머지 버튼 누른 사람
                        env.BUILD_URL,
                        "" // 성공 시 에러 로그 없음
                    )
                } else {
                    echo "변경 사항이 없어 알림을 건너뜁니다."
                }
            }
        }

        failure {
            script {
                // 실패 시에도 Git 정보 똑같이 가져오기
                def gitData = getGitData()
                
                // 에러 로그 추출 (스마트 감지 로직)
                def errorMsg = getErrorLogs()

                sendMattermostAttachment(
                    "#ff0000", // 빨간색
                    "🚨 배포 실패 (Build Failed)",
                    gitData.msg,    // 실패 원인이 된 커밋 내역들
                    gitData.author, // MR 올린 사람 (범인?)
                    gitData.merger, // 머지해준 사람
                    env.BUILD_URL,
                    errorMsg // 추출된 에러 로그
                )
            }
        }
    }
}

/**
 * 🧹 Git 정보 추출 함수 (중복 제거)
 * Returns: [msg: "커밋리스트", author: "작성자", merger: "승인자"]
 */
def getGitData() {
    def merger = sh(script: "git log -1 --pretty=%cn", returnStdout: true).trim()
    def author = ""
    def message = ""
    
    // Merge 커밋인지 확인
    def isMerge = sh(script: "git rev-parse -q --verify HEAD^2", returnStatus: true) == 0

    if (isMerge) {
        // 🅰️ Merge 상황
        // Author = HEAD^2 (기능 브랜치의 마지막 커밋 작성자 = MR 올린 사람)
        author = sh(script: "git log -1 --pretty=%an HEAD^2", returnStdout: true).trim()
        
        // 커밋 메시지 리스트 추출
        def rawLog = sh(script: "git log --no-merges --pretty=format:'• %s - %an' HEAD^1..HEAD", returnStdout: true).trim()
        def logLines = rawLog.split("\n")
        if (logLines.size() > 10) {
            message = logLines.take(10).join("\n") + "\n... (외 ${logLines.size() - 10}개의 커밋)"
        } else {
            message = rawLog
        }
    } else {
        // 🅱️ Direct Push 상황
        author = sh(script: "git log -1 --pretty=%an", returnStdout: true).trim()
        message = sh(script: "git log -1 --pretty=format:'• %s - %an'", returnStdout: true).trim()
    }
    
    return [msg: message, author: author, merger: merger]
}

/**
 * 🔥 에러 로그 추출 함수
 */
def getErrorLogs() {
    // 1. 넉넉하게 로그 가져오기
    def logs = currentBuild.rawBuild.getLog(200)
    def errorKeywords = ["error", "exception", "fail", "fatal", "denied", "panic"]
    
    // 2. 키워드 검색
    def firstErrorIndex = -1
    for (int i = 0; i < logs.size(); i++) {
        if (errorKeywords.any { logs[i].toLowerCase().contains(it) }) {
            firstErrorIndex = i
            break
        }
    }

    // 3. 로그 잘라내기
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