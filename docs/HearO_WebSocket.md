# HearO 프로젝트 WebSocket 설명

## 1. WebSocket이란? (기본 개념)

```
일반 HTTP (단방향)                  WebSocket (양방향)
┌────────┐    요청   ┌────────┐     ┌────────┐ ←──────→ ┌────────┐
│ Client │ ────────→ │ Server │     │ Client │  실시간  │ Server │
│        │ ←──────── │        │     │        │  양방향  │        │
└────────┘    응답   └────────┘     └────────┘          └────────┘
```

- HTTP: 클라이언트가 요청해야만 서버가 응답
- WebSocket: 서버가 먼저 클라이언트에게 메시지 전송 가능

---

## 2. HearO에서 WebSocket이 필요한 이유

### 문제 상황

- 고객이 대기열에서 기다리는 동안 순위가 실시간으로 변합니다.
- 1번 고객 상담 시작 → 2번 고객이 1번으로 승격 → 3번이 2번으로...

### REST 폴링 방식의 한계

```js
// 5초마다 서버에 물어봄
setInterval(() => {
  fetch('/api/queue/rank')  // "내 순위가 뭐야?"
}, 5000)
- 5초 지연 발생
- 불필요한 요청 (순위 안 바뀌어도 계속 물어봄)
- 서버 부하 증가
```

### WebSocket 방식

```js
// 서버가 변경될 때만 알려줌
socket.on('rank_update', (data) => {
  // "너 순위 3번 됐어!"
  updateUI(data.rank)
})
- 즉시 반영
- 변경 시에만 전송 (효율적)
- 서버가 능동적으로 푸시
```

---

## 3. HearO WebSocket 아키텍처

### 전체 구조

```
┌─────────────────────────────────────────────────────────────────┐
│                        Backend (Spring)                         │
│  ┌─────────────────┐    ┌────────────────-──┐                   │
│  │MatchingScheduler│───→│QueueEventPublisher│                   │
│  │   (5초 주기)    │    │   (메시지 발행)   │                   │
│  └─────────────────┘    └────────┬──────────┘                   │
│                                  │ STOMP                        │
│                    ┌─────────────┴─────────────┐                │
│                    ▼                           ▼                │
│         /topic/queue-rank/{id}      /topic/counselor/{id}       │
└─────────────────────┼──────────────────────────┼────────────────┘
                      │                          │
          ┌───────────▼───────────┐   ┌──────────▼──────────┐
          │   고객 클라이언트     │   │  상담원 클라이언트  │
          │  (ClientWaitingView)  │   │  (DashboardHeader)  │
          └───────────────────────┘   └─────────────────────┘
```

- 사용 기술: STOMP over WebSocket
  - WebSocket = 파이프 (양방향 통신 채널)
  - STOMP = 프로토콜 (메시지 형식과 라우팅 규칙)
  - SockJS = 폴백 (WebSocket 안 되면 HTTP 롱폴링으로 대체)

---

## 4. 기능별 WebSocket 역할

### 4.1 고객 대기열 순위 업데이트

```js
고객 A 상담 시작 (대기열에서 빠짐)
        ↓
Backend: "2번~10번 고객들 순위 1씩 올려야 해"
        ↓
각 고객에게 개별 메시지 전송
        ↓
/topic/queue-rank/customer_2 → { rank: 1 }
/topic/queue-rank/customer_3 → { rank: 2 }
...
```

### 4.2 매칭 완료 알림

```js
MatchingScheduler: 고객 A ↔ 상담원 B 매칭!
        ↓
동시에 두 곳으로 전송:
  → /topic/queue-rank/customer_A     → { status: "MATCHED", roomName: "room_123" }
  → /topic/counselor/counselor_B     → { type: "MATCH_ASSIGNED", roomName: "room_123" }
        ↓
양쪽이 같은 roomName으로 LiveKit 연결
        ↓
통화 시작!
```

### 4.3 메시지 데이터 구조

```js
// 순위 업데이트
{
  "customerId": "customer_123",
  "rank": 3,
  "status": "WAITING",
  "timestamp": 1707123456789
}

// 매칭 완료 (고객용)
{
  "customerId": "customer_123",
  "status": "MATCHED",
  "identity": "customer_123",    // LiveKit 식별자
  "roomName": "room_abc123",     // LiveKit 방 이름
  "timestamp": 1707123456789
}

// 매칭 배정 (상담원용)
{
  "type": "MATCH_ASSIGNED",
  "registrationId": 456,
  "customerId": 123,
  "identity": "counselor_1",
  "roomName": "room_abc123",
  "timestamp": 1707123456789
}
```

---

## 5. 신뢰성 확보 전략 (3단계 폴백)

```
1순위: STOMP WebSocket
    ↓ 실패 시
2순위: Raw WebSocket
    ↓ 실패 시
3순위: REST 폴링 (5초 간격)
```

---

## 6. 발표 QnA 예상 질문과 답변

- Q1: "WebSocket을 왜 사용했나요?"
  - 대기열 순위가 변경될 때 서버가 능동적으로 해당 고객에게만 알려줘야 합니다. REST는 클라이언트가 물어봐야만 응답할 수 있어서 실시간성과 효율성이 떨어집니다.

- Q2: "STOMP는 뭔가요?"
  - WebSocket은 단순히 양방향 파이프일 뿐, 메시지 형식이 없습니다. STOMP는 구독(subscribe)/발행(publish) 패턴을 제공하는 프로토콜입니다. 고객은 자기 토픽(/topic/queue-rank/내ID)만 구독해서 본인 메시지만 받습니다.

- Q3: "서버 확장 시 문제점은?"
  - 현재 Spring Simple Broker(메모리 기반)를 사용합니다. 서버가 2대 이상이면 A서버에서 발행한 메시지를 B서버에 연결된 클라이언트가 못 받습니다. 이 경우 Redis pub/sub 같은 외부 브로커로 교체해야 합니다.

- Q4: "LiveKit도 WebSocket인가요?"
  - LiveKit은 WebRTC 기반입니다. 우리 프로젝트에서 역할이 다릅니다:
    - STOMP WebSocket: 매칭 신호 전달 (방 이름, 참가자 ID)
    - LiveKit(WebRTC): 실제 음성 스트림 전송

- Q5: "유령 고객 문제는 어떻게 해결하나요?"
  - 고객이 브라우저를 닫으면 WebSocket이 끊기지만 대기열에는 남아있습니다. 이를 하트비트 + Redis Lease로 해결합니다:
    - 고객: 10초마다 REST 하트비트 전송
    - 서버: Redis에 40초 TTL로 lease 저장
    - 하트비트 없으면 lease 만료 → 매칭 시 자동 제거
