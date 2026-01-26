<template>
  <div class="form-container">
    <div class="form-header">
      <button class="back-button" @click="handleBack" aria-label="뒤로 가기">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <div class="page-header">
        <h1 class="page-title">상담 신청</h1>
        <span class="page-indicator">2 / 3</span>
      </div>
    </div>

    <div class="form-content">
      <form @submit.prevent="handleSubmit">
        <!-- 본인 확인 -->
        <section class="form-section">
          <h2 class="section-title">본인 확인</h2>
          <p class="section-description">상담 진행을 위해 휴대전화 본인인증이 필요합니다.</p>

          <!-- 통신사 선택 -->
          <div class="carrier-group">
            <button
              v-for="carrier in carriers"
              :key="carrier.value"
              type="button"
              class="carrier-button"
              :class="{ active: formData.carrier === carrier.value }"
              @click="formData.carrier = carrier.value"
            >
              {{ carrier.label }}
            </button>
          </div>

          <!-- 성명 입력 -->
          <div class="input-group">
            <label class="input-label">성명</label>
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 11C14.2091 11 16 9.20914 16 7C16 4.79086 14.2091 3 12 3C9.79086 3 8 4.79086 8 7C8 9.20914 9.79086 11 12 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <input
                v-model="formData.name"
                type="text"
                class="form-input"
                placeholder="실명 입력"
                maxlength="4"
                required
              />
            </div>
            <p v-if="formData.name.length > 0" class="input-hint">
              {{ formData.name.length }} / 4자
            </p>
          </div>

          <!-- 휴대폰 번호 입력 -->
          <div class="input-group">
            <label class="input-label">휴대폰 번호</label>
            <div class="phone-input-row">
              <div class="input-wrapper flex-1">
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M22 16.92V19.92C22.0011 20.1985 21.9441 20.4742 21.8325 20.7293C21.7209 20.9845 21.5573 21.2136 21.3521 21.4019C21.1468 21.5901 20.9046 21.7335 20.6407 21.8227C20.3769 21.9119 20.0974 21.9451 19.82 21.92C16.7428 21.5856 13.787 20.5341 11.19 18.85C8.77382 17.3147 6.72533 15.2662 5.18999 12.85C3.49997 10.2412 2.44824 7.27099 2.11999 4.17997C2.095 3.90344 2.12787 3.62474 2.21649 3.3616C2.30512 3.09846 2.44756 2.85666 2.63476 2.65162C2.82196 2.44658 3.0498 2.28271 3.30379 2.17052C3.55777 2.05833 3.83233 2.00026 4.10999 1.99997H7.10999C7.5953 1.9952 8.06579 2.16705 8.43376 2.48351C8.80173 2.79996 9.04207 3.23945 9.10999 3.71997C9.23662 4.68004 9.47144 5.6227 9.80999 6.52997C9.94454 6.8879 9.97366 7.27689 9.8939 7.65086C9.81415 8.02482 9.62886 8.36809 9.35999 8.63998L8.08999 9.90997C9.51355 12.4135 11.5864 14.4864 14.09 15.91L15.36 14.64C15.6319 14.3711 15.9751 14.1858 16.3491 14.1061C16.7231 14.0263 17.1121 14.0554 17.47 14.19C18.3773 14.5285 19.3199 14.7634 20.28 14.89C20.7658 14.9585 21.2094 15.2032 21.5265 15.5775C21.8437 15.9518 22.0122 16.4296 22 16.92Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <input
                  v-model="formData.phone"
                  type="tel"
                  class="form-input"
                  placeholder="숫자만 입력"
                  maxlength="11"
                  @input="handlePhoneInput"
                  :disabled="verificationSent"
                  required
                />
              </div>
              <button
                v-if="!verificationSent"
                type="button"
                class="verify-button"
                :class="{ active: isPhoneValid }"
                :disabled="!isPhoneValid"
                @click="sendVerification"
              >
                인증
              </button>
              <button
                v-else
                type="button"
                class="resend-button"
                @click="resendVerification"
              >
                재발송
              </button>
            </div>
            <p v-if="formData.phone.length > 0 && !isPhoneValid" class="error-message">
              휴대폰 번호는 11자리 숫자만 입력 가능합니다
            </p>
          </div>

          <!-- 인증번호 입력 (인증 버튼 클릭 후 표시) -->
          <div v-if="verificationSent" class="input-group verification-group">
            <label class="input-label">인증번호</label>
            <div class="phone-input-row">
              <div class="input-wrapper flex-1">
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 15C13.6569 15 15 13.6569 15 12C15 10.3431 13.6569 9 12 9C10.3431 9 9 10.3431 9 12C9 13.6569 10.3431 15 12 15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M19.4 15C19.2669 15.3016 19.2272 15.6362 19.286 15.9606C19.3448 16.285 19.4995 16.5843 19.73 16.82L19.79 16.88C19.976 17.0657 20.1235 17.2863 20.2241 17.5291C20.3248 17.7719 20.3766 18.0322 20.3766 18.295C20.3766 18.5578 20.3248 18.8181 20.2241 19.0609C20.1235 19.3037 19.976 19.5243 19.79 19.71C19.6043 19.896 19.3837 20.0435 19.1409 20.1441C18.8981 20.2448 18.6378 20.2966 18.375 20.2966C18.1122 20.2966 17.8519 20.2448 17.6091 20.1441C17.3663 20.0435 17.1457 19.896 16.96 19.71L16.9 19.65C16.6643 19.4195 16.365 19.2648 16.0406 19.206C15.7162 19.1472 15.3816 19.1869 15.08 19.32C14.7842 19.4468 14.532 19.6572 14.3543 19.9255C14.1766 20.1938 14.0813 20.5082 14.08 20.83V21C14.08 21.5304 13.8693 22.0391 13.4942 22.4142C13.1191 22.7893 12.6104 23 12.08 23C11.5496 23 11.0409 22.7893 10.6658 22.4142C10.2907 22.0391 10.08 21.5304 10.08 21V20.91C10.0723 20.579 9.96512 20.258 9.77251 19.9887C9.5799 19.7194 9.31074 19.5143 9 19.4C8.69838 19.2669 8.36381 19.2272 8.03941 19.286C7.71502 19.3448 7.41568 19.4995 7.18 19.73L7.12 19.79C6.93425 19.976 6.71368 20.1235 6.47088 20.2241C6.22808 20.3248 5.96783 20.3766 5.705 20.3766C5.44217 20.3766 5.18192 20.3248 4.93912 20.2241C4.69632 20.1235 4.47575 19.976 4.29 19.79C4.10405 19.6043 3.95653 19.3837 3.85588 19.1409C3.75523 18.8981 3.70343 18.6378 3.70343 18.375C3.70343 18.1122 3.75523 17.8519 3.85588 17.6091C3.95653 17.3663 4.10405 17.1457 4.29 16.96L4.35 16.9C4.58054 16.6643 4.73519 16.365 4.794 16.0406C4.85282 15.7162 4.81312 15.3816 4.68 15.08C4.55324 14.7842 4.34276 14.532 4.07447 14.3543C3.80618 14.1766 3.49179 14.0813 3.17 14.08H3C2.46957 14.08 1.96086 13.8693 1.58579 13.4942C1.21071 13.1191 1 12.6104 1 12.08C1 11.5496 1.21071 11.0409 1.58579 10.6658C1.96086 10.2907 2.46957 10.08 3 10.08H3.09C3.42099 10.0723 3.742 9.96512 4.0113 9.77251C4.28059 9.5799 4.48572 9.31074 4.6 9C4.73312 8.69838 4.77282 8.36381 4.714 8.03941C4.65519 7.71502 4.50054 7.41568 4.27 7.18L4.21 7.12C4.02405 6.93425 3.87653 6.71368 3.77588 6.47088C3.67523 6.22808 3.62343 5.96783 3.62343 5.705C3.62343 5.44217 3.67523 5.18192 3.77588 4.93912C3.87653 4.69632 4.02405 4.47575 4.21 4.29C4.39575 4.10405 4.61632 3.95653 4.85912 3.85588C5.10192 3.75523 5.36217 3.70343 5.625 3.70343C5.88783 3.70343 6.14808 3.75523 6.39088 3.85588C6.63368 3.95653 6.85425 4.10405 7.04 4.29L7.1 4.35C7.33568 4.58054 7.63502 4.73519 7.95941 4.794C8.28381 4.85282 8.61838 4.81312 8.92 4.68H9C9.29577 4.55324 9.54802 4.34276 9.72569 4.07447C9.90337 3.80618 9.99872 3.49179 10 3.17V3C10 2.46957 10.2107 1.96086 10.5858 1.58579C10.9609 1.21071 11.4696 1 12 1C12.5304 1 13.0391 1.21071 13.4142 1.58579C13.7893 1.96086 14 2.46957 14 3V3.09C14.0013 3.41179 14.0966 3.72618 14.2743 3.99447C14.452 4.26276 14.7042 4.47324 15 4.6C15.3016 4.73312 15.6362 4.77282 15.9606 4.714C16.285 4.65519 16.5843 4.50054 16.82 4.27L16.88 4.21C17.0657 4.02405 17.2863 3.87653 17.5291 3.77588C17.7719 3.67523 18.0322 3.62343 18.295 3.62343C18.5578 3.62343 18.8181 3.67523 19.0609 3.77588C19.3037 3.87653 19.5243 4.02405 19.71 4.21C19.896 4.39575 20.0435 4.61632 20.1441 4.85912C20.2448 5.10192 20.2966 5.36217 20.2966 5.625C20.2966 5.88783 20.2448 6.14808 20.1441 6.39088C20.0435 6.63368 19.896 6.85425 19.71 7.04L19.65 7.1C19.4195 7.33568 19.2648 7.63502 19.206 7.95941C19.1472 8.28381 19.1869 8.61838 19.32 8.92V9C19.4468 9.29577 19.6572 9.54802 19.9255 9.72569C20.1938 9.90337 20.5082 9.99872 20.83 10H21C21.5304 10 22.0391 10.2107 22.4142 10.5858C22.7893 10.9609 23 11.4696 23 12C23 12.5304 22.7893 13.0391 22.4142 13.4142C22.0391 13.7893 21.5304 14 21 14H20.91C20.5882 14.0013 20.2738 14.0966 20.0055 14.2743C19.7372 14.452 19.5268 14.7042 19.4 15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <input
                  v-model="formData.verificationCode"
                  type="text"
                  class="form-input"
                  placeholder="인증번호 6자리 입력"
                  maxlength="6"
                  :disabled="verificationSuccess"
                  @input="clearVerificationError"
                />
              </div>
              <button
                type="button"
                class="confirm-button-green"
                :class="{ active: formData.verificationCode.length === 6 || verificationSuccess }"
                :disabled="(formData.verificationCode.length !== 6 && !verificationSuccess) || verificationSuccess"
                @click="verifyCode"
              >
                <svg v-if="verificationSuccess" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 6L9 17L4 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span v-else>확인</span>
              </button>
            </div>
            <p v-if="verificationError" class="error-message">
              {{ verificationError }}
            </p>
            <p v-if="verificationSuccess" class="success-message">
              ✓ 본인인증이 완료되었습니다.
            </p>
          </div>
        </section>

        <!-- 다음 단계 버튼 -->
        <button
          type="submit"
          class="submit-button"
          :disabled="!isFormComplete"
        >
          다음 단계
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 통신사 목록
const carriers = [
  { value: 'skt', label: 'SKT' },
  { value: 'kt', label: 'KT' },
  { value: 'lgu', label: 'LG U+' }
]

