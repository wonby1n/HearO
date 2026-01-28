<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 자동 종료 모달 -->
    <AutoTerminationModal
      :show="showAutoTerminationModal"
      @confirm="handleAutoTerminationConfirm"
      @close="handleAutoTerminationConfirm"
    />

    <!-- 상단 헤더 -->
    <header class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-[1920px] mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <!-- 좌측: 로고 -->
          <div class="flex items-center gap-3">
            <h1 class="text-2xl font-bold text-primary-600">HearO</h1>
          </div>

          <!-- 중앙: 타이머 + 통화 상태 -->
          <CallTimer :isActive="isCallActive" />

          <!-- 우측: 통화 컨트롤 버튼 -->
          <CounselorCallControls
            :isMuted="isMuted"
            :isPaused="isPaused"
            @mute-changed="handleMuteChanged"
            @pause-changed="handlePauseChanged"
            @call-ended="handleEndCall"
          />
        </div>
      </div>
    </header>

    <!-- 메인 컨텐츠 (3-column 레이아웃) -->
    <main class="max-w-[1920px] mx-auto p-6">
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 h-[calc(100vh-140px)]">
        <!-- 좌측: 고객 정보 패널 (3 columns) -->
        <div class="lg:col-span-3">
          <CustomerInfoPanel
            :customerInfo="customerInfo"
            :isLoading="isLoadingCustomerInfo"
            :error="customerInfoError"
          />
        </div>

        <!-- 중앙: STT 자막 영역 (6 columns) -->
        <div class="lg:col-span-6">
          <STTChatPanel
            :messages="sttMessages"
            @toggle-profanity="handleToggleProfanity"
          />
        </div>

        <!-- 우측: AI 가이드 (3 columns) -->
        <div class="lg:col-span-3">
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 h-full p-6">
            <div class="flex items-center gap-2 mb-4">
              <svg class="w-5 h-5 text-primary-600" fill="currentColor" viewBox="0 0 20 20">
                <path d="M11 3a1 1 0 10-2 0v1a1 1 0 102 0V3zM15.657 5.757a1 1 0 00-1.414-1.414l-.707.707a1 1 0 001.414 1.414l.707-.707zM18 10a1 1 0 01-1 1h-1a1 1 0 110-2h1a1 1 0 011 1zM5.05 6.464A1 1 0 106.464 5.05l-.707-.707a1 1 0 00-1.414 1.414l.707.707zM5 10a1 1 0 01-1 1H3a1 1 0 110-2h1a1 1 0 011 1zM8 16v-1h4v1a2 2 0 11-4 0zM12 14c.015-.34.208-.646.477-.859a4 4 0 10-4.954 0c.27.213.462.519.476.859h4.002z"/>
              </svg>
              <h3 class="text-lg font-semibold text-gray-900">AI 가이드</h3>
            </div>

            <!-- 검색 바 -->
            <div class="mb-6">
              <div class="relative">
                <input
                  type="text"
                  placeholder="메뉴얼 / FAQ 검색"
                  class="w-full px-4 py-2 pl-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                  v-model="searchQuery"
                />
                <svg class="w-5 h-5 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
                </svg>
              </div>
            </div>

            <!-- 추천 솔루션 -->
            <section class="mb-6">
              <h4 class="text-sm font-semibold text-gray-900 mb-3">추천 솔루션</h4>
              <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <ul class="space-y-2 text-sm text-gray-900">
                  <li class="flex items-start gap-2">
                    <span class="text-blue-600 mt-0.5">1.</span>
                    <span>전원 플러그 확인 및 재부팅</span>
                  </li>
                  <li class="flex items-start gap-2">
                    <span class="text-blue-600 mt-0.5">2.</span>
                    <span>온도 센서 점검 필요</span>
                  </li>
                  <li class="flex items-start gap-2">
                    <span class="text-blue-600 mt-0.5">3.</span>
                    <span>다시 문을 제대로 닫기</span>
                  </li>
                </ul>
              </div>
            </section>

            <!-- 추천 응대 스크립트 -->
            <section class="mb-6">
              <h4 class="text-sm font-semibold text-gray-900 mb-3">추천 응대 스크립트</h4>
              <div class="bg-gray-50 border border-gray-200 rounded-lg p-4 mb-3">
                <p class="text-sm text-gray-900 leading-relaxed">
                  "고객님, 우선 냉장고 전원을 완전히 끄고 5분 정도 기다린 후 다시 켜주시겠어요?"
                </p>
              </div>
              <div class="flex gap-2">
                <button class="flex-1 px-4 py-2 bg-primary-600 text-white rounded-lg text-sm font-medium hover:bg-primary-700 transition-colors">
                  응대제안 사용
                </button>
                <button class="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-300 transition-colors">
                  로그 저장
                </button>
              </div>
            </section>

            <!-- 메모 영역 -->
            <section>
              <h4 class="text-sm font-semibold text-gray-900 mb-3">메모</h4>
              <textarea
                v-model="memo"
                placeholder="상담 메모를 입력하세요..."
                class="w-full h-32 px-3 py-2 border border-gray-300 rounded-lg resize-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 text-sm"
              ></textarea>
            </section>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import CallTimer from '@/components/counselor/CallTimer.vue'
