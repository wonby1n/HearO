<template>
  <Transition name="modal">
    <div v-if="isVisible" class="modal-overlay">
      <div class="modal-container">
        <CallEndModalContent
          title="통화가 자동 종료되었습니다"
          :message="'고객의 반복적인 폭언으로 인해\n통화가 자동으로 종료되었습니다.'"
          :ai-summary="aiSummary"
          :memo="memo"
          @update:memo="emit('update:memo', $event)"
        >
          <template #extra>
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
          </template>
        </CallEndModalContent>

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
import CallEndModalContent from '@/components/call/CallEndModalContent.vue'

const props = defineProps({
  show: {
    type: Boolean,
    required: true
  },
  aiSummary: {
    type: String,
    default: ''
  },
  memo: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['close', 'confirm', 'update:memo'])

const isVisible = ref(props.show)

watch(() => props.show, (newValue) => {
  isVisible.value = newValue
})


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
  border-radius: 20px;
  padding: 28px;
  max-width: 520px;
  width: 100%;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  animation: modalSlideUp 0.3s ease-out;
  max-height: 80vh;
  overflow-y: auto;
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
  padding: 14px 36px;
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 140px;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
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
