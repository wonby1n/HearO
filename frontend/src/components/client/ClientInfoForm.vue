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
          <p class="section-description">
            {{ productName ? '아래 자동 인식된 모델이 맞는지 확인해주세요.' : '제품 정보가 없습니다. 상담 시 상세히 말씀해주세요.' }}
          </p>

          <div class="device-card" :class="{ 'no-data': !productName }">
            <div class="device-visual">
              <img v-if="productImg" :src="productImg" :alt="productName" class="product-image" />
              
              <div v-else class="device-icon">
                <!-- 냉장고 아이콘 -->
                <svg v-if="category === 'REFRIGERATOR'" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M5 2H19V22H5V2Z" stroke="white" stroke-width="2" stroke-linejoin="round"/>
                  <path d="M5 10H19" stroke="white" stroke-width="2" stroke-linecap="round"/>
                  <path d="M9 6V8" stroke="white" stroke-width="2" stroke-linecap="round"/>
                  <path d="M9 14V18" stroke="white" stroke-width="2" stroke-linecap="round"/>
                </svg>

                <!-- 세탁기 아이콘 -->
                <svg v-else-if="category === 'WASHING_MACHINE'" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="4" y="2" width="16" height="20" rx="2" stroke="white" stroke-width="2"/>
                  <circle cx="7" cy="5" r="0.5" fill="white"/>
                  <circle cx="9" cy="5" r="0.5" fill="white"/>
                  <circle cx="12" cy="13" r="5" stroke="white" stroke-width="2"/>
                  <path d="M12 10V16M9 13H15" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
                </svg>

                <!-- TV 아이콘 -->
                <svg v-else-if="category === 'TV'" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="2" y="6" width="20" height="13" rx="2" stroke="white" stroke-width="2"/>
                  <path d="M8 22H16" stroke="white" stroke-width="2" stroke-linecap="round"/>
                  <path d="M12 19V22" stroke="white" stroke-width="2" stroke-linecap="round"/>
                </svg>

                <!-- 에어컨 아이콘 -->
                <svg v-else-if="category === 'AIR_CONDITIONER'" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="3" y="4" width="18" height="8" rx="2" stroke="white" stroke-width="2"/>
                  <path d="M7 12V15M12 12V17M17 12V15" stroke="white" stroke-width="2" stroke-linecap="round"/>
                  <path d="M5 15L3 17M7 17L5 19M10 17L8 19" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
                  <path d="M14 19L16 17M17 19L19 17M19 15L21 17" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
                </svg>

                <!-- 스마트폰 아이콘 (기본값) -->
                <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="6" y="2" width="12" height="20" rx="2" stroke="white" stroke-width="2"/>
                  <path d="M10 19H14" stroke="white" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
            </div>

            <div class="device-details">
              <p class="device-label">{{ productName ? '자동 인식된 모델' : '제품 정보 직접 입력' }}</p>
              <p class="device-model">{{ productName || '정보를 확인할 수 없음' }}</p>
              <p class="device-model-number">{{ modelNumber || '' }}</p>
              
              <!-- 날짜 정보 표시 (값이 있을 때만) -->
              <div v-if="manufacturedAt" class="device-date-info">
                <span>제조일자 : {{ manufacturedAt }}</span>
                <span class="date-divider">|</span>
                <span>보증기간 : {{ warrantyEndsAt }}</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 에러코드 (선택) -->
        <section class="form-section">
          <h2 class="section-title py-2">에러코드 및 증상 입력</h2>
          <div class="label-with-counter py-2">
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
            placeholder="언제부터, 어떤 문제가 발생했는지 상세히 적어주세요."
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
                <button type="button" class="clear-button" @click="clearTranscript">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                </button>
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
  productName: { type: String, required: true },
  modelNumber: { type: String, required: true },
  imageUrl: { type: String, default: '' },
  category: { type: String, default: '' },
  manufacturedAt: { type: String, default: '' },
  warrantyEndsAt: { type: String, default: '' }
})

const productImg = computed(() => {
  // 이미지 비활성화 - 아이콘만 표시
  return null;
})

