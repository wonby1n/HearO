<template>
  <div class="client-call-issue-view">
    <div class="main-content">
      <!-- 물음표 아이콘 -->
      <div class="icon-container">
        <div class="icon-circle">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" />
            <circle cx="12" cy="17" r="0.5" fill="currentColor" />
          </svg>
        </div>
      </div>

      <!-- 메시지 -->
      <h1 class="title">통화에 문제가 있었나요?</h1>
      <p class="description">
        연결이 끊기거나 기타 문제가 있었다면<br />
        알려주세요.
      </p>
    </div>

    <!-- 하단 버튼 영역 -->
    <div class="button-section">
      <button type="button" class="no-button" @click="handleNo">
        없어요
      </button>
      <button type="button" class="yes-button" @click="handleYes">
        있어요
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 문제 없음 - 만족도 조사로 이동
const handleNo = () => {
  router.push({
    name: 'client-review',
    query: { consultationId: route.query.consultationId }
  })
}

// 문제 있음 - 재연결 페이지로 이동
const handleYes = () => {
  router.push({ 
    name: 'client-reconnect',
    // 💡 여기에 query를 추가해서 ID를 계속 들고 가야 합니다!
    query: { consultationId: route.query.consultationId }
  })
}
</script>

<style scoped>
.client-call-issue-view {
  min-height: 100vh;
  max-width: 430px;
  margin: 0 auto;
  background: #ffffff;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 24px;
  text-align: center;
}

/* 아이콘 컨테이너 */
.icon-container {
  margin-bottom: 32px;
}

.icon-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-circle svg {
  width: 40px;
  height: 40px;
  color: #9ca3af;
}

/* 제목 */
.title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 16px 0;
}

/* 설명 */
.description {
  font-size: 14px;
  color: #9ca3af;
  margin: 0;
  line-height: 1.6;
}

/* 버튼 영역 */
.button-section {
  display: flex;
  gap: 12px;
  padding: 16px 24px 32px;
}

.no-button {
  flex: 1;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #ffffff;
  font-size: 16px;
  font-weight: 600;
  color: #6b7280;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.no-button:hover {
  background: #f9fafb;
}

.yes-button {
  flex: 1;
  padding: 16px;
  border: none;
  border-radius: 12px;
  background: #3b82f6;
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.yes-button:hover {
  background: #2563eb;
}
</style>
