<template>
  <div class="client-call-end-view">
    <div class="main-content">
      <!-- 종료된 통화 아이콘 (회색) -->
      <div class="call-ended-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <!-- 전화기 아이콘 -->
          <path
            d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72 12.84 12.84 0 00.7 2.81 2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45 12.84 12.84 0 002.81.7A2 2 0 0122 16.92z" />
          <!-- X 표시 -->
          <line x1="15" y1="9" x2="9" y2="15" stroke-linecap="round" />
          <line x1="9" y1="9" x2="15" y2="15" stroke-linecap="round" />
        </svg>
      </div>

      <!-- 상담 종료 메시지 -->
      <h1 class="end-message">상담이 종료되었습니다</h1>

      <!-- 통화 시간 -->
      <p class="call-duration">{{ formattedDuration }}</p>
    </div>
    <div class="call-reconnection">
      <p @click="handleReconnect">같은 내용으로 재연결하기</p>
    </div>
    <!-- 하단 버튼 영역 -->
    <div class="button-section">
      <button type="button" class="next-button" @click="clientReview">
        다음으로
      </button>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 라우트 쿼리에서 통화 시간(초) 가져오기
const callDuration = computed(() => {
  return parseInt(route.query.duration) || 0
})

// 통화 시간 포맷팅 (mm:ss)
const formattedDuration = computed(() => {
  const minutes = Math.floor(callDuration.value / 60)
  const seconds = callDuration.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})


const clientReview = () => {
  router.push({ name: 'client-review'})
}

const handleReconnect = () => {
  router.push({ name: 'client-waiting'})
}


</script>

<style scoped>
.client-call-end-view {
  min-height: 100vh;
  max-width: 430px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  display: flex;
  flex-direction: column;
  position: relative;
}


.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  gap: 16px;
}

/* 종료된 통화 아이콘 */
.call-ended-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.call-ended-icon svg {
  width: 48px;
  height: 48px;
}

/* 상담 종료 메시지 */
.end-message {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  margin-top: 8px;
}

/* 통화 시간 */
.call-duration {
  font-size: 16px;
  color: #9ca3af;
  margin: 0;
  font-family: 'SF Mono', Monaco, monospace;
  letter-spacing: 1px;
}


.next-button {
  width: 100%;
  padding: 16px;
  border: none;
  border-radius: 12px;
  background: #3b82f6;
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  cursor: pointer;
  transition: background-color 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.next-button:hover {
  background: #2563eb;
}

.button-section {
  padding: 16px 24px 32px;
}


.call-reconnection {
  width: 100%;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #646464;
  cursor: pointer;
  transition: background-color 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

</style>
