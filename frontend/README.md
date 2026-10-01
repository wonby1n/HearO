# HearO Frontend

Vue 3 + Vite 기반 웹 클라이언트이며, 상담원용 화면은 Electron 데스크톱 앱으로도 패키징됩니다.

- **고객 화면**: QR 접수 → 본인 확인 → 개인정보 동의 → 대기열 → 통화
- **상담원 화면**: 로그인 → 대시보드 → 매칭 → 통화(실시간 자막, 폭언 마스킹, AI 가이드) → 통화 이력

## 실행 환경

- Node.js `^20.19.0` 또는 `>=22.12.0`
- 백엔드 서버 (`../backend`)
- 폭언 감지를 사용하려면 로컬 AI 서버 (`../ai`, 기본 `http://127.0.0.1:8000`)

## 설치

```sh
npm install
```

## 실행

```sh
# 웹 개발 서버 (http://localhost:5173)
npm run dev

# 웹 + Electron 동시 실행
npm run dev:all
```

## 빌드

```sh
# 웹 빌드 (dist/)
npm run build

# Electron 설치 파일 빌드 (Windows NSIS)
# ../ai/dist/ai-server.exe 가 미리 빌드되어 있어야 합니다
npm run dist
```

## 환경 변수

`.env` 파일 또는 빌드 환경에서 주입합니다. 모두 선택 사항입니다.

| 변수명 | 설명 | 기본값 |
| --- | --- | --- |
| `VITE_API_BASE_URL` | 백엔드 API 주소 | 빈 값 (Vite proxy 사용) |
| `VITE_WS_BASE_URL` | WebSocket 주소 | 현재 접속 호스트 |
| `VITE_TOXIC_API_URL` | 폭언 감지 API 주소 | `http://127.0.0.1:8000/unsmile` |

URL 쿼리로 `?apiBase=...`, `?wsBase=...` 를 넘기면 환경 변수보다 우선 적용됩니다.

## 디렉터리 구조

```
src/
 ┣ views/          # 라우트 단위 화면 (client, counselor, dashboard, auth)
 ┣ components/     # 화면 구성 컴포넌트
 ┣ composables/    # LiveKit, 통화 연결, 녹음 등 재사용 로직
 ┣ services/       # 백엔드 API 호출
 ┣ stores/         # Pinia 스토어
 ┣ router/
 ┗ utils/
electron/          # Electron 메인 프로세스, AI 서버 실행
```
