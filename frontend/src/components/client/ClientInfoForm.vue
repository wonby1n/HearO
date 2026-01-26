<template>
  <div class="form-container">
    <!-- 브라우저 알림 배너 -->
    <BrowserNotice v-model="showBrowserNotice" />

    <div class="form-header">
      <button class="back-button" @click="handleBack" aria-label="뒤로 가기">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <div class="page-header">
        <h1 class="page-title">상담 신청</h1>
        <span class="page-indicator">1 / 3</span>
      </div>
    </div>

    <div class="form-content">
      <form @submit.prevent="handleSubmit">
        <!-- 내 기기 정보 -->
        <section class="form-section">
          <h2 class="section-title">내 기기 정보</h2>
          <p class="section-description">아래 자동 인식된 모델이 맞는지 확인해주세요.</p>

          <div class="device-card">
            <div class="device-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="6" y="2" width="12" height="20" rx="2" stroke="white" stroke-width="2"/>
                <path d="M10 19H14" stroke="white" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="device-details">
              <p class="device-label">자동 인식된 모델</p>
              <p class="device-model">{{ productName }}</p>
              <p class="device-model-number">{{ modelNumber }}</p>
            </div>
          </div>
        </section>

        <!-- 에러코드 (선택) -->
        <section class="form-section">
          <h2 class="section-title">에러코드 및 증상 입력</h2>

          <div class="label-with-counter">
            <label class="input-label optional">
              <svg class="info-icon" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="8" cy="8" r="7" stroke="currentColor" stroke-width="1.5"/>
                <path d="M8 11V8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <circle cx="8" cy="5.5" r="0.75" fill="currentColor"/>
              </svg>
              에러코드 (선택사항)
            </label>
            <span class="char-counter">{{ formData.errorCode.length }} / 100</span>
          </div>
          <input
            v-model="formData.errorCode"
            type="text"
            class="form-input"
            placeholder="화면에 표시된 에러코드가 있다면 입력 (선택)"
            maxlength="100"
          />
        </section>

        <!-- 증상 상세 입력 -->
        <section class="form-section">

          <div class="label-with-counter">
            <label class="input-label required">
              <svg class="message-icon" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="2" y="3" width="12" height="10" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                <path d="M4 6H12M4 8.5H9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              증상 입력
            </label>
            <span class="char-counter" :class="{ warning: formData.symptomDetail.length > 450 }">
              {{ formData.symptomDetail.length }} / 500
            </span>
          </div>
          <textarea
            v-model="formData.symptomDetail"
            class="form-textarea"
            rows="5"
            placeholder="언제부터, 어떤 문제가 발생했는지, 어떤 소리가 나는지 등 상세히 적어주시면 빠른 상담이 가능합니다."
            maxlength="500"
            required
          ></textarea>

          <!-- 음성 녹음 영역 -->
          <div v-if="speechSupported" class="voice-section">
            <div class="voice-guide">
              <svg class="guide-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M12 8V12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <circle cx="12" cy="16" r="1" fill="currentColor"/>
              </svg>
              <span>직접 입력이 어려우시다면 음성으로 말씀해주세요</span>
            </div>

            <!-- 녹음 버튼 -->
            <button
              type="button"
              class="voice-record-button"
              :class="{ recording: isRecording }"
              :aria-label="isRecording ? '녹음 중지' : '녹음 시작'"
              :aria-pressed="isRecording"
              @click="toggleVoiceRecognition"
            >
              <svg class="mic-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 15C13.6569 15 15 13.6569 15 12V6C15 4.34315 13.6569 3 12 3C10.3431 3 9 4.34315 9 6V12C9 13.6569 10.3431 15 12 15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M19 12C19 15.866 15.866 19 12 19M12 19C8.13401 19 5 15.866 5 12M12 19V22M12 22H15M12 22H9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span class="button-text">{{ isRecording ? '녹음 중지' : '녹음 시작' }}</span>
            </button>

            <!-- 녹음 중 상태 표시 -->
            <div v-if="isRecording" class="recording-indicator">
              <div class="recording-animation">
                <span class="recording-dot"></span>
                <span class="recording-dot"></span>
                <span class="recording-dot"></span>
              </div>
              <p class="recording-text">음성을 듣고 있습니다. 천천히 말씀해주세요.</p>
            </div>

            <!-- 녹음된 내용 미리보기 -->
            <div v-if="voiceTranscript" class="transcript-preview">
              <div class="preview-header">
                <span class="preview-label">녹음된 내용</span>
                <div class="preview-actions">
                  <span class="preview-counter">{{ voiceTranscript.length }}자</span>
                  <button type="button" class="clear-button" @click="clearTranscript">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                  </button>
                </div>
              </div>
              <p class="preview-text">{{ voiceTranscript }}</p>
              <button type="button" class="confirm-button" @click="addTranscriptToForm">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 6L9 17L4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                이 내용을 증상 입력란에 추가하기
              </button>
            </div>
          </div>
        </section>

        <!-- 다음 단계 버튼 -->
        <button type="submit" class="submit-button" :disabled="!isFormValid">
          다음 단계
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notification'
import BrowserNotice from '@/components/common/BrowserNotice.vue'

