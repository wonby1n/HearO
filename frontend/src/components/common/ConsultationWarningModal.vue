<template>
  <Transition name="modal-fade">
    <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <!-- 배경 오버레이 -->
      <div class="absolute inset-0 bg-gray-900/40 backdrop-blur-sm" @click="closeModal"></div>

      <!-- 모달 컨텐츠 -->
      <div class="relative w-full max-w-md bg-white rounded-3xl shadow-2xl border-4 border-[#f59e0b]/20 overflow-hidden animate-modal-pop">

        <!-- 상단 배너 (경고 느낌) -->
        <div class="relative bg-gradient-to-br from-[#fef3c7] to-[#fde68a] px-8 py-8 text-center border-b-2 border-[#f59e0b]/30">
          <!-- 배경 패턴 -->
          <div class="absolute inset-0 opacity-5">
            <div class="absolute top-4 right-4 w-24 h-24 border-2 border-[#f59e0b] rounded-full"></div>
            <div class="absolute bottom-4 left-4 w-16 h-16 border-2 border-[#f59e0b] rounded-full"></div>
          </div>

          <!-- 경고 아이콘 -->
          <div class="relative inline-flex items-center justify-center w-20 h-20 bg-white rounded-full shadow-lg mb-4 border-4 border-[#f59e0b]/20">
            <div class="absolute inset-0 bg-[#f59e0b]/10 rounded-full animate-ping"></div>
            <svg class="relative w-10 h-10 text-[#f59e0b]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
            </svg>
          </div>

          <h2 class="relative text-2xl font-bold text-[#92400e]">퇴근 전 한 번 더 체크하셨나요?</h2>
        </div>

        <!-- 메시지 영역 -->
        <div class="px-8 py-8 text-center bg-gradient-to-b from-white to-gray-50">
          <div class="mb-4 inline-flex items-center gap-2 px-4 py-2 bg-[#fef3c7] border border-[#f59e0b]/30 rounded-full">
            <span class="relative flex h-2 w-2">
              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#f59e0b] opacity-75"></span>
              <span class="relative inline-flex rounded-full h-2 w-2 bg-[#f59e0b]"></span>
            </span>
            <span class="text-xs font-bold text-[#92400e] tracking-wide uppercase">상담 모드 활성화 중</span>
          </div>

          <p class="text-gray-700 text-lg font-semibold mb-3">
            상담 모드가 켜져 있습니다
          </p>
          <p class="text-gray-500 text-sm leading-relaxed">
            로그아웃하기 전에<br />
            먼저 <span class="text-[#f59e0b] font-bold">상담 모드를 OFF</span>로 변경해주세요.
          </p>
        </div>

        <!-- 버튼 영역 -->
        <div class="px-8 pb-8 bg-gradient-to-b from-gray-50 to-white">
          <button
            @click="closeModal"
            class="w-full py-4 bg-[#1F3A8C] hover:bg-[#1a3278] text-white text-lg font-bold rounded-xl shadow-lg transition-all active:scale-95"
          >
            확인
          </button>
        </div>

      </div>
    </div>
  </Transition>
</template>

<script setup>
const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

const closeModal = () => {
  emit('close')
}
</script>

<style scoped>
/* 모달 팝업 효과 */
@keyframes modal-pop {
  0% { opacity: 0; transform: scale(0.9) translateY(20px); }
  100% { opacity: 1; transform: scale(1) translateY(0); }
}
.animate-modal-pop {
  animation: modal-pop 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

/* 트랜지션 (Fade) */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
