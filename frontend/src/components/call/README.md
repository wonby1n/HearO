# Call 컴포넌트

## CallControls.vue

통화 중 사용하는 컨트롤 버튼 컴포넌트 (음소거, 종료)

### 기능

- **음소거 버튼**: 마이크 음소거/해제 토글
- **종료 버튼**: 통화 종료 (확인 모달 포함)

### 사용법

```vue
<template>
  <CallControls
    @mute-changed="handleMuteChanged"
    @call-ended="handleCallEnded"
  />
</template>

<script setup>
import CallControls from '@/components/call/CallControls.vue'

const handleMuteChanged = (isMuted) => {
  console.log('음소거 상태:', isMuted)
}

const handleCallEnded = (callData) => {
  console.log('통화 종료 데이터:', callData)
  // callData: { callId, duration, transcripts, memo, profanityCount }
}
</script>
```

### Props

없음 (Pinia store를 통해 상태 관리)

### Emits

- `mute-changed`: 음소거 상태 변경 시 발생
  - 파라미터: `isMuted` (boolean)

- `call-ended`: 통화 종료 확인 시 발생
  - 파라미터: `callData` (object)
    ```js
    {
      callId: string,
      duration: number,      // 초 단위
      transcripts: array,    // STT 텍스트 배열
      memo: string,          // 통화 메모
      profanityCount: number // 욕설 카운트
    }
    ```

### 스타일

- 화면 하단 중앙에 고정 (fixed position)
- 버튼 클릭 시 스케일 애니메이션
- 음소거 상태에 따라 버튼 색상 변경
- 종료 확인 모달 (Teleport 사용)

### 의존성

- Pinia store: `useCallStore`
- TailwindCSS
- Vue 3 Composition API

### 주의사항

- LiveKit 오디오 트랙이 설정되어 있어야 음소거 기능이 정상 작동합니다
- 통화 종료 시 `callStore.endCall()`이 호출되므로 상담 기록 페이지로 라우팅해야 합니다
