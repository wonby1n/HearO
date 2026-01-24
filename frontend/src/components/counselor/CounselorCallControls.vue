<template>
  <div class="fixed bottom-6 left-1/2 transform -translate-x-1/2 z-40">
    <div class="flex items-center justify-center gap-4 p-4 bg-white rounded-lg shadow-lg">
      <!-- 음소거 버튼 -->
      <button
        @click="toggleMute"
        :class="[
          'flex flex-col items-center justify-center w-16 h-16 rounded-full text-white shadow-lg transition-all duration-200 focus:outline-none focus:ring-4 focus:ring-offset-2 active:scale-95',
          localMuted ? 'bg-yellow-500 hover:bg-yellow-600' : 'bg-gray-600 hover:bg-gray-700'
        ]"
        :title="localMuted ? '음소거 해제' : '음소거'"
      >
        <svg
          v-if="!localMuted"
          class="w-6 h-6"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"
          />
        </svg>
        <svg
          v-else
          class="w-6 h-6"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z M17 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2"
          />
        </svg>
        <span class="text-xs mt-1">{{ localMuted ? '음소거됨' : '음소거' }}</span>
      </button>

      <!-- 통화 종료 버튼 -->
      <button
        @click="handleEndCall"
        class="flex flex-col items-center justify-center w-16 h-16 rounded-full text-white shadow-lg transition-all duration-200 focus:outline-none focus:ring-4 focus:ring-offset-2 active:scale-95 bg-red-600 hover:bg-red-700"
        title="통화 종료"
      >
        <svg
          class="w-6 h-6"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M16 8l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2M5 3a2 2 0 00-2 2v1c0 8.284 6.716 15 15 15h1a2 2 0 002-2v-3.28a1 1 0 00-.684-.948l-4.493-1.498a1 1 0 00-1.21.502l-1.13 2.257a11.042 11.042 0 01-5.516-5.517l2.257-1.128a1 1 0 00.502-1.21L9.228 3.683A1 1 0 008.279 3H5z"
          />
        </svg>
        <span class="text-xs mt-1">종료</span>
      </button>
    </div>

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
import { useCallStore } from '@/stores/call'

const callStore = useCallStore()

// Props 정의
const props = defineProps({
  isMuted: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

// 로컬 음소거 상태 (props와 동기화)
const localMuted = ref(props.isMuted)

// props 변경 시 로컬 상태 동기화
watch(() => props.isMuted, (newVal) => {
  localMuted.value = newVal
})

// 종료 확인 모달 상태
const showConfirmModal = ref(false)

// 음소거 토글
const toggleMute = () => {
  if (props.disabled) return

  localMuted.value = !localMuted.value

  // 이벤트 발생 (부모 컴포넌트에서 LiveKit 처리)
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

  // 통화 종료 처리
  const callData = callStore.endCall()

  // 이벤트 발생 (부모 컴포넌트에서 처리)
  emit('call-ended', callData)
}

// 이벤트 정의
const emit = defineEmits(['mute-changed', 'call-ended'])
</script>

<style scoped>
/* 컴포넌트 스타일 - Tailwind 유틸리티 클래스를 template에서 직접 사용 */
</style>
