# TIL: Vue로 WebRTC 기본 구현 흐름 (1:1)

> 목표: **Vue 프론트에서 WebRTC 1:1 영상통화가 동작**하도록 “표시(화면)” + “연결(시그널링)” + “미디어(카메라/마이크)” 흐름을 이해하고 최소 구현 구조를 잡는다.

---

## 1) WebRTC 구현에 필요한 큰 구성

WebRTC가 동작하려면 프론트에서 아래 3가지를 **각자 역할대로** 준비해야 한다.

1. **UI(화면 표시)**: 내 영상 / 상대 영상이 보여질 `<video>` 영역
2. **Media(미디어 캡처)**: `getUserMedia()`로 카메라/마이크 스트림 확보
3. **Connection(연결)**: `RTCPeerConnection`으로 P2P 연결 구성
4. **Signaling(시그널링)**: SDP/ICE 정보를 “서로” 전달하는 서버(WebSocket 등)

> ⚠️ WebRTC 자체에는 시그널링이 포함되지 않는다. 즉, **SDP/ICE를 전달할 통로(서버)**는 반드시 별도로 구현해야 한다.

---

## 2) 프론트(UI)는 어떻게 표시하나?

### 2-1. 최소 UI 구성

* `localVideo`: 내 카메라 화면
* `remoteVideo`: 상대 화면
* 버튼(테스트용): 카메라 시작 / 통화 걸기(offer) / 받기(answer) / 끊기

```vue
<template>
  <div>
    <h2>WebRTC 기본 (Vue)</h2>

    <div style="display:flex; gap:16px;">
      <div>
        <p>내 화면</p>
        <video ref="localVideo" autoplay playsinline muted></video>
      </div>

      <div>
        <p>상대 화면</p>
        <video ref="remoteVideo" autoplay playsinline></video>
      </div>
    </div>

    <div style="margin-top:16px; display:flex; gap:8px;">
      <button @click="startCamera">Start Camera</button>
      <button @click="call">Call (Offer)</button>
      <button @click="hangup">Hangup</button>
    </div>

    <p style="margin-top:12px;">status: {{ status }}</p>
  </div>
</template>
```

### 2-2. video 태그 필수 포인트

* `autoplay playsinline`: 모바일/브라우저에서 자동재생/인라인 재생
* `muted`: 내 로컬 영상은 **에코 방지** + 브라우저 자동재생 정책 대응

---

## 3) Media: 내 카메라/마이크를 가져오고 화면에 붙이기

### 3-1. 핵심 API

* `navigator.mediaDevices.getUserMedia({ video: true, audio: true })`
* 얻은 `MediaStream`을 `localVideo.srcObject = stream`으로 연결

---

## 4) Connection: RTCPeerConnection에 트랙 붙이기

### 4-1. PeerConnection 생성

* ICE 서버(STUN/TURN) 정보를 넣어 네트워크 연결 성공률을 올림

> 예시: STUN만 넣은 최소 구성 (TURN은 필요 시 추가)

```js
const pc = new RTCPeerConnection({
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' }
  ]
})
```

### 4-2. 내 스트림을 PC에 추가

* `stream.getTracks()`로 오디오/비디오 트랙을 가져와 `pc.addTrack(track, stream)`

---

## 5) 상대 영상은 어떻게 화면에 뜨나?

### 5-1. 상대 트랙 수신 이벤트 처리

* 상대가 보내는 트랙이 들어오면 `pc.ontrack`이 호출됨
* `remoteVideo.srcObject`에 상대 `MediaStream`을 연결

---

## 6) Signaling: SDP/ICE는 어떻게 주고받나?

### 6-1. WebRTC 연결에 필요한 교환 데이터 2가지

1. **SDP(offer/answer)**: “어떤 코덱/해상도/오디오 등으로 통화할지” 협상 정보
2. **ICE candidate**: “실제로 통신 가능한 네트워크 경로 후보들”

### 6-2. 왜 서버가 필요하지?

브라우저 A와 B는 처음엔 서로 주소/경로를 모른다.
그래서 A가 만든 offer, ICE 후보를 B에게 전달해야 하고,
B가 만든 answer, ICE 후보도 A에게 전달해야 한다.

➡️ 이 전달 통로가 **Signaling Server(WebSocket, Socket.IO 등)**

---

## 7) 기본 연결 순서(핵심 플로우)

### 7-1. Caller(통화 거는 쪽)

1. 카메라 시작(getUserMedia)
2. RTCPeerConnection 생성
3. 트랙 추가(addTrack)
4. `createOffer()` → `setLocalDescription(offer)`
5. offer를 시그널링 서버로 전송
6. 상대 answer 수신 → `setRemoteDescription(answer)`
7. ICE 후보 생성 시마다 서버로 전송

### 7-2. Callee(통화 받는 쪽)

1. offer 수신
2. 카메라 시작(getUserMedia)
3. RTCPeerConnection 생성
4. 트랙 추가(addTrack)
5. `setRemoteDescription(offer)`
6. `createAnswer()` → `setLocalDescription(answer)`
7. answer를 서버로 전송
8. ICE 후보 교환

---

## 8) Vue 구현 시 주의점

* `ref`로 video 엘리먼트를 잡고 `srcObject`를 넣어야 함
* 컴포넌트 unmount 시 **트랙/연결 정리** 필요

  * 트랙 stop: `track.stop()`
  * 연결 종료: `pc.close()`
* 음성 에코 방지

  * 로컬 video는 `muted`

---

## 9) 최소 구현 체크리스트

*  localVideo / remoteVideo 영역이 화면에 존재
*  Start Camera 눌렀을 때 내 화면이 즉시 나온다
*  offer/answer 교환이 된다(시그널링 서버 연결)
*  ICE 후보 교환이 된다
*  연결되면 remoteVideo에 상대 화면이 뜬다
*  Hangup 시 리소스가 정리된다

---

## 10) 오늘 학습 결론

* WebRTC 프론트 구현은 **UI + Media + PeerConnection + Signaling** 4개가 한 세트다.
* STUN/TURN은 연결 성공률을 높이는 인프라이고,
* OpenVidu는 이 모든 과정을 플랫폼 형태로 제공하는 선택지다.

> 다음 할 일: 시그널링 서버(WebSocket) 최소 구현 + roomId 기반으로 1:1 매칭 붙이기
