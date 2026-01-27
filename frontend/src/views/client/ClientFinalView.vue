<template>
  <div class="client-final-view">
    <div class="main-content">
      <!-- 체크 아이콘 -->
      <div class="icon-container">
        <div class="icon-circle">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12" />
          </svg>
        </div>
      </div>

      <!-- 메시지 -->
      <h1 class="title">감사합니다.</h1>
      <p class="description">
        소중한 의견을 바탕으로<br />
        더 나은 서비스를 제공하겠습니다.
      </p>
    </div>
    <div class="button-section">
      <button type="button" class="main-redirect" @click="goToMain">
        <svg viewBox="0 0 24 24" fill="currentColor" class="home-icon">
          <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
        </svg>
        메인으로
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToMain = () => {
  router.push({ name: 'client-landing' })
}
</script>

<style scoped>
.client-final-view {
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
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-circle svg {
  width: 40px;
  height: 40px;
  color: #3b82f6;
}

/* 제목 */
.title {
  font-size: 24px;
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
  padding: 16px 24px 32px;
}

.main-redirect {
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

.main-redirect:hover {
  background: #2563eb;
}

.home-icon {
  width: 20px;
  height: 20px;
}
</style>