const props = defineProps({
  productName: {
    type: String,
    required: true
  },
  modelNumber: {
    type: String,
    required: true
  }
})

const router = useRouter()
const notificationStore = useNotificationStore()

// 브라우저 알림 표시 여부
const showBrowserNotice = ref(true)

// 음성 인식 관련
const isRecording = ref(false)
const speechSupported = ref(false)
const voiceTranscript = ref('')
const recognition = ref(null)

const formData = ref({
  errorCode: '',
  symptomDetail: ''
})

const isFormValid = computed(() => {
  return formData.value.symptomDetail.trim().length > 0
})

// 음성 인식 초기화
const initSpeechRecognition = () => {
  // Web Speech API 지원 확인
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition

  if (!SpeechRecognition) {
    speechSupported.value = false
    console.warn('음성 인식이 지원되지 않는 브라우저입니다.')
    return
  }

  speechSupported.value = true
  recognition.value = new SpeechRecognition()

  // 한국어 설정
  recognition.value.lang = 'ko-KR'
  // 중간 결과도 반환
  recognition.value.interimResults = true
  // 연속 인식
  recognition.value.continuous = true

  // 인식 결과 처리
  recognition.value.onresult = (event) => {
    let interimTranscript = ''
    let finalTranscript = ''

    for (let i = event.resultIndex; i < event.results.length; i++) {
      const transcript = event.results[i][0].transcript
      if (event.results[i].isFinal) {
        finalTranscript += transcript
      } else {
        interimTranscript += transcript
      }
    }

    // 최종 결과를 미리보기에 추가 (최대 500자 제한)
    if (finalTranscript) {
      const newTranscript = voiceTranscript.value + (voiceTranscript.value ? ' ' : '') + finalTranscript
      if (newTranscript.length > 500) {
        voiceTranscript.value = newTranscript.substring(0, 500)
        recognition.value.stop()
        notificationStore.notifyWarning('최대 500자에 도달하여 녹음이 자동 종료되었습니다')
      } else {
        voiceTranscript.value = newTranscript
      }
    }
  }

  // 인식 시작
  recognition.value.onstart = () => {
    isRecording.value = true
  }

  // 인식 종료
  recognition.value.onend = () => {
    isRecording.value = false
  }

  // 에러 처리
  recognition.value.onerror = (event) => {
    console.error('음성 인식 오류:', event.error)
    isRecording.value = false

    if (event.error === 'not-allowed') {
      notificationStore.notifyWarning('마이크 권한이 필요합니다. 브라우저 설정에서 마이크 권한을 허용해주세요')
    }
  }
}

// 음성 인식 토글
const toggleVoiceRecognition = () => {
  if (!recognition.value) return

  if (isRecording.value) {
    recognition.value.stop()
  } else {
    // 녹음 시작 전 이전 내용 초기화
    voiceTranscript.value = ''
    recognition.value.start()
  }
}

// 녹음된 내용을 폼에 추가
const addTranscriptToForm = () => {
  if (voiceTranscript.value.trim()) {
    const currentText = formData.value.symptomDetail
    const newText = currentText + (currentText ? ' ' : '') + voiceTranscript.value

    // 500자 제한 확인
    if (newText.length > 500) {
      const remaining = 500 - currentText.length
      if (remaining > 0) {
        formData.value.symptomDetail = currentText + (currentText ? ' ' : '') + voiceTranscript.value.substring(0, remaining - 1)
        notificationStore.notifyWarning(`최대 500자까지 입력 가능합니다. ${remaining}자만 추가되었습니다`)
      } else {
        notificationStore.notifyWarning('증상 입력란이 이미 가득 찼습니다 (최대 500자)')
      }
    } else {
      formData.value.symptomDetail = newText
    }
    voiceTranscript.value = ''
  }
}

// 녹음 내용 지우기
const clearTranscript = () => {
  voiceTranscript.value = ''
}

onMounted(() => {
  initSpeechRecognition()
})

onUnmounted(() => {
  // 음성 인식 정리 (메모리 누수 방지)
  if (recognition.value) {
    recognition.value.stop()
    recognition.value = null
  }
})

// 뒤로 가기
const handleBack = () => {
  router.push({ name: 'client-landing' })
}

