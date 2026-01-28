<template>
  <div class="landing-container">
    <!-- 브라우저 알림 배너 -->
    <BrowserNotice v-model="showBrowserNotice" />

    <!-- QR 인식 초기 화면 -->
    <div v-if="showInitialScreen" class="initial-screen">
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
      </div>
    </div>

    <!-- 제품 정보 입력 화면 -->
    <div v-else class="info-form-screen">
      <ClientInfoForm :product-name="productName" :model-number="modelNumber" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import ClientInfoForm from '@/components/client/ClientInfoForm.vue'
import BrowserNotice from '@/components/common/BrowserNotice.vue'

const route = useRoute()

// 화면 전환 상태
const showInitialScreen = ref(true)

// 브라우저 알림 표시 여부
const showBrowserNotice = ref(true)

// 제품 정보 (QR 코드에서 받아오거나 URL 파라미터로 받음)
const productName = ref('Galaxy S24 Ultra')
const modelNumber = ref('SM-S928N')

onMounted(() => {
  // URL 파라미터에서 제품 정보 가져오기 (추후 QR 코드 구현 시)
  if (route.query.product) {
    productName.value = route.query.product
  }
  if (route.query.model) {
    modelNumber.value = route.query.model
  }

  // 3-4초 후 정보 입력 화면으로 전환
  setTimeout(() => {
    showInitialScreen.value = false
  }, 3500)
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

/* QR 인식 초기 화면 */
.initial-screen {
  text-align: center;
  color: white;
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.qr-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto 40px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 20px;
  backdrop-filter: blur(10px);
}

.qr-icon svg {
  width: 100%;
  height: 100%;
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
  margin: 0 0 60px 0;
  opacity: 0.95;
  font-weight: 400;
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

.phone-icon svg {
  width: 100%;
  height: 100%;
}

.device-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  letter-spacing: -0.3px;
}

/* 정보 입력 화면 */
.info-form-screen {
  width: 100%;
  max-width: 480px;
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 모바일 최적화 */
@media (max-width: 768px) {
  .landing-container {
    padding: 0;
  }
}
</style>
