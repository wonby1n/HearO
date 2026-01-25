# 알림 시스템 (Notification System)

상담 중 발생하는 폭언 감지, 비속어 감지 등의 실시간 알림을 표시하는 독립적인 모듈입니다.

## 📁 구조

```
components/notification/
├── NotificationContainer.vue   # 알림 컨테이너 (화면에 알림 표시)
├── NotificationItem.vue        # 개별 알림 UI
├── NotificationExample.vue     # 사용 예제 컴포넌트
└── README.md                   # 이 문서

stores/
└── notification.js             # 알림 상태 관리 (Pinia)
```

## 🚀 사용 방법

### 1. 기본 설정 (이미 완료됨)

`App.vue`에 `NotificationContainer`가 이미 추가되어 있습니다.

```vue
<template>
  <RouterView />
  <NotificationContainer position="top-right" />
</template>
```

### 2. 알림 표시하기

어떤 컴포넌트에서든 store를 import하여 알림을 표시할 수 있습니다.

```vue
<script setup>
import { useNotificationStore } from '@/stores/notification'
import { useCallStore } from '@/stores/call'

const notificationStore = useNotificationStore()
const callStore = useCallStore()

// 폭언 감지 시
const handleProfanityDetected = () => {
  const count = callStore.incrementProfanityCount()
  notificationStore.notifyProfanity(count)
}

// 비속어 감지 시
const handleAbuseDetected = () => {
  notificationStore.notifyAbuse()
}

// 커스텀 경고
const showCustomWarning = () => {
  notificationStore.notifyWarning('고객님의 목소리가 들리지 않습니다')
}
</script>
```

### 3. API Reference

#### 📌 Notification Store 메서드

##### 미리 정의된 알림 타입

```javascript
// 폭언 감지 알림
notificationStore.notifyProfanity(count)
// 예: notifyProfanity(3) → "폭언 감지 : 3회" 표시

// 비속어 감지 알림
notificationStore.notifyAbuse()
// → "비속어 등 경험이" 표시

// 일반 경고
notificationStore.notifyWarning(message)
// 예: notifyWarning('연결이 불안정합니다')

// 정보 알림
notificationStore.notifyInfo(message)
// 예: notifyInfo('녹음이 시작되었습니다')

// 성공 알림
notificationStore.notifySuccess(message)
// 예: notifySuccess('상담이 저장되었습니다')
```

##### 커스텀 알림

```javascript
notificationStore.addNotification({
  type: 'profanity' | 'abuse' | 'warning' | 'info' | 'success',
  message: '표시할 메시지',
  duration: 3000,  // ms (0이면 자동 제거 안 함)
  count: 5         // 횟수 표시 (선택)
})
```

##### 알림 제거

```javascript
// 특정 알림 제거
const notificationId = notificationStore.notifyInfo('테스트')
notificationStore.removeNotification(notificationId)

// 모든 알림 제거
notificationStore.clearAllNotifications()
```

## 🎨 알림 타입별 스타일

| 타입 | 색상 | 용도 | 아이콘 |
|------|------|------|--------|
| `profanity` | 빨강 (#ff4444) | 폭언 감지 | ⚠️ 삼각형 |
| `abuse` | 파랑 (#4a5eb8) | 비속어 감지 | 🛡️ 방패 |
| `warning` | 주황 (#ff9800) | 일반 경고 | ⚠️ 원형 |
| `info` | 회색 (#607d8b) | 정보 | ℹ️ |
| `success` | 녹색 (#4caf50) | 성공 | ✓ |

## 🔧 커스터마이징

### 위치 변경

```vue
<!-- 기본: 우측 상단 -->
<NotificationContainer position="top-right" />

<!-- 다른 위치 옵션 -->
<NotificationContainer position="top-left" />
<NotificationContainer position="top-center" />
<NotificationContainer position="bottom-right" />
<NotificationContainer position="bottom-left" />
<NotificationContainer position="bottom-center" />
```

### 최대 표시 개수 제한

```vue
<!-- 최대 3개만 표시 -->
<NotificationContainer :max-visible="3" />

<!-- 무제한 -->
<NotificationContainer :max-visible="0" />
```

## 📝 실제 사용 시나리오

### 상담사 화면에서 STT 결과와 연동

```vue
<script setup>
import { useNotificationStore } from '@/stores/notification'
import { useCallStore } from '@/stores/call'

const notificationStore = useNotificationStore()
const callStore = useCallStore()

// STT 결과 처리 함수
const handleSTTResult = (result) => {
  // AI가 폭언 감지했을 때
  if (result.isProfanity) {
    const count = callStore.incrementProfanityCount()
    notificationStore.notifyProfanity(count)
  }

  // AI가 비속어 감지했을 때
  if (result.hasAbusiveWords) {
    notificationStore.notifyAbuse()
  }
}

// WebSocket이나 LiveKit 이벤트에서 호출
websocket.on('stt-result', handleSTTResult)
</script>
```

### 통화 상태 변화 알림

```vue
<script setup>
const notificationStore = useNotificationStore()

// 통화 연결 시
const onCallConnected = () => {
  notificationStore.notifySuccess('통화가 연결되었습니다')
}

// 통화 종료 시
const onCallEnded = () => {
  notificationStore.notifyInfo('통화가 종료되었습니다')
}

// 연결 오류 시
const onConnectionError = (error) => {
  notificationStore.notifyWarning('연결에 실패했습니다')
}
</script>
```

## 🧪 테스트하기

테스트 컴포넌트가 제공됩니다.

```vue
<!-- 라우터에 추가 -->
{
  path: '/notification-test',
  component: () => import('@/components/notification/NotificationExample.vue')
}
```

브라우저에서 `/notification-test` 경로로 이동하여 각 알림 타입을 테스트할 수 있습니다.

## 🔗 다른 팀원 코드와 통합

### 상담사 화면 통합 예시

다른 팀원이 만든 `CounselorCallView.vue`에서:

```vue
<script setup>
import { useNotificationStore } from '@/stores/notification'

const notificationStore = useNotificationStore()

// 어디서든 호출 가능
function onProfanityDetected() {
  notificationStore.notifyProfanity(1)
}
</script>
```

**중요**: `App.vue`에 `NotificationContainer`가 이미 있으므로, 다른 화면에서는 **store 메서드만 호출**하면 됩니다!

## 📱 반응형

모바일 화면에서는 자동으로 좌우 여백이 조정됩니다 (10px).

## ⚡ 성능

- 알림은 자동으로 3초 후 제거됩니다 (duration 설정 가능)
- 최대 5개까지만 표시됩니다 (maxVisible 설정 가능)
- TransitionGroup을 사용하여 부드러운 애니메이션 제공

## 🐛 문제 해결

### 알림이 표시되지 않을 때

1. `App.vue`에 `NotificationContainer`가 추가되어 있는지 확인
2. store import 경로가 올바른지 확인
3. 브라우저 콘솔에서 에러 메시지 확인

### 알림이 겹칠 때

```vue
<!-- z-index가 낮은 경우 position 조정 -->
<NotificationContainer position="top-right" />
```

알림의 z-index는 9999로 설정되어 있어 대부분의 요소보다 위에 표시됩니다.