const handleSubmit = async () => {
  if (!isFormValid.value) return

  try {
    // TODO: API 호출로 제품 정보 저장
    // const response = await saveProductInfo({ ... })

    // 임시로 localStorage에 저장
    localStorage.setItem('clientInfo', JSON.stringify({
      product: props.productName,
      model: props.modelNumber,
      ...formData.value
    }))

    notificationStore.notifySuccess('정보가 저장되었습니다')

    // 다음 단계로 이동 (본인 확인 페이지)
    router.push('/client/consultation/verification')
  } catch (error) {
    console.error('제출 실패:', error)
    notificationStore.notifyWarning('제출 중 오류가 발생했습니다. 다시 시도해주세요')
  }
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

/* 기기 정보 카드 */
.device-card {
  background: linear-gradient(135deg, #5B7CFF 0%, #4A61D9 100%);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(91, 124, 255, 0.2);
}

.device-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.device-icon svg {
  width: 28px;
  height: 28px;
}

.device-details {
  flex: 1;
}

.device-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 4px 0;
  font-weight: 500;
}

.device-model {
  font-size: 20px;
  font-weight: 700;
  color: white;
  margin: 0 0 2px 0;
}

.device-model-number {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  font-weight: 500;
}

/* 입력 필드 */
.label-with-counter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.input-label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  display: flex;
  align-items: center;
  gap: 6px;
}

.input-label svg {
  width: 16px;
  height: 16px;
  color: #5B7CFF;
}

.optional::after {
  content: '';
}

.required::after {
  content: '*';
  color: #ff3b30;
  margin-left: 2px;
}

.char-counter {
  font-size: 13px;
  color: #86868b;
  font-weight: 500;
}

.char-counter.warning {
  color: #ff9500;
  font-weight: 600;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  border: none;
  border-radius: 12px;
  background: white;
  font-size: 15px;
  color: #1d1d1f;
  box-sizing: border-box;
  transition: box-shadow 0.2s;
}

.form-input::placeholder {
  color: #86868b;
}

.form-input:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(91, 124, 255, 0.15);
}

.form-textarea {
  width: 100%;
  padding: 14px 16px;
  border: none;
  border-radius: 12px;
  background: white;
  font-size: 15px;
  color: #1d1d1f;
  resize: vertical;
  font-family: inherit;
  box-sizing: border-box;
  transition: box-shadow 0.2s;
  line-height: 1.5;
}

.form-textarea::placeholder {
  color: #86868b;
}

.form-textarea:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(91, 124, 255, 0.15);
}

/* 음성 녹음 섹션 */
.voice-section {
  margin-top: 20px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 12px;
  border: 2px dashed #d1d5db;
}

.voice-guide {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding: 12px;
  background: white;
  border-radius: 8px;
  font-size: 15px;
  color: #4b5563;
  line-height: 1.5;
}

.guide-icon {
  width: 24px;
  height: 24px;
  color: #5B7CFF;
  flex-shrink: 0;
}

/* 녹음 버튼 */
.voice-record-button {
  width: 100%;
  padding: 18px;
  background: linear-gradient(135deg, #5B7CFF 0%, #4A61D9 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(91, 124, 255, 0.3);
}

.voice-record-button .mic-icon {
  width: 28px;
  height: 28px;
}

.voice-record-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(91, 124, 255, 0.4);
}

.voice-record-button:active {
  transform: translateY(0);
}

/* 녹음 중 상태 */
.voice-record-button.recording {
  background: linear-gradient(135deg, #ff3b30 0%, #d32f2f 100%);
  box-shadow: 0 4px 12px rgba(255, 59, 48, 0.3);
}

.voice-record-button.recording:hover {
  box-shadow: 0 6px 20px rgba(255, 59, 48, 0.4);
}

/* 녹음 중 표시 */
.recording-indicator {
  margin-top: 16px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 2px solid #ff3b30;
}

.recording-animation {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
}

.recording-dot {
  width: 12px;
  height: 12px;
  background: #ff3b30;
  border-radius: 50%;
  animation: wave 1.4s ease-in-out infinite;
}

.recording-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.recording-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes wave {
  0%, 60%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  30% {
    transform: scale(1.5);
    opacity: 0.6;
  }
}

.recording-text {
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #ff3b30;
  margin: 0;
}

/* 녹음 내용 미리보기 */
.transcript-preview {
  margin-top: 16px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 2px solid #5B7CFF;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.preview-label {
  font-size: 14px;
  font-weight: 600;
  color: #5B7CFF;
}

.preview-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-counter {
  font-size: 12px;
  color: #86868b;
  font-weight: 500;
}

.clear-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  color: #9ca3af;
  transition: color 0.2s;
}

.clear-button:hover {
  color: #ff3b30;
}

.clear-button svg {
  width: 20px;
  height: 20px;
}

.preview-text {
  font-size: 16px;
  line-height: 1.6;
  color: #1d1d1f;
  margin: 0 0 16px 0;
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
}

.confirm-button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
}

.confirm-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.confirm-button:active {
  transform: translateY(0);
}

.confirm-button svg {
  width: 20px;
  height: 20px;
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
}
</style>
