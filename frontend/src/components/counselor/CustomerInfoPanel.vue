<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 h-full flex flex-col">
    <!-- 고객 정보 섹션 -->
    <div class="bg-primary-600 text-white px-6 py-4 rounded-t-lg">
      <h3 class="text-sm font-semibold mb-1">사스케</h3>
      <p class="text-lg font-medium">{{ maskedPhone }}</p>
    </div>

    <div class="flex-1 overflow-y-auto p-6">
      <!-- 로딩 상태 -->
      <div v-if="isLoading" class="flex items-center justify-center h-full">
        <div class="text-center">
          <svg class="animate-spin h-10 w-10 text-primary-600 mx-auto mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <p class="text-sm text-gray-600">고객 정보를 불러오는 중...</p>
        </div>
      </div>

      <!-- 에러 상태 -->
      <div v-else-if="error" class="flex items-center justify-center h-full">
        <div class="text-center px-4">
          <svg class="w-12 h-12 text-red-500 mx-auto mb-4" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
          </svg>
          <p class="text-sm text-red-600 mb-2">{{ error }}</p>
          <p class="text-xs text-gray-500">기본 정보로 표시됩니다</p>
        </div>
      </div>

      <!-- 고객 정보 표시 -->
      <div v-else class="space-y-6">
        <!-- 접수 사유 정보 -->
      <section>
        <div class="flex items-center gap-2 mb-3">
          <svg class="w-5 h-5 text-gray-700" fill="currentColor" viewBox="0 0 20 20">
            <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z"/>
            <path fill-rule="evenodd" d="M4 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v11a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3zm-3 4a1 1 0 100 2h.01a1 1 0 100-2H7zm3 0a1 1 0 100 2h3a1 1 0 100-2h-3z" clip-rule="evenodd"/>
          </svg>
          <h4 class="text-sm font-semibold text-gray-900">접수 정보</h4>
        </div>

        <div class="space-y-3">
          <!-- 제품 이미지 -->
          <div v-if="customerInfo.productImage" class="flex justify-center">
            <img
              :src="customerInfo.productImage"
              :alt="customerInfo.productName"
              class="w-32 h-32 object-contain"
            />
          </div>

          <!-- 제품 정보 -->
          <div class="space-y-2 text-sm">
            <div>
              <span class="text-gray-500">제품:</span>
              <span class="ml-2 font-medium text-gray-900">{{ customerInfo.productName }}</span>
            </div>
            <div>
              <span class="text-gray-500">가전제품 / 냉장고</span>
            </div>
            <div>
              <span class="text-gray-500">모델명:</span>
              <span class="ml-2 font-medium text-gray-900">{{ customerInfo.modelNumber }}</span>
            </div>
            <div>
              <span class="text-gray-500">구매일시 / 보증:</span>
              <span class="ml-2 text-gray-900">{{ customerInfo.purchaseDate }}</span>
              <span class="ml-2 text-gray-900">보증 기간 {{ customerInfo.warrantyStatus }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 접수 증상 -->
      <section>
        <div class="flex items-center gap-2 mb-3">
          <svg class="w-5 h-5 text-red-600" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
          </svg>
          <h4 class="text-sm font-semibold text-gray-900">접수 증상</h4>
        </div>

        <div class="bg-red-50 border border-red-200 rounded-lg p-4">
          <ul class="space-y-2 text-sm text-gray-900">
            <li
              v-for="(symptom, index) in customerInfo.symptoms"
              :key="index"
              class="flex items-start gap-2"
            >
              <span class="text-red-600 mt-1">•</span>
              <span>{{ symptom }}</span>
            </li>
          </ul>
        </div>
      </section>

      <!-- 과거 상담 이력 -->
      <section>
        <div class="flex items-center gap-2 mb-3">
          <svg class="w-5 h-5 text-gray-700" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/>
          </svg>
          <h4 class="text-sm font-semibold text-gray-900">과거 상담 이력</h4>
        </div>

        <div v-if="customerInfo.consultationHistory && customerInfo.consultationHistory.length > 0" class="space-y-2">
          <div
            v-for="(history, index) in customerInfo.consultationHistory"
            :key="index"
            class="bg-gray-50 border border-gray-200 rounded-lg p-3 text-sm"
          >
            <div class="font-medium text-gray-900">{{ history.date }}</div>
            <div class="text-gray-600 mt-1">
              <span class="font-medium">{{ history.agent }}</span>
              <span class="ml-2">{{ history.summary }}</span>
            </div>
          </div>
        </div>

        <div v-else class="text-sm text-gray-500 text-center py-4">
          과거 상담 이력이 없습니다.
        </div>
      </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  customerInfo: {
    type: Object,
    default: () => ({
      phone: '010-1234-5678',
      productName: '삼성',
      modelNumber: 'SSAFY-E106',
      purchaseDate: '2026.01.12',
      warrantyStatus: '이내',
      productImage: null,
      symptoms: [],
      consultationHistory: []
    })
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: null
  }
})

// 전화번호 마스킹 (010-****-5678)
const maskedPhone = computed(() => {
  if (!props.customerInfo.phone) return '010-****-****'

  const parts = props.customerInfo.phone.split('-')
  if (parts.length === 3) {
    return `${parts[0]}-****-${parts[2]}`
  }
  return props.customerInfo.phone
})
</script>

<style scoped>
/* CustomerInfoPanel 전용 스타일 */
.scrollbar-thin::-webkit-scrollbar {
  width: 6px;
}

.scrollbar-thin::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.scrollbar-thin::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

.scrollbar-thin::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>
