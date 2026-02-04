<template>
  <Transition name="modal">
    <div v-if="isVisible" class="modal-overlay">
      <div class="modal-container">
        <CallEndModalContent
          title="통화가 종료되었습니다"
          :message="'통화가 정상적으로 종료되었습니다.\n상담 요약과 메모를 확인해 주세요.'"
          :ai-summary="aiSummary"
          :memo="memo"
          @update:memo="emit('update:memo', $event)"
        />

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
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
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
