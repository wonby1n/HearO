# 📘 03_Signaling — WebRTC 연결 준비 과정

## ✔️ Signaling이란?
WebRTC가 P2P 연결을 만들기 위해 처음에 필요한 정보(SDP, ICE 후보)를 서로 교환하는 과정을 의미한다.

- WebRTC 표준에는 signaling 방식이 포함되어 있지 않음
- 반드시 별도로 구현해야 함 (WebSocket, Firebase, Socket.io 등)
- signaling은 연결 준비까지만 관여하고, 연결 후에는 관여 X

## 🧩 Signaling이 필요한 이유

브라우저 두 개는 처음에 서로의:

- 네트워크 정보(IP, Port)
- 사용 가능한 코덱 정보
- 세션 정보(SDP)
- ICE 후보 정보

를 전혀 알지 못한다.

그래서 직접 연결(P2P)을 하기 전에

- “서로의 신원을 교환하는 중간 메신저”
가 필요해지는 것임.

## 📡 Signaling이 주고받는 정보 (확실한 정보)
### 1️⃣ SDP(세션 설명 정보)

Session Description Protocol

- 어떤 코덱 사용?
- 오디오/비디오 스트림 정보?
- 연결 방식(offer/answer)

이걸 통해 Peer A와 Peer B는
“어떻게 대화할 것인지”를 정의함.

### 2️⃣ ICE Candidate

ICE(Interactive Connectivity Establishment) 후보 정보

- “내가 연결 가능한 모든 네트워크 경로 후보”
- STUN/TURN에서 파악한 IP/Port들

## 🔄 Signaling 동작 흐름

### ✔️ 전체 시퀀스

```less
1. Peer A: createOffer() → offer SDP 생성
2. Peer A → signaling 서버 → Peer B 전달
3. Peer B: setRemoteDescription(offer)
4. Peer B: createAnswer() → answer SDP 생성
5. Peer B → signaling 서버 → Peer A 전달
6. Ice candidate 교환
7. PeerConnection 연결 완료
```

---

## 🛠️ Signaling은 어떤 기술을 사용하는가?

“무조건 WebSocket을 써야 한다”는 규칙 없음.
WebRTC는 signaling 방식에 대해 어떤 것도 강제하지 않는다.

일반적으로 사용되는 방식:


|방식 |	사용 예 | 장점 | 단점 |
| --- | --- | --- | --- |
| WebSocket | 실시간 데이터 |양방향/실시간 | 서버 필요 |
| Socket.io | Node.js 환경 | 이벤트 기반	 | 백엔드 의존 |
| Firebase Realtime DB | 서버리스 | 빠른 개발 | 비용/제한 |
| HTTP Polling | 단순한 시스템 | 구현 쉬움 | 실시간성 낮음 |

---

## ⚡ Signaling 서버가 하는 일 (정확하게)

Signaling 서버는 단 3가지만 한다:


|역할 | 설명 |
| --- | --- |
| Offer/Answer 전달 | Peer A ↔ Peer B 네고 정보 중계 |
| ICE 후보 전달 | STUN/TURN에서 받은 후보 공유 |
| 방 관리(Room) | 동일 세션의 유저 매칭 |

Signaling 서버는 미디어나 데이터를 전송하지 않는다.
오직 연결 준비용 정보만 교환한다.

---

🚫 Signaling 서버로 할 수 없는 일 

- 영상/음성 스트림 전송 ❌
- 데이터 채널 메시지 전송 ❌
- 대역폭 관리 ❌
- 암호화 설정 ❌

이건 모두 브라우저의 RTCPeerConnection이 처리한다.