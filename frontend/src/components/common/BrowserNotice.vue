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
        class="open-chrome-btn"
        @click="openInChrome"
      >
        크롬에서 열기
      </button>
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

    <!-- URL 복사 완료 토스트 -->
    <transition name="toast">
      <div v-if="showCopyToast" class="copy-toast">
        {{ toastMessage }}
      </div>
    </transition>
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
const showCopyToast = ref(false)
const toastMessage = ref('')

const checkBrowser = () => {
  const userAgent = navigator.userAgent.toLowerCase()
  // Chrome, Edge (Chromium 기반) 모두 허용
  isChrome.value = /chrome|crios|edg/.test(userAgent) && !/opr|opera/.test(userAgent)
}

const getDeviceType = () => {
  const userAgent = navigator.userAgent.toLowerCase()
  if (/iphone|ipad|ipod/.test(userAgent)) return 'ios'
  if (/android/.test(userAgent)) return 'android'
  return 'other'
}

const showToast = (message) => {
  toastMessage.value = message
  showCopyToast.value = true
  setTimeout(() => {
    showCopyToast.value = false
  }, 3000)
}

const copyToClipboard = async () => {
  try {
    await navigator.clipboard.writeText(window.location.href)
    showToast('URL이 복사되었습니다. Chrome에서 붙여넣기 해주세요.')
  } catch {
    // Fallback for older browsers
    const textArea = document.createElement('textarea')
    textArea.value = window.location.href
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    showToast('URL이 복사되었습니다. Chrome에서 붙여넣기 해주세요.')
  }
}

const openInChrome = async () => {
  const deviceType = getDeviceType()
  const currentUrl = window.location.href

  if (deviceType === 'ios') {
    // iOS: googlechromes:// 스킴 사용 (https -> googlechromes)
    const chromeUrl = currentUrl.replace(/^https?:\/\//, 'googlechromes://')

    // Chrome 앱 열기 시도
    const startTime = Date.now()
    window.location.href = chromeUrl

    // 앱이 열리지 않으면 (일정 시간 후에도 페이지에 있으면) URL 복사
    setTimeout(() => {
      if (Date.now() - startTime < 2000) {
        copyToClipboard()
      }
    }, 1500)
  } else if (deviceType === 'android') {
    // Android: intent:// 스킴 사용
    const intentUrl = `intent://${currentUrl.replace(/^https?:\/\//, '')}#Intent;scheme=https;package=com.android.chrome;end`

    // Chrome 앱 열기 시도
    const startTime = Date.now()
    window.location.href = intentUrl

    // 앱이 열리지 않으면 URL 복사
    setTimeout(() => {
      if (Date.now() - startTime < 2000) {
        copyToClipboard()
      }
    }, 1500)
  } else {
    // 데스크탑 등: URL 복사만
    copyToClipboard()
  }
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

.open-chrome-btn {
  background: #000;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s;
}

.open-chrome-btn:hover {
  background: #333;
}

.open-chrome-btn:active {
  background: #555;
}

/* 토스트 메시지 */
.copy-toast {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.85);
  color: #fff;
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 14px;
  z-index: 1001;
  text-align: center;
  max-width: 90%;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(20px);
}
</style>
