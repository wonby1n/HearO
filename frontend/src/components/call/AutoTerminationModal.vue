<template>
  <Transition name="modal">
    <div v-if="isVisible" class="modal-overlay">
      <div class="modal-container">
        <!-- 경고 아이콘 -->
        <div class="icon-container">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
        </div>

        <!-- 제목 및 메시지 -->
        <h2 class="modal-title">통화가 자동 종료되었습니다</h2>

        <div class="message-container">
          <p class="main-message">
            고객의 반복적인 폭언으로 인해<br>
            통화가 자동으로 종료되었습니다.
          </p>

          <div class="ai-transfer-notice">
            <svg class="bot-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-5-9h2v2H7v-2zm8 0h2v2h-2v-2zm-4 4h2v2h-2v-2z"/>
            </svg>
            <p class="ai-message">
              고객은 AI 상담사로 자동 전환되었습니다.
            </p>
          </div>

          <div class="info-box">
            <p class="info-text">
              • 해당 고객은 블랙리스트에 등록됩니다<br>
              • 향후 이 고객의 상담은 자동으로 배정되지 않습니다
            </p>
          </div>
        </div>

        <!-- 버튼 -->
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
import { ref, watch } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['close', 'confirm'])

const isVisible = ref(props.show)

watch(() => props.show, (newValue) => {
  isVisible.value = newValue
})

const handleClose = () => {
  emit('close')
}

const handleConfirm = () => {
  emit('confirm')
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}

.modal-container {
  background: white;
  border-radius: 16px;
  padding: 32px;
  max-width: 500px;
  width: 100%;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  animation: modalSlideUp 0.3s ease-out;
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.icon-container {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: #fef2f2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-container svg {
  width: 32px;
  height: 32px;
  color: #dc2626;
}

.modal-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  text-align: center;
  margin: 0 0 24px 0;
}

.message-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 28px;
}

.main-message {
  font-size: 15px;
  color: #4b5563;
  text-align: center;
  line-height: 1.6;
  margin: 0;
}

.ai-transfer-notice {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.bot-icon {
  width: 24px;
  height: 24px;
  color: #3b82f6;
  flex-shrink: 0;
}

.ai-message {
  font-size: 14px;
  font-weight: 600;
  color: #1e40af;
  margin: 0;
}

.info-box {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
}

.info-text {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.7;
  margin: 0;
}

.button-container {
  display: flex;
  justify-content: center;
}

.confirm-button {
  padding: 12px 32px;
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.15s ease;
  min-width: 120px;
}

.confirm-button:hover {
  background: #b91c1c;
}

.confirm-button:active {
  transform: scale(0.98);
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
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.9);
  opacity: 0;
}
</style>
