<template>
  <div
    :class="['notification-item', `notification-${type}`]"
    @click="handleClick"
  >
    <div class="notification-icon">
      <component :is="iconComponent" />
    </div>
    <div class="notification-content">
      <span class="notification-message">{{ message }}</span>
      <span v-if="count !== null" class="notification-count"> : {{ count }}회</span>
    </div>
    <button
      class="notification-close"
      @click.stop="$emit('close')"
      aria-label="알림 닫기"
    >
      ✕
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  id: {
    type: Number,
    required: true
  },
  type: {
    type: String,
    default: 'info',
    validator: (value) => ['profanity', 'abuse', 'warning', 'info', 'success'].includes(value)
  },
  message: {
    type: String,
    required: true
  },
  count: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['close', 'click'])

// 타입별 아이콘 컴포넌트
const iconComponent = computed(() => {
  const icons = {
    profanity: 'AlertTriangleIcon',
    abuse: 'ShieldIcon',
    warning: 'AlertCircleIcon',
    info: 'InfoIcon',
    success: 'CheckCircleIcon'
  }
  return icons[props.type] || 'InfoIcon'
})

const handleClick = () => {
  emit('click', props.id)
}
</script>

<script>
// 간단한 SVG 아이콘 컴포넌트들
export const AlertTriangleIcon = {
  template: `
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
      <line x1="12" y1="9" x2="12" y2="13"/>
      <line x1="12" y1="17" x2="12.01" y2="17"/>
    </svg>
  `
}

export const ShieldIcon = {
  template: `
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
    </svg>
  `
}

export const AlertCircleIcon = {
  template: `
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <circle cx="12" cy="12" r="10"/>
      <line x1="12" y1="8" x2="12" y2="12"/>
      <line x1="12" y1="16" x2="12.01" y2="16"/>
    </svg>
  `
}

export const InfoIcon = {
  template: `
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <circle cx="12" cy="12" r="10"/>
      <line x1="12" y1="16" x2="12" y2="12"/>
      <line x1="12" y1="8" x2="12.01" y2="8"/>
    </svg>
  `
}

export const CheckCircleIcon = {
  template: `
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
      <polyline points="22 4 12 14.01 9 11.01"/>
    </svg>
  `
}
</script>

<style scoped>
.notification-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 280px;
  max-width: 400px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  animation: slideIn 0.3s ease-out;
}

.notification-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 폭언 감지 - 빨간색 + 점멸 애니메이션 */
.notification-profanity {
  background-color: #ff4444;
  color: white;
  animation: slideIn 0.3s ease-out, blink 1s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

/* 비속어 감지 - 파란색 */
.notification-abuse {
  background-color: #4a5eb8;
  color: white;
}

/* 경고 - 주황색 */
.notification-warning {
  background-color: #ff9800;
  color: white;
}

/* 정보 - 회색 */
.notification-info {
  background-color: #607d8b;
  color: white;
}

/* 성공 - 녹색 */
.notification-success {
  background-color: #4caf50;
  color: white;
}

.notification-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-content {
  flex: 1;
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
}

.notification-message {
  white-space: nowrap;
}

.notification-count {
  font-weight: 600;
}

.notification-close {
  flex-shrink: 0;
  background: none;
  border: none;
  color: inherit;
  font-size: 18px;
  cursor: pointer;
  padding: 4px;
  opacity: 0.8;
  transition: opacity 0.2s ease;
  line-height: 1;
}

.notification-close:hover {
  opacity: 1;
}
</style>
