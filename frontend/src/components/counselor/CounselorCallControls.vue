<template>
  <div class="flex items-center gap-3">

    <!-- 음소거 버튼 -->
    <button @click="toggleMute" :class="[
      'flex items-center justify-center w-10 h-10 rounded-full transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 active:scale-95',
      localMuted ? 'bg-yellow-500 hover:bg-yellow-600 text-white' : 'bg-gray-800 hover:bg-gray-900 text-white'
    ]" :title="localMuted ? '음소거 해제' : '음소거'">
      <svg v-if="!localMuted" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd"
          d="M7 4a3 3 0 016 0v4a3 3 0 11-6 0V4zm4 10.93A7.001 7.001 0 0017 8a1 1 0 10-2 0A5 5 0 015 8a1 1 0 00-2 0 7.001 7.001 0 006 6.93V17H6a1 1 0 100 2h8a1 1 0 100-2h-3v-2.07z"
          clip-rule="evenodd" />
      </svg>
      <svg v-else class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM7 9a1 1 0 000 2h6a1 1 0 100-2H7z"
          clip-rule="evenodd" />
      </svg>
    </button>

    <!-- 통화 종료 버튼 -->
    <button @click="handleEndCall"
      class="flex items-center justify-center w-10 h-10 rounded-full bg-red-600 hover:bg-red-700 text-white transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 active:scale-95"
      title="통화 종료">
      <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
        <path
          d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z" />
        <path
          d="M16.707 3.293a1 1 0 010 1.414L15.414 6l1.293 1.293a1 1 0 01-1.414 1.414L14 7.414l-1.293 1.293a1 1 0 11-1.414-1.414L12.586 6l-1.293-1.293a1 1 0 011.414-1.414L14 4.586l1.293-1.293a1 1 0 011.414 0z" />
      </svg>
    </button>
  </div>
</template>

<script setup>
import { toRef } from 'vue'

// Props 정의
const props = defineProps({
  isMuted: {
    type: Boolean,
    default: false
  },
  isPaused: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

// 이벤트 정의
const emit = defineEmits(['mute-changed', 'pause-changed', 'call-end-requested'])

// props를 ref로 변환 (반응성 유지)
const localMuted = toRef(props, 'isMuted')
const localPaused = toRef(props, 'isPaused')

// 일시정지 토글
const togglePause = () => {
  if (props.disabled) return
  emit('pause-changed', !localPaused.value)
}

// 음소거 토글
const toggleMute = () => {
  if (props.disabled) return
  emit('mute-changed', !localMuted.value)
}

// 통화 종료 버튼 클릭
const handleEndCall = () => {
  emit('call-end-requested')
}
</script>

<style scoped>
/* CounselorCallControls 전용 스타일 */
</style>
