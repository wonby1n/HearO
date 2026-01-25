<template>
  <Teleport to="body">
    <div :class="['notification-container', position]">
      <TransitionGroup name="notification-list">
        <NotificationItem
          v-for="notification in notifications"
          :key="notification.id"
          :id="notification.id"
          :type="notification.type"
          :message="notification.message"
          :count="notification.count"
          @close="handleClose(notification.id)"
          @click="handleClick"
        />
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import { useNotificationStore } from '@/stores/notification'
import NotificationItem from './NotificationItem.vue'

const props = defineProps({
  position: {
    type: String,
    default: 'top-right',
    validator: (value) => [
      'top-right',
      'top-left',
      'top-center',
      'bottom-right',
      'bottom-left',
      'bottom-center'
    ].includes(value)
  },
  maxVisible: {
    type: Number,
    default: 5
  }
})

const notificationStore = useNotificationStore()

// 최대 표시 개수 제한
const notifications = computed(() => {
  const all = notificationStore.notifications
  return props.maxVisible > 0
    ? all.slice(-props.maxVisible)
    : all
})

const handleClose = (id) => {
  notificationStore.removeNotification(id)
}

const handleClick = (id) => {
  // 클릭 시 추가 동작이 필요하면 여기에 구현
  console.log('Notification clicked:', id)
}
</script>

<style scoped>
.notification-container {
  position: fixed;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 12px;
  pointer-events: none;
}

.notification-container > * {
  pointer-events: auto;
}

/* 위치별 스타일 */
.notification-container.top-right {
  top: 20px;
  right: 20px;
}

.notification-container.top-left {
  top: 20px;
  left: 20px;
}

.notification-container.top-center {
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
}

.notification-container.bottom-right {
  bottom: 20px;
  right: 20px;
  flex-direction: column-reverse;
}

.notification-container.bottom-left {
  bottom: 20px;
  left: 20px;
  flex-direction: column-reverse;
}

.notification-container.bottom-center {
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  flex-direction: column-reverse;
}

/* TransitionGroup 애니메이션 */
.notification-list-enter-active,
.notification-list-leave-active {
  transition: all 0.3s ease;
}

.notification-list-enter-from {
  opacity: 0;
  transform: translateX(100px);
}

.notification-list-leave-to {
  opacity: 0;
  transform: translateX(100px) scale(0.8);
}

.notification-list-move {
  transition: transform 0.3s ease;
}

/* 모바일 반응형 */
@media (max-width: 768px) {
  .notification-container {
    left: 10px !important;
    right: 10px !important;
    transform: none !important;
  }

  .notification-container.top-center,
  .notification-container.bottom-center {
    left: 10px !important;
  }
}
</style>