// 폼 데이터
const formData = ref({
  carrier: 'skt',
  name: '',
  phone: '',
  verificationCode: ''
})

// 인증 상태
const verificationSent = ref(false)
const verificationSuccess = ref(false)
const verificationError = ref('')

// 휴대폰 번호 유효성 검사 (11자리 숫자)
const isPhoneValid = computed(() => {
  return /^\d{11}$/.test(formData.value.phone)
})

// 폼 완성 여부
const isFormComplete = computed(() => {
  return formData.value.name.trim().length > 0 &&
         isPhoneValid.value &&
         verificationSuccess.value
})

// 휴대폰 번호 입력 처리 (숫자만 허용)
const handlePhoneInput = (event) => {
  formData.value.phone = event.target.value.replace(/\D/g, '')
}

// 인증번호 발송
const sendVerification = () => {
  if (!isPhoneValid.value) return

  // TODO: 실제 API 호출
  // 현재는 발송만 시뮬레이션
  verificationSent.value = true
  console.log('인증번호 발송:', {
    carrier: formData.value.carrier,
    name: formData.value.name,
    phone: formData.value.phone
  })
}

// 인증번호 재발송
const resendVerification = () => {
  // 인증 상태 초기화
  verificationError.value = ''
  verificationSuccess.value = false
  formData.value.verificationCode = ''

  // TODO: 실제 API 호출
  console.log('인증번호 재발송:', {
    carrier: formData.value.carrier,
    name: formData.value.name,
    phone: formData.value.phone
  })
}

