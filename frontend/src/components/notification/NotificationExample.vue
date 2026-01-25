<template>
  <div class="notification-example">
    <h2>알림 시스템 테스트</h2>
    <p class="description">
      이 컴포넌트는 알림 시스템의 사용 예제입니다.<br />
      실제 화면에서는 필요한 곳에서 store의 메서드를 호출하면 됩니다.
    </p>

    <div class="button-group">
      <button @click="testProfanity" class="btn btn-danger">
        폭언 감지 알림
      </button>
      <button @click="testAbuse" class="btn btn-primary">
        비속어 감지 알림
      </button>
      <button @click="testWarning" class="btn btn-warning">
        경고 알림
      </button>
      <button @click="testInfo" class="btn btn-info">
        정보 알림
      </button>
      <button @click="testSuccess" class="btn btn-success">
        성공 알림
      </button>
      <button @click="clearAll" class="btn btn-secondary">
        모두 지우기
      </button>
    </div>

    <div class="code-example">
      <h3>사용 방법</h3>
      <pre><code>// 1. Store import
import { useNotificationStore } from '@/stores/notification'

// 2. setup에서 초기화
const notificationStore = useNotificationStore()

// 3. 폭언 감지 시 (예: call store와 연동)
const handleProfanityDetected = () => {
  const count = callStore.incrementProfanityCount()
  notificationStore.notifyProfanity(count)
}

// 4. 비속어 감지 시
notificationStore.notifyAbuse()

// 5. 커스텀 알림
notificationStore.addNotification({
  type: 'warning',
  message: '사용자 정의 메시지',
  duration: 5000, // 5초
  count: null
})</code></pre>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useNotificationStore } from '@/stores/notification'

const notificationStore = useNotificationStore()
const profanityCount = ref(0)

const testProfanity = () => {
  profanityCount.value++
  notificationStore.notifyProfanity(profanityCount.value)
}

const testAbuse = () => {
  notificationStore.notifyAbuse()
}

const testWarning = () => {
  notificationStore.notifyWarning('이것은 경고 메시지입니다')
}

const testInfo = () => {
  notificationStore.notifyInfo('정보 알림입니다')
}

const testSuccess = () => {
  notificationStore.notifySuccess('작업이 완료되었습니다')
}

const clearAll = () => {
  notificationStore.clearAllNotifications()
  profanityCount.value = 0
}
</script>

<style scoped>
.notification-example {
  max-width: 800px;
  margin: 40px auto;
  padding: 20px;
}

h2 {
  font-size: 24px;
  margin-bottom: 16px;
  color: #333;
}

.description {
  color: #666;
  margin-bottom: 24px;
  line-height: 1.6;
}

.button-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 32px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  color: white;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.btn:active {
  transform: translateY(0);
}

.btn-danger {
  background-color: #ff4444;
}

.btn-primary {
  background-color: #4a5eb8;
}

.btn-warning {
  background-color: #ff9800;
}

.btn-info {
  background-color: #607d8b;
}

.btn-success {
  background-color: #4caf50;
}

.btn-secondary {
  background-color: #9e9e9e;
}

.code-example {
  background-color: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
}

.code-example h3 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 18px;
  color: #333;
}

.code-example pre {
  background-color: #2d2d2d;
  color: #f8f8f2;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.5;
}

.code-example code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}
</style>
