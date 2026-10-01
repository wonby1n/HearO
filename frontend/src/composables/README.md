# Composables

Vue 3 Composition API 기반 재사용 로직입니다.

| 파일 | 설명 | 사용처 |
| --- | --- | --- |
| `useLiveKit.js` | LiveKit room 연결/해제, 오디오 트랙 관리 | `ClientCallView`, `useCallConnection` |
| `useCallConnection.js` | 매칭 수신 → LiveKit 토큰 발급 → 통화 연결 흐름 | `DashboardHeader`, `ClientWaitingView` |
| `useMatchingNotification.js` | 매칭 알림 WebSocket(STOMP) 구독 | `useCallConnection` |
| `useDelayedCustomerAudio.js` | 고객 음성 지연 재생, 폭언 구간 차단 및 삐- 소리 대체 | `CounselorCallView` |
| `useMemoDraft.js` | 통화 메모 localStorage 임시 저장/복구 | `CounselorCallView` |
| `useAudioRecorder.js` | 고객 + 상담원 음성 믹스 녹음 및 파일 다운로드 | `CounselorCallView` |

## useDelayedCustomerAudio

상담원이 듣는 고객 음성을 `delaySec`만큼 늦춰 재생합니다. 그 사이 도착한 고객 STT로 폭언을 판정해,
상담원이 듣기 전에 해당 구간을 음소거하고 삐- 소리로 대체합니다.

```javascript
const customerAudio = useDelayedCustomerAudio({ delaySec: 3, mutePostpadMs: 600 })

await customerAudio.attach(track, participant.identity, containerEl)
customerAudio.blockUntilNextStt(participantId) // 폭언 감지 시
customerAudio.scheduleUnblock(participantId)   // 다음 STT 수신 시
customerAudio.cleanup()                        // unmount 시
```

## useMemoDraft

`callStore.callMemo`를 v-model로 쓰면서 500ms 디바운스로 localStorage에 임시 저장합니다.
서버 저장에 성공하면 `clearMemoDraft()`로 임시본을 지웁니다.

```javascript
const { memo, memoSaveLabel, clearMemoDraft } = useMemoDraft(callStore)
```