// 인증번호 확인
const verifyCode = () => {
  if (formData.value.verificationCode.length !== 6) return

  // 인증번호 검증 (961018만 통과)
  if (formData.value.verificationCode !== '961018') {
    verificationError.value = '인증번호가 올바르지 않습니다'
    verificationSuccess.value = false
    return
  }

  // 인증 성공
  verificationError.value = ''
  verificationSuccess.value = true

  console.log('인증 성공:', formData.value)
}

// 인증 에러 메시지 초기화
const clearVerificationError = () => {
  verificationError.value = ''
}

// 뒤로 가기
const handleBack = () => {
  router.back()
}

// 폼 제출
const handleSubmit = () => {
  if (!isFormComplete.value) return

  // TODO: 실제 API 호출
  console.log('인증 완료:', formData.value)

  // 임시로 localStorage에 저장
  localStorage.setItem('clientVerification', JSON.stringify(formData.value))

  // 다음 단계로 이동 (3/3 단계 - 추후 구현)
  // TODO: 3단계 페이지 구현 후 주석 해제
  // router.push('/client/consultation/final')

  alert('본인 인증이 완료되었습니다!\n(3/3 단계 페이지는 추후 구현 예정)')
}
</script>

<style scoped>
.form-container {
  min-height: 100vh;
  background: #f5f5f7;
  display: flex;
  flex-direction: column;
}

