<template>
  <div class="flex items-center gap-3">
    <!-- 일시정지 버튼 -->
    <button
      @click="togglePause"
      :class="[
        'flex items-center justify-center w-10 h-10 rounded-full transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 active:scale-95',
        localPaused ? 'bg-yellow-500 hover:bg-yellow-600 text-white' : 'bg-gray-800 hover:bg-gray-900 text-white'
      ]"
      :title="localPaused ? '재개' : '일시정지'"
    >
      <svg v-if="!localPaused" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zM7 8a1 1 0 012 0v4a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v4a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd"/>
      </svg>
      <svg v-else class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z" clip-rule="evenodd"/>
      </svg>
    </button>

    <!-- 음소거 버튼 -->
    <button
      @click="toggleMute"
      :class="[
        'flex items-center justify-center w-10 h-10 rounded-full transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 active:scale-95',
        localMuted ? 'bg-yellow-500 hover:bg-yellow-600 text-white' : 'bg-gray-800 hover:bg-gray-900 text-white'
      ]"
      :title="localMuted ? '음소거 해제' : '음소거'"
    >
      <svg v-if="!localMuted" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M7 4a3 3 0 016 0v4a3 3 0 11-6 0V4zm4 10.93A7.001 7.001 0 0017 8a1 1 0 10-2 0A5 5 0 015 8a1 1 0 00-2 0 7.001 7.001 0 006 6.93V17H6a1 1 0 100 2h8a1 1 0 100-2h-3v-2.07z" clip-rule="evenodd"/>
      </svg>
      <svg v-else class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM7 9a1 1 0 000 2h6a1 1 0 100-2H7z" clip-rule="evenodd"/>
      </svg>
    </button>

    <!-- 통화 종료 버튼 -->
    <button
      @click="handleEndCall"
      class="flex items-center justify-center w-10 h-10 rounded-full bg-red-600 hover:bg-red-700 text-white transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 active:scale-95"
      title="통화 종료"
    >
      <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
        <path d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z"/>
        <path d="M16.707 3.293a1 1 0 010 1.414L15.414 6l1.293 1.293a1 1 0 01-1.414 1.414L14 7.414l-1.293 1.293a1 1 0 11-1.414-1.414L12.586 6l-1.293-1.293a1 1 0 011.414-1.414L14 4.586l1.293-1.293a1 1 0 011.414 0z"/>
      </svg>
    </button>

    <!-- 통화 종료 확인 모달 -->
    <Teleport to="body">
      <div
        v-if="showConfirmModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
        @click.self="closeConfirmModal"
      >
        <div class="bg-white rounded-lg shadow-xl p-6 max-w-sm w-full mx-4">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">통화 종료 확인</h3>
          <p class="text-gray-600 mb-6">
            통화를 종료하시겠습니까?<br>
            종료 후 상담 기록 작성 화면으로 이동합니다.
          </p>
          <div class="flex gap-3 justify-end">
            <button
              @click="closeConfirmModal"
              class="px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition"
            >
              취소
            </button>
            <button
              @click="confirmEndCall"
              class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
            >
              종료
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

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

// 로컬 상태 (props와 동기화)
const localMuted = ref(props.isMuted)
const localPaused = ref(props.isPaused)

// props 변경 시 로컬 상태 동기화
watch(() => props.isMuted, (newVal) => {
  localMuted.value = newVal
})

watch(() => props.isPaused, (newVal) => {
  localPaused.value = newVal
})

// 종료 확인 모달 상태
const showConfirmModal = ref(false)

// 일시정지 토글
const togglePause = () => {
  if (props.disabled) return
  localPaused.value = !localPaused.value
  emit('pause-changed', localPaused.value)
}

// 음소거 토글
const toggleMute = () => {
  if (props.disabled) return
  localMuted.value = !localMuted.value
  emit('mute-changed', localMuted.value)
}

// 통화 종료 버튼 클릭
const handleEndCall = () => {
  showConfirmModal.value = true
}

// 모달 닫기
const closeConfirmModal = () => {
  showConfirmModal.value = false
}

// 통화 종료 확인
const confirmEndCall = () => {
  showConfirmModal.value = false
  emit('call-ended')
}

// 이벤트 정의
const emit = defineEmits(['mute-changed', 'pause-changed', 'call-ended'])
</script>

<style scoped>
/* CounselorCallControls 전용 스타일 */
</style>
