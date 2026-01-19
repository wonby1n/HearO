# Continuous Claude 설치 가이드 (Windows)

> Claude Code의 컨텍스트 관리 시스템 설치 과정 및 트러블슈팅 기록

---

## 📌 도입 배경

### 이전 프로젝트에서의 경험

1학기 관통 프로젝트에서 Claude Code를 활용해 개발을 진행했다. 마지막 집중 주간에 대부분의 구현을 완료했는데, 이 과정에서 두 가지 불편함을 경험했다.

**1. 컨텍스트 자동 압축으로 인한 맥락 손실**

Claude Code로 대화를 이어가다 보면, 어느 순간 갑자기 새 탭이 열리면서 대화가 재시작되는 현상이 있었다. 이는 Claude가 이전 대화 맥락을 자동으로 요약(compaction)하고 새로운 세션으로 옮기는 동작인데, 이 과정에서 세부적인 맥락이 손실되어 다시 설명해야 하는 상황이 발생했다.

**2. 토큰 한도 부족**

Claude Pro를 구독 중임에도 불구하고, 집중적인 구현 과정에서 토큰이 빠르게 소모되었다. 특히 여러 파일을 동시에 분석하거나, 코드 전체를 컨텍스트에 포함시킬 때 한도에 금방 도달했다.

### Continuous Claude란?

이러한 문제를 해결하기 위해 **Continuous Claude**를 도입했다. Claude Code CLI를 위한 컨텍스트 관리 시스템으로, 다음과 같은 기능을 제공한다:

| 기존 문제 | Continuous Claude 해결책 |
|-----------|-------------------------|
| 컨텍스트 압축 시 맥락 손실 | YAML 핸드오프로 세션 간 상태 명시적 전달 |
| 토큰 과다 소모 | TLDR 5단계 코드 분석으로 **~95% 토큰 절약** |
| 매 세션 새로 시작 | 메모리 시스템으로 학습 내용 축적 |

### 핵심 개념: "Compound, don't compact"

Claude의 기본 동작은 컨텍스트가 가득 차면 자동으로 압축(compact)하는 것이다. 이 과정에서 세부사항이 손실된다.

Continuous Claude는 다른 접근을 취한다: 압축 대신 **학습 내용을 추출하고 축적(compound)**한다. 세션이 끝나면 핵심 내용을 핸드오프 문서로 저장하고, 다음 세션에서 이를 불러와 이어갈 수 있다.

---

## 🔧 설치 가이드

### 사전 요구사항

- Windows 10/11
- Git
- 관리자 권한 PowerShell

### Step 1: uv 패키지 관리자 설치

PowerShell (관리자 권한)에서:

```powershell
powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"
```

설치 후 **새 터미널 열고** 확인:

```powershell
uv --version
```

> **Note**: PATH 자동 등록 안 되면 터미널 재시작 필요

### Step 2: Docker Desktop 설치

1. https://www.docker.com/products/docker-desktop/ 에서 다운로드
2. **AMD64** 버전 선택 (일반 Windows PC)
3. 설치 중 **"Use WSL 2 instead of Hyper-V"** 체크
4. 설치 완료 후 재부팅
5. Docker Desktop 실행 및 로그인

확인:

```powershell
docker --version
docker ps
```

### Step 3: WSL 설치

> ⚠️ **중요**: Windows PowerShell에서 직접 설치 시 Unix 의존성 패키지 빌드 실패. WSL 필수!

```powershell
wsl --install
```

재부팅 후 Ubuntu 계정 설정 (username, password 입력)

### Step 4: Docker Desktop - WSL 연동

1. Docker Desktop 열기
2. **Settings(⚙️)** → **Resources** → **WSL Integration**
3. **"Enable integration with my default WSL distro"** 켜기
4. **Ubuntu** 토글 켜기
5. **Apply & Restart**

WSL 터미널에서 확인:

```bash
docker --version
```

### Step 5: WSL에서 Continuous Claude 설치

WSL 터미널 열고:

```bash
# 필수 도구 설치
sudo apt update
sudo apt install -y python3 python3-pip git curl nodejs npm

# uv 설치
curl -LsSf https://astral.sh/uv/install.sh | sh
source ~/.local/bin/env

# Continuous Claude 클론 및 설치
git clone https://github.com/parcadei/Continuous-Claude-v3.git
cd Continuous-Claude-v3/opc
uv run python scripts/setup/wizard.py
```

