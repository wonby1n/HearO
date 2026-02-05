<template>
  <div class="bg-white rounded-2xl shadow-lg border border-gray-200 h-full flex flex-col">
    <!-- 고객 정보 섹션 -->
    <div class="px-5 py-4 border-b border-gray-100 bg-gradient-to-r from-primary-50 to-blue-50 rounded-t-2xl">
      <h3 class="text-lg font-bold text-gray-900 mb-1">{{ customerInfo.customerName || '고객 정보 없음' }}</h3>
      <p class="text-base font-medium text-gray-700">{{ maskedPhone }}</p>
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
          <div class="flex justify-center mb-4">
            <img
              v-if="productImg && !imageLoadFailed"
              :src="productImg"
              :alt="customerInfo.productName"
              class="w-40 h-40 object-contain rounded-md border border-gray-100 shadow-sm"
              @error="handleImageError"
            />
            <div v-else class="w-40 h-40 flex items-center justify-center bg-gray-100 rounded-md border border-gray-200">
              <svg class="w-16 h-16 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clip-rule="evenodd"/>
              </svg>
            </div>
          </div>

          <!-- 제품 정보 -->
          <div class="space-y-2 text-sm">
            <div>
              <span class="text-gray-500">제품:</span>
              <span class="ml-2 font-medium text-gray-900">{{ customerInfo.productName || '정보 없음' }}</span>
            </div>
            <div>
              <span class="text-gray-500">카테고리:</span>
              <span class="ml-2 font-medium text-gray-900">{{ customerInfo.productCategory || '정보 없음' }}</span>
            </div>
            <div>
              <span class="text-gray-500">모델명:</span>
              <span class="ml-2 font-medium text-gray-900">{{ customerInfo.modelCode || '정보 없음' }}</span>
            </div>
            <div>
              <span class="text-gray-500">구매일시:</span>
              <span class="ml-2 text-gray-900">{{ customerInfo.purchaseDate || '정보 없음' }}</span>
            </div>
            <div>
              <span class="text-gray-500">보증기간:</span>
              <span
                class="ml-2 font-semibold"
                :class="warrantyClass"
              >
                {{ warrantyStatusText }}
              </span>
              <span v-if="customerInfo.warrantyStatus?.endDate" class="ml-2 text-xs text-gray-500">
                ({{ customerInfo.warrantyStatus.endDate }} 까지)
              </span>
            </div>
          </div>
        </div>
      </section>

      <!-- 접수 증상 -->
      <section>
        <div class="flex items-center gap-2 mb-3">
          <svg class="w-5 h-5 text-primary-600" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
          </svg>
          <h4 class="text-sm font-bold text-gray-900">접수 증상</h4>
        </div>

        <div class="bg-primary-50 border border-primary-200 rounded-xl p-4">
          <!-- 에러 코드 (있는 경우) -->
          <div v-if="customerInfo.errorCode && customerInfo.errorCode !== '정보 없음'" class="mb-3 pb-3 border-b border-primary-200">
            <span class="text-xs font-medium text-gray-600">에러 코드</span>
            <span class="ml-2 font-mono font-bold text-primary-700">{{ customerInfo.errorCode }}</span>
          </div>

          <!-- 증상 목록 -->
          <ul v-if="customerInfo.symptoms && customerInfo.symptoms.length > 0" class="space-y-2">
            <li
              v-for="(symptom, index) in customerInfo.symptoms"
              :key="index"
              class="flex items-start gap-2 text-sm text-gray-800"
            >
              <span class="text-primary-600 font-bold mt-0.5">•</span>
              <span class="font-medium">{{ symptom }}</span>
            </li>
          </ul>
          <div v-else class="text-sm text-gray-500 text-center py-2">
            증상 정보가 없습니다.
          </div>
        </div>
      </section>

      <!-- 과거 상담 이력 -->
      <section>
        <div class="flex items-center gap-2 mb-3">
          <svg class="w-5 h-5 text-primary-600" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/>
          </svg>
          <h4 class="text-sm font-bold text-gray-900">과거 상담 이력</h4>
        </div>

        <div v-if="customerInfo.consultationHistory && customerInfo.consultationHistory.length > 0" class="space-y-2">
          <div
            v-for="(history, index) in customerInfo.consultationHistory.slice(0, 3)"
            :key="index"
            class="bg-gray-50 border border-gray-200 rounded-lg overflow-hidden"
          >
            <!-- 기본 정보 (클릭 가능) -->
            <div 
              @click="toggleHistory(index)"
              class="p-3 cursor-pointer hover:bg-gray-100 transition-colors"
            >
              <div class="flex items-center justify-between mb-1">
                <div class="flex items-center gap-2">
                  <svg 
                    class="w-4 h-4 text-gray-500 transition-transform"
                    :class="{ 'rotate-90': expandedHistory[index] }"
                    fill="none" 
                    stroke="currentColor" 
                    viewBox="0 0 24 24"
                  >
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
                  </svg>
                  <div class="font-medium text-gray-900">{{ formatHistoryDate(history.createdAt) }}</div>
                </div>
                <div class="flex items-center gap-1">
                  <span v-if="history.durationSeconds" class="text-xs text-gray-500">{{ formatHistoryDuration(history.durationSeconds) }}</span>
                  <span
                    v-if="history.terminationReason === 'PROFANITY_LIMIT' || history.terminationReason === 'AGGRESSION_LIMIT'"
                    class="px-2 py-0.5 bg-red-100 text-red-700 text-xs font-bold rounded-full"
                    :title="getTerminationReasonText(history.terminationReason)"
                  >
                    ⚠️
                  </span>
                </div>
              </div>
              <div class="text-gray-700 font-medium text-sm">{{ history.title || '상담 내용 없음' }}</div>
            </div>

            <!-- 상세 정보 (확장 시) -->
            <div v-if="expandedHistory[index]" class="px-3 pb-3 pt-0 border-t border-gray-200 bg-white">
              <div class="space-y-2 mt-2">
                <!-- 접수 증상 -->
                <div v-if="history.symptom">
                  <div class="text-xs font-semibold text-gray-600 mb-1">접수 증상</div>
                  <div class="text-sm text-gray-800 bg-gray-50 p-2 rounded">{{ history.symptom }}</div>
                </div>

                <!-- 에러 코드 -->
                <div v-if="history.errorCode">
                  <div class="text-xs font-semibold text-gray-600 mb-1">에러 코드</div>
                  <div class="text-sm font-mono font-semibold text-primary-700 bg-primary-50 p-2 rounded">{{ history.errorCode }}</div>
                </div>

                <!-- 종료 사유 -->
                <div>
                  <div class="text-xs font-semibold text-gray-600 mb-1">종료 사유</div>
                  <div class="text-sm text-gray-800">
                    <span 
                      class="inline-block px-2 py-1 rounded text-xs font-medium"
                      :class="getTerminationReasonClass(history.terminationReason)"
                    >
                      {{ getTerminationReasonText(history.terminationReason) }}
                    </span>
                  </div>
                </div>

                <!-- 욕설 횟수 -->
                <div v-if="history.profanityCount > 0">
                  <div class="text-xs font-semibold text-gray-600 mb-1">욕설 감지</div>
                  <div class="text-sm text-red-700 font-semibold">{{ history.profanityCount }}회</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="bg-primary-50 border border-primary-200 rounded-xl p-4 text-sm text-gray-500 text-center">
          과거 상담 이력이 없습니다.
        </div>
      </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 확장된 상담 이력 인덱스 추적
