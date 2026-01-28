# Composables

Vue 3 Composition API 기반 재사용 가능한 로직들입니다.

## useWebSocket

WebSocket 연결을 관리하는 composable입니다.

### 기본 사용법

```javascript
import { useWebSocket } from '@/composables/useWebSocket'

const { isConnected, connect, disconnect, send } = useWebSocket(
  'ws://localhost:8080/api/v1/notifications',
  {
    onMessage: (data) => {
      console.log('메시지 수신:', data)
    },
    onOpen: () => {
      console.log('WebSocket 연결 성공')
    },
    onClose: (event) => {
      console.log('WebSocket 연결 종료')
    },
    onError: (error) => {
      console.error('WebSocket 에러:', error)
    },
    reconnectDelay: 3000, // 재연결 지연 시간 (ms)
    maxReconnectAttempts: 5 // 최대 재연결 시도 횟수
  }
)

// 연결
connect()

// 메시지 전송
send({ type: 'ping' })

// 연결 해제
disconnect()
```

### 반환값

- `ws`: WebSocket 인스턴스 (ref)
- `isConnected`: 연결 상태 (ref<boolean>)
- `connect()`: WebSocket 연결 함수
- `disconnect()`: WebSocket 연결 해제 함수
- `send(data)`: 메시지 전송 함수

### 옵션

| 옵션 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `onMessage` | Function | - | 메시지 수신 콜백 |
| `onOpen` | Function | - | 연결 성공 콜백 |
| `onClose` | Function | - | 연결 종료 콜백 |
| `onError` | Function | - | 에러 콜백 |
| `reconnectDelay` | Number | 3000 | 재연결 지연 시간 (ms) |
| `maxReconnectAttempts` | Number | 5 | 최대 재연결 시도 횟수 |

### 특징

- **자동 재연결**: 연결이 끊어지면 자동으로 재연결 시도
- **메시지 자동 파싱**: JSON 메시지 자동 파싱
- **자동 정리**: 컴포넌트 언마운트 시 자동 연결 해제
- **에러 핸들링**: 연결 실패 및 메시지 전송 실패 처리

## useAudioStream

LiveKit 기반 오디오 스트림 관리 composable입니다.

## useLiveKit

LiveKit 통화 연결 관리 composable입니다.

## useVoiceCall

음성 통화 통합 관리 composable입니다.