### Step 6: 설치 위자드 진행

대부분 **Enter (기본값)** 선택:

| 단계 | 선택 |
|------|------|
| Database mode | docker (기본값) |
| PostgreSQL host | localhost (기본값) |
| PostgreSQL port | 5432 (기본값) |
| Database name | continuous_claude (기본값) |
| Database user | claude (기본값) |
| Database password | claude_dev (기본값) |
| API Keys | n (나중에 설정 가능) |
| Docker stack | y (기본값) |
| Database migrations | y (기본값) |
| Claude Code integration | y (기본값) |
| Math features | n (필요 없으면 스킵) |
| TLDR code analysis | **y (핵심 기능!)** |
| Loogle | n (필요 없으면 스킵) |

### Step 7: Claude Code CLI 설치

```bash
sudo npm install -g @anthropic-ai/claude-code
```

확인:

```bash
claude --version
```

---

## 🔍 트러블슈팅

### Issue 1: Python C 확장 빌드 실패

**환경**: Windows PowerShell에서 직접 설치 시도

**에러 메시지**:
```
× Failed to build `polyleven==0.9.0`
error: Microsoft Visual C++ 14.0 or greater is required.
```

**원인**: Python C 확장 패키지 컴파일에 필요한 빌드 도구 미설치

**해결**:
1. https://visualstudio.microsoft.com/visual-cpp-build-tools/ 접속
2. Build Tools 다운로드 및 설치
3. "Desktop development with C++" 워크로드 선택

---

### Issue 2: Unix 전용 패키지 빌드 실패

**환경**: C++ Build Tools 설치 후 Windows에서 재시도

**에러 메시지**:
```
× Failed to build `jq==1.10.0`
Executing: ./configure CFLAGS=-fPIC -pthread
error: [WinError 2] 지정된 파일을 찾을 수 없습니다
```

**원인**: `jq` 패키지가 Unix 빌드 스크립트(`./configure`)를 실행하려 함

**해결**: WSL 환경으로 전환하여 설치 진행

---

### Issue 3: WSL에서 Docker 인식 불가

**환경**: WSL Ubuntu에서 설치 위자드 실행

**에러 메시지**:
```
Docker or Podman is required but not installed.
```

**원인**: Windows Docker Desktop과 WSL 간 연동 설정 누락

**해결**:
1. Docker Desktop → Settings → Resources → WSL Integration
2. "Enable integration with my default WSL distro" 활성화
3. Ubuntu 토글 활성화 후 Apply & Restart

---

### Issue 4: npm 전역 설치 권한 오류

**환경**: WSL에서 Claude Code 설치 시도

**에러 메시지**:
```
npm ERR! code EACCES
npm ERR! Error: EACCES: permission denied, mkdir '/usr/local/lib/node_modules'
```

**원인**: 전역 패키지 설치에 관리자 권한 필요

**해결**:
```bash
sudo npm install -g @anthropic-ai/claude-code
```

---

## 🎯 사용법

### VS Code에서 사용하기

1. `Ctrl + Shift + P` → **"WSL: Connect to WSL"**
2. 프로젝트 폴더 열기
3. 터미널 (`Ctrl + ``) 에서:

```bash
claude
```

### Windows 폴더 접근

WSL에서 Windows 경로는 `/mnt/c/`, `/mnt/d/` 형태:

```bash
# 예: C:\Users\SSAFY\Desktop\project
cd /mnt/c/Users/SSAFY/Desktop/project
claude
```

### 주요 명령어

| 명령어 | 용도 |
|--------|------|
| `/workflow-router` | 작업 가이드 시작 |
| `/explore` | 코드베이스 파악 |
| `/build greenfield "기능명"` | 새 기능 개발 |
| `/fix bug "설명"` | 버그 수정 |
| `/help` | 전체 명령어 목록 |

### 세션 연속성 활용

```bash
# 작업 종료 시
"done for today"  # 핸드오프 생성

# 다음 작업 시작 시
"resume work"     # 이전 상태 복원
```

---

## 🔗 참고 자료

- [Continuous Claude v3 GitHub](https://github.com/parcadei/Continuous-Claude-v3)
- [Claude Code 공식 문서](https://docs.anthropic.com/en/docs/claude-code)