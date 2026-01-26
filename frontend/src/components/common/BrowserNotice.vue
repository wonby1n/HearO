<template>
  <div v-if="!isChrome && modelValue" class="browser-notice">
    <div class="notice-content">
      <svg class="notice-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
        <path d="M12 8V12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        <circle cx="12" cy="16" r="1" fill="currentColor"/>
      </svg>
      <span class="notice-text">Chrome 브라우저에서 더 원활한 상담이 가능합니다</span>
      <button
        class="notice-close"
        @click="$emit('update:modelValue', false)"
        aria-label="알림 닫기"
      >
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

defineProps({
  modelValue: {
    type: Boolean,
    default: true
  }
})

defineEmits(['update:modelValue'])

const isChrome = ref(false)

const checkBrowser = () => {
  const userAgent = navigator.userAgent.toLowerCase()
  // Chrome, Edge (Chromium 기반) 모두 허용
  isChrome.value = /chrome|crios|edg/.test(userAgent) && !/opr|opera/.test(userAgent)
}

onMounted(() => {
  checkBrowser()
})
</script>

<style scoped>
.browser-notice {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: rgba(255, 193, 7, 0.95);
  backdrop-filter: blur(10px);
  z-index: 1000;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
  }
  to {
    transform: translateY(0);
  }
}

.notice-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px 20px;
  max-width: 768px;
  margin: 0 auto;
}

.notice-icon {
  width: 20px;
  height: 20px;
  color: #000;
  flex-shrink: 0;
}

.notice-text {
  font-size: 14px;
  font-weight: 600;
  color: #000;
  flex: 1;
  text-align: center;
}

.notice-close {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #000;
  opacity: 0.7;
  transition: opacity 0.2s;
  flex-shrink: 0;
}

.notice-close:hover {
  opacity: 1;
}

.notice-close svg {
  width: 18px;
  height: 18px;
}
</style>