const router = useRouter()
const notificationStore = useNotificationStore()

const showBrowserNotice = ref(true)
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

const initSpeechRecognition = () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SpeechRecognition) {
    speechSupported.value = false
    return
  }

  speechSupported.value = true
  recognition.value = new SpeechRecognition()
  recognition.value.lang = 'ko-KR'
  recognition.value.interimResults = true
  recognition.value.continuous = true

  recognition.value.onresult = (event) => {
    let finalTranscript = ''
    for (let i = event.resultIndex; i < event.results.length; i++) {
      if (event.results[i].isFinal) {
        finalTranscript += event.results[i][0].transcript
      }
    }
    if (finalTranscript) {
      voiceTranscript.value = (voiceTranscript.value + (voiceTranscript.value ? ' ' : '') + finalTranscript).trim().substring(0, 500)
    }
  }

  recognition.value.onstart = () => isRecording.value = true
  recognition.value.onend = () => isRecording.value = false
  recognition.value.onerror = (event) => {
    console.error('음성 인식 오류:', event.error)
    isRecording.value = false
  }
}

const toggleVoiceRecognition = () => {
  if (!recognition.value) return
  isRecording.value ? recognition.value.stop() : (voiceTranscript.value = '', recognition.value.start())
}

const addTranscriptToForm = () => {
  if (voiceTranscript.value.trim()) {
    formData.value.symptomDetail = (formData.value.symptomDetail + (formData.value.symptomDetail ? ' ' : '') + voiceTranscript.value).trim().substring(0, 500)
    voiceTranscript.value = ''
  }
}

const clearTranscript = () => voiceTranscript.value = ''

onMounted(() => initSpeechRecognition())
onUnmounted(() => { if (recognition.value) recognition.value.stop() })

const handleBack = () => router.push({ name: 'client-landing' })

const handleSubmit = async () => {
  if (!isFormValid.value) return
  
  const productId = localStorage.getItem('clientProductId')

  const consultationData = {
    symptom: formData.value.symptomDetail,
    errorCode: formData.value.errorCode || '',
    productId: productId ? parseInt(productId) : null,
    manufacturedAt: props.manufacturedAt,
    warrantyEndsAt: props.warrantyEndsAt
  }

  localStorage.setItem('clientConsultationData', JSON.stringify(consultationData))
  router.push('/client/consultation/verification')
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

.device-card {
  background: linear-gradient(135deg, #5B7CFF 0%, #4A61D9 100%);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(91, 124, 255, 0.2);
}

.device-card.no-data {
  background: linear-gradient(135deg, #86868b 0%, #52525b 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.device-visual {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.device-icon svg {
  width: 32px;
  height: 32px;
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
  margin: 0 0 4px 0;
  font-weight: 500;
}

.device-date-info {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
  display: flex;
  align-items: center;
  gap: 6px;
}

.date-divider {
  font-size: 10px;
  opacity: 0.5;
}

.label-with-counter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.input-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
}

.input-label svg {
  width: 16px;
  height: 16px;
  color: #5B7CFF;
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

.form-input, .form-textarea {
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

.form-textarea {
  resize: vertical;
  line-height: 1.5;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(91, 124, 255, 0.15);
}

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
}

.guide-icon {
  width: 24px;
  height: 24px;
  color: #5B7CFF;
  flex-shrink: 0;
}

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
  box-shadow: 0 4px 12px rgba(91, 124, 255, 0.3);
}

.voice-record-button .mic-icon {
  width: 20px;
  height: 20px;
}

.voice-record-button.recording {
  background: linear-gradient(135deg, #ff3b30 0%, #d32f2f 100%);
}

.submit-button {
  width: 100%;
  padding: 18px;
  background: linear-gradient(135deg, #5B7CFF 0%, #4A61D9 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 17px;
  font-weight: 700;
  margin-top: 32px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(91, 124, 255, 0.3);
}

.submit-button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.recording-indicator {
  margin-top: 16px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 2px solid #ff3b30;
}

.transcript-preview {
  margin-top: 16px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 2px solid #5B7CFF;
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
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .form-content {
    padding: 20px 16px;
  }
}
</style>