<template>
  <Transition name="modal-fade">
    <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <!-- 배경 오버레이 (클릭 방지) -->
      <div class="absolute inset-0 bg-gray-900/40 backdrop-blur-sm"></div>

      <!-- 모달 컨텐츠 -->
      <div class="relative w-full max-w-2xl bg-white rounded-[32px] shadow-2xl border-[6px] border-blue-100 overflow-hidden p-12 text-center animate-modal-pop">
        
        <!-- 상단 텍스트 -->
        <h2 class="text-2xl md:text-3xl font-bold text-gray-500 leading-tight mb-12">
          나에게 맞는<br />
          <span class="text-gray-800">고객을 연결했습니다</span>
        </h2>

        <!-- 중앙 비주얼 영역 -->
        <div class="relative flex items-center justify-center py-16">
          
          <!-- 회전하는 점선 궤도 -->
          <div class="absolute w-[340px] h-[340px] border-2 border-dashed border-blue-200 rounded-full animate-orbit">
            <!-- 궤도 위 장식 아이콘들 -->
            <div class="absolute -top-3 left-1/2 -translate-x-1/2 w-6 h-6 text-blue-300">
              <svg fill="currentColor" viewBox="0 0 24 24"><path d="M12 2l2.4 7.2h7.6l-6.2 4.5 2.4 7.2-6.2-4.5-6.2 4.5 2.4-7.2-6.2-4.5h7.6z"/></svg>
            </div>
            <div class="absolute top-1/2 -right-3 -translate-y-1/2 w-4 h-4 text-blue-200">
              <svg fill="currentColor" viewBox="0 0 24 24"><path d="M12 2l2.4 7.2h7.6l-6.2 4.5 2.4 7.2-6.2-4.5-6.2 4.5 2.4-7.2-6.2-4.5h7.6z"/></svg>
            </div>
          </div>

          <!-- 중앙 메인 서클 -->
          <div class="relative z-10 w-48 h-48 bg-[#1F3A8C] rounded-full flex flex-col items-center justify-center shadow-xl shadow-blue-900/30">
            <!-- 로고 아이콘 -->
            <div class="mb-2 text-white/90">
              <svg class="w-12 h-12" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2L4 5v6.09c0 5.05 3.41 9.76 8 10.91 4.59-1.15 8-5.86 8-10.91V5l-8-3zm0 18.5c-3.76-1.08-6.5-5.06-6.5-9.41V6.3l6.5-2.6 6.5 2.6v4.79c0 4.35-2.74 8.33-6.5 9.41z"/>
              </svg>
            </div>
            <p class="text-white text-xl font-bold tracking-tight">분석 완료!</p>
          </div>

          <!-- 플로팅 데이터 레이블들 -->
          <!-- 왼쪽: 스트레스 지수 -->
          <div class="absolute left-[-20px] top-1/2 -translate-y-1/2 bg-white px-5 py-3 rounded-2xl shadow-lg border border-gray-100 animate-float-slow">
            <p class="text-[#1F3A8C] font-bold text-sm">고객과의 궁합</p>
          </div>

          <!-- 오른쪽 상단: 만족도 평가 -->
          <div class="absolute right-0 top-0 bg-white px-5 py-3 rounded-2xl shadow-lg border border-gray-100 animate-float-medium">
            <p class="text-[#1F3A8C] font-bold text-sm">블랙리스트 제외<span class="text-blue-500 uppercase">Good</span></p>
          </div>

          <!-- 오른쪽 하단: 상담 내역 -->
          <div class="absolute right-0 bottom-4 bg-white px-5 py-3 rounded-2xl shadow-lg border border-gray-100 animate-float-fast">
            <p class="text-[#1F3A8C] font-bold text-sm">상담 내역 다수 제품</p>
          </div>
        </div>

        <!-- 하단 확인 버튼 -->
        <button 
          @click="closeModal"
          class="mt-12 w-full max-w-xs bg-[#1F3A8C] hover:bg-[#162a65] text-white text-2xl font-bold py-4 rounded-xl shadow-lg transition-all active:scale-95"
        >
          확인
        </button>

      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref } from 'vue'

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
/**
 * 애니메이션 정의
 */

/* 궤도 회전 */
@keyframes orbit {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.animate-orbit {
  animation: orbit 20s linear infinite;
}

/* 플로팅 효과들 (서로 다른 속도로 생동감 부여) */
@keyframes float {
  0%, 100% { transform: translateY(-50%) translateX(0); }
  50% { transform: translateY(-60%) translateX(5px); }
}
.animate-float-slow {
  animation: float 4s ease-in-out infinite;
}

@keyframes float-diag {
  0%, 100% { transform: translateY(0) translateX(0); }
  50% { transform: translateY(-10px) translateX(-5px); }
}
.animate-float-medium {
  animation: float-diag 3s ease-in-out infinite;
}

@keyframes float-simple {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(10px); }
}
.animate-float-fast {
  animation: float-simple 3.5s ease-in-out infinite;
}

/* 모달 팝업 효과 */
@keyframes modal-pop {
  0% { opacity: 0; transform: scale(0.9) translateY(20px); }
  100% { opacity: 1; transform: scale(1) translateY(0); }
}
.animate-modal-pop {
  animation: modal-pop 0.5s cubic-bezier(0.16, 1, 0.3, 1) forwards;
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