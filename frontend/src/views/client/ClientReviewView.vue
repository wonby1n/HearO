<template>
  <div class="client-review-view">
    <div class="main-content">
      <!-- 헤더 -->
      <div class="header">
        <h1 class="title">상담 만족도 조사</h1>
        <p class="subtitle">더 나은 서비스 제공을 위해 의견을 들려주세요.</p>
      </div>

      <!-- 질문 섹션 -->
      <div class="questions-section">
        <!-- 질문 1: 상담 과정 만족도 -->
        <div class="question-item">
          <label class="question-label">상담 과정이 원활했나요?</label>
          <div class="star-rating">
            <button
              v-for="star in 5"
              :key="'process-' + star"
              type="button"
              class="star-button"
              :class="{ active: processRating >= star }"
              @click="processRating = star"
            >
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- 질문 2: 해결 방법 만족도 -->
        <div class="question-item">
          <label class="question-label">해결 방법에 만족하시나요?</label>
          <div class="star-rating">
            <button
              v-for="star in 5"
              :key="'solution-' + star"
              type="button"
              class="star-button"
              :class="{ active: solutionRating >= star }"
              @click="solutionRating = star"
            >
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- 추가 의견 -->
        <div class="question-item">
          <label class="question-label">추가 의견이 있으면 작성해주세요.</label>
          <textarea
            v-model="additionalComment"
            class="comment-textarea"
            placeholder="상담 중 불편하셨던 점이나 칭찬하고 싶은 내용을 적어주세요."
            rows="4"
          ></textarea>
        </div>
      </div>
    </div>

    <!-- 하단 버튼 영역 -->
    <div class="button-section">
      <button type="button" class="skip-button" @click="handleSkip">
        건너뛰기
      </button>
      <button
        type="button"
        class="submit-button"
        :class="{ active: isFormValid }"
        :disabled="!isFormValid"
        @click="handleSubmit"
      >
        제출하기
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 별점 상태
const processRating = ref(0)
const solutionRating = ref(0)
const additionalComment = ref('')

// 폼 유효성 검사 (두 별점 모두 선택해야 제출 가능)
const isFormValid = computed(() => {
  return processRating.value > 0 && solutionRating.value > 0
})

// 건너뛰기
const handleSkip = () => {
  router.push({ name: 'client-landing' })
}

// 제출하기
const handleSubmit = () => {
  if (!isFormValid.value) return

  const reviewData = {
    processRating: processRating.value,
    solutionRating: solutionRating.value,
    additionalComment: additionalComment.value,
    consultationId: route.query.consultationId
  }

  console.log('Review submitted:', reviewData)

  // TODO: API 호출로 리뷰 데이터 전송

  // 제출 완료 후 랜딩 페이지로 이동
  router.push({ name: 'client-landing' })
}
</script>

<style scoped>
.client-review-view {
  min-height: 100vh;
  max-width: 430px;
  margin: 0 auto;
  background: #ffffff;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 40px 24px 24px;
  display: flex;
  flex-direction: column;
}

/* 헤더 */
.header {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #9ca3af;
  margin: 0;
}

/* 질문 섹션 */
.questions-section {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.question-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.question-label {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

/* 별점 */
.star-rating {
  display: flex;
  gap: 8px;
}

.star-button {
  width: 36px;
  height: 36px;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  color: #e5e7eb;
  transition: color 0.15s ease, transform 0.1s ease;
}

.star-button:hover {
  transform: scale(1.1);
}

.star-button.active {
  color: #fbbf24;
}

.star-button svg {
  width: 100%;
  height: 100%;
}

/* 텍스트 영역 */
.comment-textarea {
  width: 100%;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  font-size: 14px;
  color: #1f2937;
  resize: none;
  outline: none;
  transition: border-color 0.15s ease;
  box-sizing: border-box;
}

.comment-textarea::placeholder {
  color: #9ca3af;
}

.comment-textarea:focus {
  border-color: #3b82f6;
}

/* 버튼 영역 */
.button-section {
  display: flex;
  gap: 12px;
  padding: 16px 24px 32px;
  background: #ffffff;
}

.skip-button {
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

.skip-button:hover {
  background: #f9fafb;
}

.submit-button {
  flex: 1.5;
  padding: 16px;
  border: none;
  border-radius: 12px;
  background: #e5e7eb;
  font-size: 16px;
  font-weight: 600;
  color: #9ca3af;
  cursor: not-allowed;
  transition: background-color 0.15s ease, color 0.15s ease;
}

.submit-button.active {
  background: #3b82f6;
  color: #ffffff;
  cursor: pointer;
}

.submit-button.active:hover {
  background: #2563eb;
}
</style>
