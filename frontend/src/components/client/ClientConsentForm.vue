<template>
  <div class="form-container">
    <div class="form-header">
      <!-- 뒤로가기 버튼 가시성 및 클릭 영역 개선 -->
      <button class="back-button" @click="handleBack" aria-label="뒤로 가기">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M15 19L8 12L15 5" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <div class="page-header">
        <h1 class="page-title">상담 신청</h1>
        <span class="page-indicator">3 / 3</span>
      </div>
    </div>

    <div class="form-content">
      <form @submit.prevent="handleSubmit">
        <!-- 약관 동의 -->
        <section class="form-section">
          <h2 class="section-title">약관 동의</h2>
          <p class="section-description">안전한 상담을 위해 아래 약관에 동의해주세요.</p>

          <!-- 전체 동의하기 -->
          <button
            type="button"
            class="agree-all-button"
            :class="{ active: isAllAgreed }"
            @click="toggleAllAgreements"
          >
            <div class="checkbox-icon">
              <svg v-if="isAllAgreed" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 6L9 17L4 12" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span class="button-text">전체 동의하기</span>
          </button>

          <!-- 약관 목록 -->
          <div class="terms-list">
            <!-- 개인정보 수집 이용 (필수) -->
            <div class="term-item">
              <button
                type="button"
                class="term-header"
                @click="toggleTerm('privacy')"
              >
                <div class="term-header-left">
                  <div
                    class="checkbox-small"
                    role="checkbox"
                    :aria-checked="agreements.privacy"
                    :class="{ checked: agreements.privacy }"
                    @click.stop="toggleAgreement('privacy')"
                    tabindex="0"
                  >
                    <svg v-if="agreements.privacy" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M20 6L9 17L4 12" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </div>
                  <span class="term-title">개인정보 수집 이용 (필수)</span>
                </div>
                <svg class="expand-icon" :class="{ rotated: expandedTerms.privacy }" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
              
              <div v-if="expandedTerms.privacy" class="term-content">
                <div class="term-inner-box">
                  <dl class="term-details">
                    <div class="term-row">
                      <dt class="term-label">수집 항목</dt>
                      <dd class="term-value"><span class="highlight">[필수]</span> 성명, 전화번호</dd>
                    </div>
                    <div class="term-row">
                      <dt class="term-label">수집 · 목적</dt>
                      <dd class="term-value">전화 상담 예약 및 서비스 접수<br>(A/S, B/S, 만족도 조사)</dd>
                    </div>
                    <div class="term-row">
                      <dt class="term-label">보유 · 기간</dt>
                      <dd class="term-value">
                        <span class="highlight">1년</span>
                        <p class="text-muted">단, 관련 법령에 따른 보존 의무가 있는 경우 해당 기간까지 보관</p>
                      </dd>
                    </div>
                  </dl>
                  <div class="term-footer-notice">
                    <p>※ 동의를 거절하실 수 있으나, 미동의 시 상담 서비스 이용에 제약이 있을 수 있습니다.</p>
                    <p>※ 상세 내용은 하단의 개인정보 처리방침을 참조해 주세요.</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 고유식별정보 처리 (필수) -->
            <div class="term-item">
              <button
                type="button"
                class="term-header"
                @click="toggleTerm('identification')"
              >
                <div class="term-header-left">
                  <div
                    class="checkbox-small"
                    role="checkbox"
                    :aria-checked="agreements.identification"
                    tabindex="0"
                    :class="{ checked: agreements.identification }"
                    @click.stop="toggleAgreement('identification')"
                  >
                    <svg v-if="agreements.identification" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M20 6L9 17L4 12" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </div>
                  <span class="term-title">고유식별정보 처리 (필수)</span>
                </div>
                <svg class="expand-icon" :class="{ rotated: expandedTerms.identification }" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
              <div v-if="expandedTerms.identification" class="term-content">
                <div class="term-inner-box">
                  <p class="term-text-small">
                    관계 법령에 따라 고유식별정보를 안전하게 처리하며 목적 외 용도로 사용하지 않습니다.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 주의사항 섹션 -->
        <section class="notice-section">
          <div class="notice-box">
            <div class="notice-header">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="notice-icon">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M12 8V12M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <h3 class="notice-title">개인정보 입력 시 주의사항</h3>
            </div>
            <ul class="notice-list">
              <li>개인정보 보호를 위해 <strong>본인 외 타인의 정보(가족 포함)는 입력이 불가</strong>하며, 잘못된 정보 제공으로 인한 책임은 정보 제공자에게 있으니 주의해 주시기 바랍니다.</li>
              <li>본 화면에 입력하신 개인정보는 실제 서비스(상담)나 상품 주문 등을 <strong>신청해야 수집</strong>이 되며, 그 외에는 수집 또는 저장되지 않습니다.</li>
            </ul>
          </div>
        </section>

        <!-- 상담사 연결 버튼 -->
        <button
          type="submit"
          class="submit-button"
          :disabled="!isFormComplete"
        >
          상담사 연결
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCustomerStore } from '@/stores/customer'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const customerStore = useCustomerStore()
const notificationStore = useNotificationStore()

const agreements = ref({
  privacy: false,
  identification: false,
})

// 모든 약관 항목을 기본적으로 접힘(false) 상태로 설정
const expandedTerms = ref({
  privacy: false, 
  identification: false
})

const isAllAgreed = computed(() => {
  return agreements.value.privacy && agreements.value.identification
})

const isFormComplete = computed(() => isAllAgreed.value)

const toggleAllAgreements = () => {
  const newValue = !isAllAgreed.value
  agreements.value.privacy = newValue
  agreements.value.identification = newValue
}

const toggleAgreement = (term) => {
  agreements.value[term] = !agreements.value[term]
}

