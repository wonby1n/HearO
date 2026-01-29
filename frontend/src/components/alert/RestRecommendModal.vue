<template>
  <Transition name="modal">
    <div v-if="isVisible" class="modal-overlay" @click="handleOverlayClick">
      <div class="modal-container" @click.stop>
        <!-- 제목 -->
        <h2 class="modal-title">에너지 지수가 낮아요</h2>

        <!-- 펄스 애니메이션 원형 -->
        <div class="pulse-container">
          <!-- 퍼져나가는 원형선들 -->
          <div class="pulse-ring pulse-ring-1"></div>
          <div class="pulse-ring pulse-ring-2"></div>

          <!-- 중앙 녹색 원형과 번개 아이콘 -->
          <div class="energy-icon">
            <svg class="lightning-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M13 2L3 14h8l-1 8 10-12h-8l1-8z"/>
            </svg>
          </div>
        </div>

        <!-- 메시지 -->
        <p class="modal-message">휴식이 필요해요</p>

        <!-- 확인 버튼 -->
        <div class="button-container">
          <button
            type="button"
            class="confirm-button"
            @click="handleConfirm"
          >
            확인
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['close', 'confirm'])

const isVisible = computed(() => props.show)

const handleConfirm = () => {
  emit('confirm')
}

const handleOverlayClick = () => {
  emit('close')
}
</script>

<style scoped>
/* CSS 변수 정의 */
:root {
  --shadow-modal: 0 20px 25px -5px rgba(0, 0, 0, 0.3), 0 10px 10px -5px rgba(0, 0, 0, 0.2);
  --shadow-icon: 0 10px 30px rgba(129, 140, 248, 0.4), 0 0 0 8px rgba(129, 140, 248, 0.2);
  --shadow-button: 0 4px 12px rgba(31, 58, 140, 0.3);
  --shadow-button-hover: 0 6px 16px rgba(31, 58, 140, 0.4);
  --shadow-button-active: 0 2px 8px rgba(31, 58, 140, 0.3);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}

.modal-container {
  background: #2d3748;
  border-radius: 24px;
  padding: 48px 40px;
  max-width: 480px;
  width: 100%;
  box-shadow: var(--shadow-modal);
  animation: modalSlideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-title {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  margin: 0;
  text-align: center;
}

/* 펄스 컨테이너 */
.pulse-container {
  position: relative;
  width: 280px;
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 퍼져나가는 원형선 애니메이션 */
.pulse-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid #818cf8;
  border-radius: 50%;
  opacity: 0;
  animation: pulse 2.5s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

.pulse-ring-1 {
  animation-delay: 0s;
}

.pulse-ring-2 {
  animation-delay: 1.25s;
}

@keyframes pulse {
  0% {
    transform: scale(0.5);
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
  100% {
    transform: scale(1.2);
    opacity: 0;
  }
}

/* 중앙 navy 원형 */
.energy-icon {
  position: relative;
  width: 160px;
  height: 160px;
  background: linear-gradient(135deg, #818cf8 0%, #3d5abe 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-icon);
  z-index: 1;
}

/* 번개 아이콘 */
.lightning-icon {
  width: 80px;
  height: 80px;
  color: #e0e7ff;
  filter: drop-shadow(0 2px 8px rgba(224, 231, 255, 0.5));
}

.modal-message {
  font-size: 20px;
  font-weight: 600;
  color: #e2e8f0;
  margin: 0;
  text-align: center;
}

.button-container {
  display: flex;
  justify-content: center;
  width: 100%;
  margin-top: 8px;
}

.confirm-button {
  padding: 14px 48px;
  background: #1F3A8C;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 140px;
  box-shadow: var(--shadow-button);
}

.confirm-button:hover {
  background: #1a3278;
  transform: translateY(-2px);
  box-shadow: var(--shadow-button-hover);
}

.confirm-button:active {
  transform: translateY(0);
  box-shadow: var(--shadow-button-active);
}

/* 모달 트랜지션 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-container,
.modal-leave-active .modal-container {
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.3s ease;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.9) translateY(20px);
  opacity: 0;
}
</style>
