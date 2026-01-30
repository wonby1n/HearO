<template>
  <div class="landing-container">
    <!-- 브라우저 알림 배너 -->
    <BrowserNotice v-model="showBrowserNotice" />

    <!-- 1. 제품 확인 중 (로딩) -->
    <div v-if="isLoading" class="initial-screen loading-state">
      <div class="spinner"></div>
      <p class="loading-text">제품 정보를 확인하고 있습니다...</p>
    </div>

    <!-- 2. 제품 정보 오류 화면 (productId가 없거나 유효하지 않을 때) -->
    <div v-else-if="isError" class="initial-screen error-state">
      <div class="error-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="12" r="10" stroke="white" stroke-width="2"/>
          <path d="M12 8V12M12 16H12.01" stroke="white" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <h1 class="title">제품 확인 불가</h1>
      <p class="subtitle">
        제품 정보가 유효하지 않거나 확인할 수 없습니다.<br>
        정확한 상담을 위해 고객센터를 통한 접수가 필요합니다.<br>
        대표 번호로 연락 주시면 신속히 도와드리겠습니다.
      </p>
      
    </div>

    <!-- 3. QR 인식 초기 성공 화면 -->
    <div v-else-if="showInitialScreen" class="initial-screen">
      <div class="qr-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="3" y="3" width="8" height="8" rx="1" stroke="white" stroke-width="2"/>
          <rect x="13" y="3" width="8" height="8" rx="1" stroke="white" stroke-width="2"/>
          <rect x="3" y="13" width="8" height="8" rx="1" stroke="white" stroke-width="2"/>
          <rect x="5" y="5" width="4" height="4" fill="white"/>
          <rect x="15" y="5" width="4" height="4" fill="white"/>
          <rect x="5" y="15" width="4" height="4" fill="white"/>
          <path d="M13 13H15V15H13V13Z" fill="white"/>
          <path d="M17 13H19V15H17V13Z" fill="white"/>
          <path d="M13 17H15V19H13V17Z" fill="white"/>
          <path d="M17 17H21V21H17V17Z" fill="white"/>
          <rect x="19" y="19" width="2" height="2" fill="#5B7CFF"/>
        </svg>
      </div>

      <h1 class="title">Smart AS Connect</h1>
      <p class="subtitle">
        QR 코드를 통해 제품이 확인되었습니다.<br>
        상담 접수를 시작합니다.
      </p>

      <div class="device-info">
        <div class="phone-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="6" y="2" width="12" height="20" rx="2" stroke="white" stroke-width="2"/>
            <path d="M10 19H14" stroke="white" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <p class="device-name">{{ productName }}</p>
        <div v-if="manufacturedAt" class="device-dates">
          <span>제조일자 : {{ manufacturedAt }}</span>
          <span class="divider">|</span>
          <span>보증기간 : {{ warrantyEndsAt }}</span>
        </div>
      </div>
    </div>

    <!-- 4. 제품 정보 입력 화면 -->
    <div v-else class="info-form-screen">
      <ClientInfoForm 
        :product-name="productName" 
        :model-number="modelNumber"
        :manufactured-at="manufacturedAt"
        :warranty-ends-at="warrantyEndsAt"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ClientInfoForm from '@/components/client/ClientInfoForm.vue'
import BrowserNotice from '@/components/common/BrowserNotice.vue'
import { fetchProductById } from '@/services/productService'

const route = useRoute()
const router = useRouter()

// 화면 상태 제어
const showInitialScreen = ref(true)
const showBrowserNotice = ref(true)
const isLoading = ref(true)
const isError = ref(false)

// 데이터 정보
const productName = ref('')
const modelNumber = ref('')
const manufacturedAt = ref('')
const warrantyEndsAt = ref('')

/**
 * 직접 입력 모드로 전환
 */
const goToManualInput = () => {
  productName.value = '' // 직접 입력을 위해 초기화
  modelNumber.value = ''
  isError.value = false
  showInitialScreen.value = false
}

/**
 * 홈페이지로 이동 (예외 상황)
 */
const handleBackToHome = () => {
  router.push('/')
}

/**
 * API에서 제품 정보 가져오기
 */
const loadProductInfo = async (productId) => {
  isLoading.value = true
  isError.value = false

  try {
    const productData = await fetchProductById(productId)
    if (!productData) throw new Error('Product not found')
    
    productName.value = productData.name
    modelNumber.value = productData.code

    localStorage.setItem('clientProductId', productId)
    
    // 성공 시 3초 대기 후 전환
    setTimeout(() => {
      showInitialScreen.value = false
    }, 3000)

  } catch (error) {
    console.error('제품 정보 로드 실패:', error)
    isError.value = true
  } finally {
    isLoading.value = false
  }
}

onMounted(async () => {
  const { productId, manufacturedAt: mDate, warrantyEndsAt: wDate } = route.query

  // 날짜 정보 저장
  manufacturedAt.value = mDate || ''
  warrantyEndsAt.value = wDate || ''
  if (mDate) localStorage.setItem('clientManufacturedAt', mDate)
  if (wDate) localStorage.setItem('clientWarrantyEndsAt', wDate)

  // 1. productId 유무 검사
  if (!productId) {
    isLoading.value = false
    isError.value = true
    return
  }

  // 2. productId가 있으면 정보 로드
  await loadProductInfo(productId)
})
</script>

<style scoped>
.landing-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  position: relative;
}

.initial-screen {
  text-align: center;
  color: white;
  animation: fadeIn 0.5s ease-in;
  width: 100%;
  max-width: 400px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 로딩 상태 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top: 4px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 에러 상태 */
.error-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
}

.error-icon svg {
  width: 100%;
  height: 100%;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 40px;
}

.manual-button {
  width: 100%;
  padding: 16px;
  background: white;
  color: #764ba2;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s;
}

.retry-button {
  width: 100%;
  padding: 16px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  font-size: 15px;
  cursor: pointer;
}

.manual-button:active {
  transform: scale(0.98);
}

/* 기존 스타일 */
.qr-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto 40px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 20px;
  backdrop-filter: blur(10px);
}

.title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 16px 0;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 16px;
  line-height: 1.6;
  margin: 0 0 40px 0;
  opacity: 0.95;
}

.device-info {
  margin-top: 60px;
}

.phone-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 16px;
  opacity: 0.9;
}

.device-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.device-dates {
  font-size: 14px;
  opacity: 0.8;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.divider {
  font-size: 10px;
}

.info-form-screen {
  width: 100%;
  max-width: 480px;
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(100%); }
  to { opacity: 1; transform: translateX(0); }
}

@media (max-width: 768px) {
  .landing-container { padding: 0; }
}
</style>