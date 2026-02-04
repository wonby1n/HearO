<template>
  <div :class="['notification-item', `notification-${type}`]">
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

// 타입별 아이콘 매핑 (성능 최적화: 외부 상수로 선언)
const ICON_MAP = {
  profanity: 'AlertTriangleIcon',
  abuse: 'ShieldIcon',
  warning: 'AlertCircleIcon',
  info: 'InfoIcon',
  success: 'CheckCircleIcon'
}

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

defineEmits(['close'])

// 타입별 아이콘 컴포넌트
const iconComponent = computed(() => ICON_MAP[props.type] || 'InfoIcon')
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
  padding: 14px 18px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2), 0 2px 8px rgba(0, 0, 0, 0.1);
  min-width: 300px;
  max-width: 400px;
  backdrop-filter: blur(8px);
}

/* 폭언 감지 - 빨간색 그라데이션 + 진동 애니메이션 */
.notification-profanity {
  background: linear-gradient(135deg, #ff3b3b 0%, #ff5252 100%);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
  animation: profanity-pulse 1.5s ease-in-out infinite;
}

@keyframes profanity-pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 8px 24px rgba(255, 59, 59, 0.4), 0 2px 8px rgba(255, 59, 59, 0.2);
  }
  50% {
    transform: scale(1.02);
    box-shadow: 0 12px 32px rgba(255, 59, 59, 0.5), 0 4px 12px rgba(255, 59, 59, 0.3);
  }
}

/* 비속어 감지 - 파란색 그라데이션 */
.notification-abuse {
  background: linear-gradient(135deg, #4a5eb8 0%, #5d6bc0 100%);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 경고 - 주황색 그라데이션 */
.notification-warning {
  background: linear-gradient(135deg, #ff9800 0%, #ffa726 100%);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 정보 - 블루 그라데이션 */
.notification-info {
  background: linear-gradient(135deg, #2196f3 0%, #42a5f5 100%);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 성공 - 녹색 그라데이션 */
.notification-success {
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
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

/* 모바일 반응형 */
@media (max-width: 768px) {
  .notification-item {
    min-width: 240px;
    max-width: calc(100vw - 40px);
    padding: 10px 12px;
  }

  .notification-content {
    font-size: 13px;
  }

  .notification-icon svg {
    width: 18px;
    height: 18px;
  }
}
</style>
