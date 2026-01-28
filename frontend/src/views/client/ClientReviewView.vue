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
              :aria-label="`상담 과정 ${star}점`"
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
              :aria-label="`해결 방법 ${star}점`"
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
    <div class="main-redirect">
      <p @click="mainRedirect">메인으로</p>
    </div>
    <!-- 하단 버튼 영역 -->
    <div class="button-section">
      <button type="button" class="submit-button" :class="{ active: isFormValid }" :disabled="!isFormValid" @click="handleSubmit">
        제출하기
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const route = useRoute()
const notificationStore = useNotificationStore()

// 별점 상태
const processRating = ref(0)
const solutionRating = ref(0)
const additionalComment = ref('')
const isLoading = ref(false)

// 폼 유효성 검사 (두 별점 모두 선택해야 제출 가능)
const isFormValid = computed(() => {
  return processRating.value > 0 && solutionRating.value > 0
})

// 메인으로
const mainRedirect = () => {
  router.push({ name: 'client-landing' })
}

// 제출하기
const handleSubmit = async () => {
  if (!isFormValid.value || isLoading.value) return

  const consultationId = route.query.consultationId
  if (!consultationId) {
    notificationStore.notifyError('상담 정보를 찾을 수 없습니다')
    return
  }

  const reviewData = {
    processRating: processRating.value,
    solutionRating: solutionRating.value,
    additionalComment: additionalComment.value,
    consultationId
  }

  isLoading.value = true

  try {
    // TODO: API 호출로 리뷰 데이터 전송
    // const response = await fetch(`/api/v1/consultations/${consultationId}/review`, {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({
    //     processRating: reviewData.processRating,
    //     solutionRating: reviewData.solutionRating,
    //     comment: reviewData.additionalComment
    //   })
    // })
    // if (!response.ok) throw new Error('리뷰 제출 실패')

    // 제출 완료 후 감사 페이지로 이동
    router.push({ name: 'client-final' })
  } catch (error) {
    console.error('리뷰 제출 중 오류:', error)
    notificationStore.notifyError('리뷰 제출에 실패했습니다')
  } finally {
    isLoading.value = false
  }
}

</script>

<style scoped>
.client-review-view {
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
  /* background: #ffffff; */
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

.skip-button:hover:not(:disabled) {
  background: #f9fafb;
}

.skip-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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

.submit-button.active:hover:not(:disabled) {
  background: #2563eb;
}

.button-section {
  padding: 16px 24px 32px;
}


.main-redirect {
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
