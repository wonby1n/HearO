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
    type: [String, Object],
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
  padding: 32px;
  max-width: 800px;
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
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border: 2px solid #1F3A8C;
  border-radius: 12px;
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(31, 58, 140, 0.1);
}

.bot-icon {
  width: 28px;
  height: 28px;
  color: #1F3A8C;
  flex-shrink: 0;
}

.ai-message {
  font-size: 15px;
  font-weight: 600;
  color: #1F3A8C;
  margin: 0;
}

.info-box {
  background: #fef2f2;
  border: 2px solid #fca5a5;
  border-radius: 12px;
  padding: 18px;
}

.info-text {
  font-size: 14px;
  color: #991b1b;
  line-height: 1.7;
  margin: 0;
  font-weight: 500;
}

.button-container {
  display: flex;
  justify-content: center;
}

.confirm-button {
  padding: 14px 36px;
  background: #1F3A8C;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 140px;
  box-shadow: 0 4px 12px rgba(31, 58, 140, 0.3);
}

.confirm-button:hover {
  background: #1a3278;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(31, 58, 140, 0.4);
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