const toggleTerm = (term) => {
  expandedTerms.value[term] = !expandedTerms.value[term]
}

const handleBack = () => {
  router.push({ name: 'client-consultation-verification' })
}

const handleSubmit = () => {
  if (!isFormComplete.value) return

  // 약관 동의 정보 저장
  customerStore.saveConsent(agreements.value)

  // 성공 알림
  notificationStore.notifySuccess('약관 동의가 완료되었습니다')

  // 상담사 연결 (추후 구현)
  // TODO: 상담사 연결 로직 구현 후 주석 해제
  // router.push({ name: 'client-call' })
}
</script>

<style scoped>
.form-container {
  min-height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  background: #f5f5f7;
  display: flex;
  flex-direction: column;
  /* 시스템 폰트 스택 적용 */
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans KR", sans-serif;
  color: #1d1d1f;
  -webkit-font-smoothing: antialiased;
}

/* 헤더 영역 */
.form-header {
  background: white;
  padding: 16px 20px;
  position: relative;
  border-bottom: 1px solid #f0f0f2;
}

.back-button {
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  padding: 8px;
  cursor: pointer;
  color: #1d1d1f;
}

.back-button svg {
  width: 24px;
  height: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-left: 32px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1d1d1f;
  margin: 0;
}

.page-indicator {
  font-size: 14px;
  color: #5B7CFF;
  font-weight: 600;
}

/* 폼 콘텐츠 */
.form-content {
  flex: 1;
  padding: 24px 20px;
}

.form-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #1d1d1f;
  margin: 0 0 8px 0;
}

.section-description {
  font-size: 14px;
  color: #86868b;
  margin: 0 0 24px 0;
  line-height: 1.5;
}

/* 전체 동의하기 버튼 */
.agree-all-button {
  width: 100%;
  padding: 18px 20px;
  background: white;
  border: 2px solid #e5e5e7;
  border-radius: 12px;
  font-size: 17px;
  font-weight: 700;
  color: #1d1d1f;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.2s;
  margin-bottom: 20px;
}

.agree-all-button.active {
  background: linear-gradient(135deg, #5B7CFF 0%, #4A61D9 100%);
  border-color: #5B7CFF;
  color: white;
}

.checkbox-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #e5e5e7;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
}

.agree-all-button.active .checkbox-icon {
  background: white;
}

.checkbox-icon svg {
  width: 20px;
  height: 20px;
  color: #5B7CFF;
}

.button-text {
  flex: 1;
  text-align: left;
}

/* 약관 목록 */
.terms-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.term-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.2s;
  border: 1px solid #f0f0f2;
}

/* 약관 헤더 */
.term-header {
  width: 100%;
  padding: 16px;
  background: none;
  border: none;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.term-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.checkbox-small {
  width: 22px;
  height: 22px;
  border: 2px solid #d1d5db;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
  background: white;
}

.checkbox-small.checked {
  background: #5B7CFF;
  border-color: #5B7CFF;
}

.checkbox-small svg {
  width: 14px;
  height: 14px;
  color: white;
}

.term-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
}

.expand-icon {
  width: 18px;
  height: 18px;
  color: #86868b;
  transition: transform 0.2s;
}

.expand-icon.rotated {
  transform: rotate(180deg);
}

/* 약관 상세 내용 디자인 개선 */
.term-content {
  padding: 0 16px 16px 16px;
  animation: slideDown 0.3s ease-out;
}

.term-inner-box {
  background: #f9fafb;
  border-radius: 10px;
  padding: 16px;
  border: 1px solid #f0f0f2;
}

.term-details {
  margin: 0;
}

.term-row {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #edf0f2;
}

.term-row:last-of-type {
  border-bottom: none;
}

.term-label {
  flex: 0 0 70px;
  font-size: 14px;
  font-weight: 700;
  color: #86868b;
  line-height: 1.6;
}

.term-value {
  flex: 1;
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: #1d1d1f;
  font-weight: 500;
}

.highlight {
  color: #5B7CFF;
  font-weight: 700;
}

.text-muted {
  display: block;
  font-size: 12px;
  color: #86868b;
  margin-top: 4px;
  font-weight: 400;
}

.term-footer-notice {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #d1d5db;
}

.term-footer-notice p {
  font-size: 12px;
  color: #86868b;
  line-height: 1.5;
  margin: 4px 0;
}

.term-text-small {
  font-size: 13px;
  line-height: 1.6;
  color: #1d1d1f;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 주의사항 섹션 */
.notice-section {
  margin-top: 14px;
}

.notice-box {
  background: #fff9f0;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #ffe8d1;
}

.notice-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.notice-icon {
  width: 18px;
  height: 18px;
  color: #ff9500;
}

.notice-title {
  font-size: 14px;
  font-weight: 700;
  color: #1d1d1f;
  margin: 0;
}

.notice-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.notice-list li {
  font-size: 12px;
  line-height: 1.6;
  color: #434345;
  margin-bottom: 8px;
  position: relative;
  padding-left: 14px;
}

.notice-list li::before {
  content: "•";
  position: absolute;
  left: 0;
  color: #ff9500;
  font-weight: bold;
}

.notice-list li:last-child {
  margin-bottom: 0;
}

/* 제출 버튼 */
.submit-button {
  width: 100%;
  padding: 18px;
  background: linear-gradient(135deg, #5B7CFF 0%, #4A61D9 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 17px;
  font-weight: 700;
  cursor: pointer;
  margin-top: 32px;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(91, 124, 255, 0.3);
}

.submit-button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  box-shadow: none;
}

/* 모바일 최적화 */
@media (max-width: 768px) {
  .form-content { padding: 20px 16px; }
  .section-title { font-size: 20px; }
  .agree-all-button { font-size: 16px; padding: 16px 18px; }
}
</style>