const expandedHistory = ref({})

const toggleHistory = (index) => {
  expandedHistory.value[index] = !expandedHistory.value[index]
}

const props = defineProps({
  customerInfo: {
    type: Object,
    default: () => ({
      customerName: '고객 정보 없음',
      phone: '010-1234-5678',
      productName: '정보 없음',
      productCategory: '정보 없음',
      modelCode: 'SSAFY-E106',
      modelNumber: 'SSAFY-E106',
      purchaseDate: '2026.01.12',
      warrantyStatus: { status: '정보 없음', isExpired: null, endDate: null },
      productImage: null,
      errorCode: null,
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

// ✅ 제품 이미지 경로 계산
const productImg = computed(() => {
  const url = props.customerInfo.productImage
  return url ? `https://i14e106.p.ssafy.io${url}` : null
})

// 고객 이름 마스킹


// 전화번호 마스킹 (010-****-5678)
const maskedPhone = computed(() => {
  if (!props.customerInfo.phone || props.customerInfo.phone === '정보 없음') {
    return '010-****-****'
  }

  // 하이픈 제거 후 숫자만 추출
  const phoneNumber = props.customerInfo.phone.replace(/\D/g, '')
  
  // 전화번호 길이 검증 (10자리 또는 11자리)
  if (phoneNumber.length === 11) {
    // 010-****-5678 형식으로 마스킹
    const first = phoneNumber.substring(0, 3)  // 010
    const last = phoneNumber.substring(7)      // 마지막 4자리
    return `${first}-****-${last}`
  } else if (phoneNumber.length === 10) {
    // 02-****-5678 형식으로 마스킹 (서울 지역번호)
    const first = phoneNumber.substring(0, 2)
    const last = phoneNumber.substring(6)
    return `${first}-****-${last}`
  }
  
  // 형식이 맞지 않으면 원본 반환
  return props.customerInfo.phone
})

// 보증 상태 텍스트
const warrantyStatusText = computed(() => {
  const warranty = props.customerInfo.warrantyStatus
  if (!warranty || warranty.status === '정보 없음') {
    return '정보 없음'
  }
  return warranty.isExpired ? '보증기간 만료' : '보증기간 이내'
})

// 상태별 스타일 클래스
const warrantyClass = computed(() => {
  const warranty = props.customerInfo.warrantyStatus
  if (!warranty || warranty.status === '정보 없음') {
    return 'text-gray-900'
  }
  return warranty.isExpired ? 'text-red-600' : 'text-blue-600'
})

// 이미지 로드 실패 시 처리
const imageLoadFailed = ref(false)
const handleImageError = () => {
  imageLoadFailed.value = true
  console.warn('제품 이미지 로드 실패:', props.customerInfo.productImage)
}

// 과거 상담 이력 날짜 포맷팅
const formatHistoryDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).replace(/\. /g, '.').replace('.', '')
}

// 과거 상담 이력 통화 시간 포맷팅
const formatHistoryDuration = (seconds) => {
  if (seconds === null || seconds === undefined) return '-'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  if (hours > 0) {
    return `${hours}시간 ${minutes}분`
  } else if (minutes > 0) {
    return `${minutes}분 ${secs}초`
  } else {
    return `${secs}초`
  }
}

// 종료 사유 텍스트
const getTerminationReasonText = (reason) => {
  const reasonMap = {
    NORMAL: '정상 종료',
    CUSTOMER_DISCONNECT: '고객 종료',
    COUNSELOR_DISCONNECT: '상담사 종료',
    BLACKLIST: '블랙리스트',
    TIMEOUT: '시간 초과',
    ERROR: '오류',
    PROFANITY_LIMIT: '욕설 제한',
    AGGRESSION_LIMIT: '공격성 제한'
  }
  return reasonMap[reason] || reason
}

// 종료 사유별 스타일 클래스
const getTerminationReasonClass = (reason) => {
  if (reason === 'PROFANITY_LIMIT' || reason === 'AGGRESSION_LIMIT') {
    return 'bg-red-100 text-red-700'
  } else if (reason === 'NORMAL' || reason === 'CUSTOMER_DISCONNECT') {
    return 'bg-green-100 text-green-700'
  } else if (reason === 'TIMEOUT' || reason === 'ERROR') {
    return 'bg-yellow-100 text-yellow-700'
  } else {
    return 'bg-gray-100 text-gray-700'
  }
}
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