import CustomerInfoPanel from '@/components/counselor/CustomerInfoPanel.vue'
import STTChatPanel from '@/components/counselor/STTChatPanel.vue'
import CounselorCallControls from '@/components/counselor/CounselorCallControls.vue'
import AutoTerminationModal from '@/components/call/AutoTerminationModal.vue'
import { mockCustomerInfo, mockSttMessages } from '@/mocks/counselor'
import { fetchCustomerData } from '@/services/customerService'
import { useNotificationStore } from '@/stores/notification'
import { useCallStore } from '@/stores/call'

const router = useRouter()

// 스토어
const notificationStore = useNotificationStore()
const callStore = useCallStore()

// 통화 상태
const isCallActive = ref(true)
const isMuted = ref(false)
const isPaused = ref(false)

// 자동 종료 모달
const showAutoTerminationModal = ref(false)

// 자동 종료 감지
watch(() => callStore.autoTerminationTriggered, (triggered) => {
  if (triggered) {
    showAutoTerminationModal.value = true
  }
})

// 고객 정보
const customerInfo = ref(mockCustomerInfo)
const isLoadingCustomerInfo = ref(false)
const customerInfoError = ref(null)

// STT 메시지 (더미 데이터)
const sttMessages = ref(mockSttMessages)

// AI 가이드
const searchQuery = ref('')
const memo = ref('')

// 고객 정보 로드
const loadCustomerData = async () => {
  try {
    isLoadingCustomerInfo.value = true
    customerInfoError.value = null

    // TODO: 실제 registrationId는 라우트 파라미터나 통화 세션에서 가져오기
    const registrationId = 1001

    const data = await fetchCustomerData(registrationId)
    customerInfo.value = data
  } catch (error) {
    console.error('고객 정보 로드 실패:', error)
    customerInfoError.value = '고객 정보를 불러오는데 실패했습니다.'
    // 에러 발생 시 mock 데이터 사용
    customerInfo.value = mockCustomerInfo
    // TODO: 에러 토스트 표시
  } finally {
    isLoadingCustomerInfo.value = false
  }
}

// 통화 컨트롤 핸들러
const handleMuteChanged = (muted) => {
  isMuted.value = muted
  // TODO: LiveKit 음소거 처리
}

const handlePauseChanged = (paused) => {
  isPaused.value = paused
  // TODO: 일시정지 처리
}

const handleEndCall = async () => {
  try {
    isCallActive.value = false
    // TODO: await livekitService.disconnect()
    // TODO: router.push('/counselor/call-summary')
    console.log('통화 종료')
  } catch (error) {
    console.error('통화 종료 실패:', error)
    // TODO: 에러 토스트 표시
  }
}

// 자동 종료 모달 확인 핸들러
const handleAutoTerminationConfirm = async () => {
  showAutoTerminationModal.value = false

  try {
    // 통화 종료 처리
    const callData = callStore.endCall()

    // TODO: API 호출 - 블랙리스트 등록
    // await addToBlacklist(callData.customerId, callStore.currentCall.agentId)

    // TODO: 통화 기록 저장
    // await saveCallRecord(callData)

    // LiveKit 연결 종료
    // TODO: await livekitService.disconnect()

    // 상태 초기화
    callStore.resetCall()

    // 대시보드로 이동
    router.push({ name: 'counselor-dashboard' })

    notificationStore.notifyInfo('고객이 블랙리스트에 등록되었습니다')
  } catch (error) {
    console.error('자동 종료 처리 실패:', error)
    notificationStore.notifyError('통화 종료 처리에 실패했습니다')
  }
}

// 욕설 표시/숨기기 토글
const handleToggleProfanity = (index) => {
  sttMessages.value[index].showOriginal = !sttMessages.value[index].showOriginal
}

/**
 * STT 메시지 추가 (실제 STT/WebSocket 연동 시 이 함수 호출)
 * @param {Object} message - STT 메시지 객체
 * @param {string} message.speaker - 화자 ('agent' | 'customer')
 * @param {string} message.text - 원본 텍스트
 * @param {string} message.maskedText - 마스킹된 텍스트
 * @param {boolean} message.hasProfanity - 폭언 포함 여부
 * @param {number} message.confidence - 신뢰도
 */
const addSttMessage = (message) => {
  const timestamp = new Date().toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit'
  })

  sttMessages.value.push({
    ...message,
    timestamp,
    showOriginal: false
  })

  // 마스킹(폭언) 감지 시 알림 표시 및 카운트 증가
  if (message.hasProfanity) {
    // callStore에서 폭언 카운트 증가 (3회 도달 시 자동 종료 트리거)
    const newCount = callStore.incrementProfanityCount()

    // 알림 표시
    notificationStore.notifyProfanity(newCount)

    console.log(`[CounselorCall] 폭언 감지 (${newCount}/3회)`)
  }
}

// 외부에서 사용할 수 있도록 expose (선택적)
defineExpose({ addSttMessage })

// 컴포넌트 마운트 시 고객 정보 로드
onMounted(() => {
  loadCustomerData()
})
</script>

<style scoped>
/* CounselorCallView 전용 스타일 */
</style>