/* 헤더 영역 */
.form-header {
  background: white;
  padding: 16px 20px;
  position: relative;
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

/* 통신사 선택 */
.carrier-group {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.carrier-button {
  flex: 1;
  padding: 14px;
  background: white;
  border: 2px solid #e5e5e7;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #86868b;
  cursor: pointer;
  transition: all 0.2s;
}

.carrier-button.active {
  background: #5B7CFF;
  border-color: #5B7CFF;
  color: white;
}

.carrier-button:hover:not(.active) {
  border-color: #5B7CFF;
  color: #5B7CFF;
}

/* 입력 그룹 */
.input-group {
  margin-bottom: 20px;
}

.input-label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-wrapper.flex-1 {
  flex: 1;
}

.input-icon {
  position: absolute;
  left: 14px;
  width: 20px;
  height: 20px;
  color: #86868b;
  pointer-events: none;
}

.form-input {
  width: 100%;
  padding: 14px 16px 14px 44px;
  border: none;
  border-radius: 12px;
  background: white;
  font-size: 16px;
  color: #1d1d1f;
  box-sizing: border-box;
  transition: box-shadow 0.2s;
}

.form-input::placeholder {
  color: #c7c7cc;
}

.form-input:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(91, 124, 255, 0.15);
}

.form-input:disabled {
  background: #f5f5f7;
  color: #86868b;
}

.input-hint {
  font-size: 13px;
  color: #86868b;
  margin: 6px 0 0 4px;
}

/* 휴대폰 번호 입력 행 */
.phone-input-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 인증 버튼 */
.verify-button {
  padding: 14px 20px;
  background: #e5e5e7;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #86868b;
  cursor: not-allowed;
  white-space: nowrap;
  transition: all 0.2s;
}

.verify-button.active {
  background: #1d1d1f;
  color: white;
  cursor: pointer;
}

.verify-button:disabled {
  cursor: not-allowed;
}

/* 재발송 버튼 */
.resend-button {
  padding: 14px 20px;
  background: #1d1d1f;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}

.resend-button:hover {
  background: #2d2d2f;
}

/* 확인 버튼 (초록색) */
.confirm-button-green {
  padding: 14px 20px;
  background: #e5e5e7;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #86868b;
  cursor: not-allowed;
  white-space: nowrap;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 70px;
}

.confirm-button-green svg {
  width: 24px;
  height: 24px;
}

.confirm-button-green.active {
  background: #34c759;
  color: white;
  cursor: pointer;
}

.confirm-button-green.active:hover {
  background: #2db84d;
}

.confirm-button-green:disabled {
  cursor: not-allowed;
}

/* 인증 성공 시 확인 버튼 */
.confirm-button-green:disabled.active {
  background: #34c759;
  opacity: 1;
  cursor: default;
}

/* 인증번호 입력 섹션 */
.verification-group {
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 에러 메시지 */
.error-message {
  font-size: 13px;
  color: #ff3b30;
  margin: 6px 0 0 4px;
}

/* 성공 메시지 */
.success-message {
  font-size: 13px;
  color: #34c759;
  margin: 6px 0 0 4px;
  font-weight: 600;
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

.submit-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(91, 124, 255, 0.4);
}

.submit-button:active:not(:disabled) {
  transform: translateY(0);
}

.submit-button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  box-shadow: none;
}

/* 모바일 최적화 */
@media (max-width: 768px) {
  .form-content {
    padding: 20px 16px;
  }

  .section-title {
    font-size: 20px;
  }

  .carrier-button {
    font-size: 15px;
    padding: 12px;
  }
}
</style>
