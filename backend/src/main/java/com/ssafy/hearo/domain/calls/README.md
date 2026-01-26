
---

## 1. 시스템 개요

### 역할 분리

| 구성요소                  | 역할                         |
| --------------------- | -------------------------- |
| Backend (Spring Boot) | LiveKit 토큰(JWT) 생성 및 반환    |
| LiveKit Cloud         | 실제 WebRTC 미디어 서버           |
| Client (Web/App)      | token + url로 LiveKit 서버 연결 |

---

## 2. 전체 흐름

```
Client
  └─ POST /api/v1/calls/token (identity, roomName)
        ↓
Backend
  └─ LiveKit API Key/Secret으로 JWT 생성
        ↓
Backend
  └─ token + LiveKit Cloud url 응답
        ↓
Client
  └─ room.connect(url, token)
        ↓
LiveKit Cloud
```

---


## 3. API 명세

### 3.1 토큰 발급 API

**URL**

```
POST /api/v1/calls/token
```

**Request Body**

```json
{
  "identity": "user123",
  "roomName": "roomA"
}
```

| 필드       | 설명                  |
| -------- | ------------------- |
| identity | LiveKit 참가자 식별자     |
| roomName | 참가할 LiveKit room 이름 |

---

**Response (200 OK)**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "url": "wss://YOUR_PROJECT.livekit.cloud"
}
```

| 필드    | 설명                         |
| ----- | -------------------------- |
| token | LiveKit JWT 토큰             |
| url   | LiveKit Cloud WebSocket 주소 |


---

## 4. 토큰 생성 로직

### 핵심 개념

* `RoomJoin(true)`
  → **방에 입장할 수 있는 권한**
* `RoomName(roomName)`
  → **입장 가능한 room 지정**
* `TTL`
  → 토큰 유효시간 (밀리초)

### 예시 코드

```java
AccessToken token = new AccessToken(API_KEY, API_SECRET);

token.setIdentity(identity);

// 토큰 유효시간 (예: 30분)
token.setTtl(Duration.ofMinutes(30).toMillis());

token.addGrants(
    new RoomJoin(true),
    new RoomName(roomName)
);

return token.toJwt();
```

---

## 5. 로컬 테스트 방법 (Postman)

### 요청

* Method: `POST`
* URL: `http://localhost:8080/api/v1/calls/token`
* Headers:

  ```
  Content-Type: application/json
  ```
* Body:

  ```json
  {
    "identity": "test-user",
    "roomName": "test-room"
  }
  ```

### 기대 결과

* Status: `200 OK`
* Response에 `token`, `url` 포함

---


