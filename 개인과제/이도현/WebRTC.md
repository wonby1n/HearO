### WebRTC
- **Web Real-Time Communication**의 약자로, 웹 브라우저에서 **별도 플러그인이나 프로그램 설치 없이** 실시간으로 영상, 음성, 데이터를 주고받을 수 있게 해주는 기술입니다.

### 특징
**1. P2P (Peer-to-Peer) 통신**

- 서버를 거치지 않고 사용자끼리 직접 연결
- 낮은 지연시간 (low latency)
- 서버 부하 감소
    
**2. 브라우저 기반**

- Chrome, Firefox, Safari 등 주요 브라우저 지원
- JavaScript API로 접근 가능
- 별도 앱 설치 불필요

**3. 보안**

- 기본적으로 암호화 (DTLS, SRTP)
- 사용자 동의 필요 (카메라/마이크 권한)

### WebRTC로 주고받을 수 있는 것
**1. MediaStream (미디어 스트림)**

- 음성, 영상 및 텍스트를 포함하는 다양한 미디어 데이터 타입
- live 미디어 (웹 캠 등)
- 저장된(스트리밍) 미디어

**2. RTCDataChannel (데이터 채널)**

- 텍스트 메시지
- 파일 전송
- 게임 상태 정보

### WebRTC의 장단점
**장점**
- 실시간성 (거의 지연 없음)
- 서버 비용 절감 (P2P 통신)
- 웹 기반이라 접근성 좋음
- 화면 공유, 파일 공유 등 다양한 기능

**단점/제약**
- 초기 연결 설정이 복잡함
- 방화벽/NAT 환경에서 연결 어려울 수 있음
- 다수 참여자(10명 이상)는 성능 저하 가능
- P2P 특성상 중계 서버(TURN) 필요할 수 있음

### 동작 원리

**1. 시그널링 (Signaling)**
- **SDP (Session Description Protocol)** 교환
- Offer/Answer 모델로 미디어 정보(코덱, 해상도 등) 협상
- 시그널링 서버는 **중계 역할만** (WebSocket, HTTP 등 자유)

**2. NAT 통과 (NAT Traversal)**
- **ICE (Interactive Connectivity Establishment)** - 최적 연결 경로 탐색
- **STUN** - 공인 IP/포트 확인 (대부분 이걸로 해결)
- **TURN** - P2P 실패 시 릴레이 서버로 우회

**3. P2P 연결**
- **ICE Candidate** 교환 → 연결 가능한 경로들 수집
- **DTLS** - 암호화 핸드셰이크
- **SRTP** - 미디어 암호화 전송

**4. 미디어/데이터 전송**
- **MediaStream** - 오디오/비디오 스트림
- **DataChannel** - 임의 데이터 (파일, 채팅 등)

### 비교
**기존 영상 통화 (서버 중계 방식)**
- 모든 미디어가 **중앙 서버 경유**
- 서버 부하 ↑, 지연 시간 ↑, 비용 ↑
- Skype 초기, 전통적인 VoIP 방식

**WebRTC**
- **P2P 직접 전송** (서버는 시그널링만)
- 낮은 지연, 서버 비용 절감
- 브라우저 네이티브 지원 (플러그인 불필요)


### 참고
[MDN](https://developer.mozilla.org/ko/docs/Web/API/WebRTC_API)
[Adapter.js](https://github.com/webrtcHacks/adapter)
