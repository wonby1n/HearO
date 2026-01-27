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
  </div>
</template>

<script setup>
</script>

<style scoped>
.client-final-view {
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
</style>
