<template>
  <div class="call-view min-h-screen bg-gradient-to-b from-blue-50 to-gray-50">
    <!-- 통화 헤더 -->
    <div class="call-header bg-white shadow-sm p-4">
      <div class="container mx-auto flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center">
            <svg class="w-6 h-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
          </div>
          <div>
            <h2 class="text-lg font-semibold text-gray-900">
              {{ customerName || '고객' }}
            </h2>
            <p class="text-sm text-gray-500">통화 중</p>
          </div>
        </div>
        <div class="flex items-center gap-4">
          <!-- 통화 시간 -->
          <div class="text-right">
            <p class="text-2xl font-mono font-semibold text-gray-900">
              {{ formattedCallDuration }}
            </p>
            <p class="text-xs text-gray-500">통화 시간</p>
          </div>
          <!-- 통화 상태 표시 -->
          <div class="flex items-center gap-2">
            <div class="w-3 h-3 bg-green-500 rounded-full animate-pulse"></div>
            <span class="text-sm text-gray-600">연결됨</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 메인 콘텐츠 영역 -->
    <div class="container mx-auto py-6 px-4">
      <div class="grid grid-cols-3 gap-6">
        <!-- 좌측: 고객 정보 (1/3) -->
        <div class="col-span-1 space-y-4">
          <div class="card">
            <h3 class="text-lg font-semibold mb-4">고객 정보</h3>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-600">이름</span>
                <span class="font-medium">{{ customerName }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">연락처</span>
                <span class="font-medium">{{ customerPhone }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">제품명</span>
                <span class="font-medium">{{ productName }}</span>
              </div>
            </div>
          </div>

          <!-- 욕설 카운트 표시 -->
          <div v-if="profanityCount > 0" class="card border-2 border-red-200 bg-red-50">
            <div class="flex items-center justify-between">
              <div>
                <h4 class="text-sm font-semibold text-red-800">욕설 감지</h4>
                <p class="text-xs text-red-600">{{ profanityCount }}/3회</p>
              </div>
              <div class="text-2xl font-bold text-red-600">
                {{ profanityCount }}
              </div>
            </div>
            <div class="mt-2 w-full bg-red-200 rounded-full h-2">
              <div
                class="bg-red-600 h-2 rounded-full transition-all duration-300"
                :style="{ width: `${(profanityCount / 3) * 100}%` }"
              ></div>
            </div>
          </div>
        </div>

        <!-- 중앙: STT 텍스트 영역 (2/3) -->
        <div class="col-span-2">
          <div class="card h-[600px] flex flex-col">
            <h3 class="text-lg font-semibold mb-4">실시간 대화 내용</h3>
            <div class="flex-1 overflow-y-auto scrollbar-thin space-y-3">
              <!-- STT 텍스트가 여기 표시됨 (임시 데이터) -->
              <div class="text-sm text-gray-500 text-center py-8">
                통화 중 대화 내용이 여기에 표시됩니다
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 통화 컨트롤 버튼 -->
    <CallControls
      @mute-changed="handleMuteChanged"
      @call-ended="handleCallEnded"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCallStore } from '@/stores/call'
import { useCustomerStore } from '@/stores/customer'
import CallControls from '@/components/call/CallControls.vue'

const router = useRouter()
const callStore = useCallStore()
const customerStore = useCustomerStore()

// 통화 시간 타이머
const callDuration = ref(0)
let timerInterval = null

// 고객 정보 computed
const customerName = computed(() => customerStore.currentCustomer.name || '고객')
const customerPhone = computed(() => customerStore.currentCustomer.phone || '-')
const productName = computed(() => customerStore.currentCustomer.productInfo.productName || '-')
const profanityCount = computed(() => callStore.currentCall.profanityCount)

// 통화 시간 포맷팅
const formattedCallDuration = computed(() => {
  const hours = Math.floor(callDuration.value / 3600)
  const minutes = Math.floor((callDuration.value % 3600) / 60)
  const seconds = callDuration.value % 60

  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

// 음소거 변경 핸들러
const handleMuteChanged = (isMuted) => {
  console.log('음소거 상태:', isMuted)
}

// 통화 종료 핸들러
const handleCallEnded = (callData) => {
  console.log('통화 종료:', callData)

  // 타이머 정리
  if (timerInterval) {
    clearInterval(timerInterval)
  }

  // 통화 기록 페이지로 이동 (나중에 구현)
  // router.push({ name: 'call-record', params: { callId: callData.callId } })
  alert('통화가 종료되었습니다. 상담 기록 작성 화면으로 이동합니다.')
}

// 컴포넌트 마운트 시 타이머 시작
onMounted(() => {
  // 테스트용 고객 정보 설정
  if (!customerStore.hasCustomerInfo) {
    customerStore.setCustomerInfo({
      id: 'customer-001',
      name: '홍길동',
      phone: '010-1234-5678',
      productInfo: {
        modelName: 'ABC-1000',
        productName: '세탁기',
        purchaseDate: '2024-01-15',
        warrantyPeriod: '2026-01-15'
      },
      issue: {
        symptom: '세탁기가 작동하지 않습니다',
        errorCode: 'E01'
      }
    })
  }

  // 테스트용 통화 시작
  if (!callStore.isInCall) {
    callStore.startCall({
      id: 'test-call-001',
      customerId: 'customer-001',
      roomToken: 'test-token'
    })
  }

  // 통화 시간 타이머
  timerInterval = setInterval(() => {
    callDuration.value++
  }, 1000)
})

// 컴포넌트 언마운트 시 타이머 정리
onUnmounted(() => {
  if (timerInterval) {
    clearInterval(timerInterval)
  }
})
</script>

<style scoped>
/* 추가 스타일이 필요하면 여기에 */
</style>
