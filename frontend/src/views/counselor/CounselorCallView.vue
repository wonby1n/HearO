<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 자동 종료 모달 -->
    <AutoTerminationModal
      :show="showAutoTerminationModal"
      :ai-summary="aiSummary"
      v-model:memo="memo"
      @confirm="handleAutoTerminationConfirm"
    />

    <ManualEndCallModal
      :show="showManualEndModal"
      :ai-summary="aiSummary"
      v-model:memo="memo"
      @confirm="handleManualEndConfirm"
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
            @call-end-requested="handleManualEndRequest"
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
        <div class="lg:col-span-6 h-full">
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
            <CallMemoPanel v-model="memo" :saved-label="memoSaveLabel" />
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import CallTimer from '@/components/counselor/CallTimer.vue'
import CustomerInfoPanel from '@/components/counselor/CustomerInfoPanel.vue'
import STTChatPanel from '@/components/counselor/STTChatPanel.vue'
import CounselorCallControls from '@/components/counselor/CounselorCallControls.vue'
import CallMemoPanel from '@/components/counselor/CallMemoPanel.vue'
import AutoTerminationModal from '@/components/call/AutoTerminationModal.vue'
import ManualEndCallModal from '@/components/call/ManualEndCallModal.vue'
import { mockCustomerInfo, mockSttMessages } from '@/mocks/counselor'
import { fetchCustomerData } from '@/services/customerService'
import { saveConsultationMemo } from '@/services/consultationService'
import { RoomEvent } from 'livekit-client'
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
const showManualEndModal = ref(false)

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
const memo = computed({
  get: () => callStore.callMemo,
  set: (value) => {
    callStore.updateMemo(value)
  }
})
const aiSummary = ref('')

// 메모 임시 저장
const memoDraftKey = ref('')
const memoLastSavedAt = ref(null)
let memoSaveTimeout = null
let skipDraftSaveOnUnmount = false

const memoSaveLabel = computed(() => {
  if (!memoLastSavedAt.value) {
    return memo.value?.trim().length ? '임시 저장 전' : ''
  }
  const timeLabel = new Date(memoLastSavedAt.value).toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit'
  })
  return `임시 저장됨 · ${timeLabel}`
})

const getSessionDraftKey = () => {
  if (typeof window === 'undefined') {
    return ''
  }
  let storedKey = sessionStorage.getItem('callMemoDraftKey')
  if (!storedKey) {
    storedKey = `callMemoDraft:${Date.now()}`
    sessionStorage.setItem('callMemoDraftKey', storedKey)
  }
  return storedKey
}

const resolveMemoDraftKey = (callId) => {
  if (callId) {
    return `callMemoDraft:${callId}`
  }
  return getSessionDraftKey()
}

const loadMemoDraft = () => {
  if (!memoDraftKey.value) {
    return
  }
  try {
    const raw = localStorage.getItem(memoDraftKey.value)
    if (!raw) {
      return
    }
    const parsed = JSON.parse(raw)
    const draftText = typeof parsed === 'string' ? parsed : parsed?.memo
    if (draftText && memo.value.trim().length === 0) {
      callStore.updateMemo(draftText)
    }
    if (parsed?.savedAt) {
      memoLastSavedAt.value = parsed.savedAt
    }
  } catch (error) {
    console.warn('메모 임시 저장 로드 실패:', error)
  }
}

const saveMemoDraft = (value) => {
  if (!memoDraftKey.value) {
    return
  }
  if (!value || value.trim().length === 0) {
    localStorage.removeItem(memoDraftKey.value)
    memoLastSavedAt.value = null
    return
  }
  const payload = {
    memo: value,
    savedAt: Date.now()
  }
  localStorage.setItem(memoDraftKey.value, JSON.stringify(payload))
  memoLastSavedAt.value = payload.savedAt
}

const clearMemoDraft = (key = memoDraftKey.value) => {
  if (typeof window === 'undefined') {
    return
  }
  if (memoSaveTimeout) {
    clearTimeout(memoSaveTimeout)
    memoSaveTimeout = null
  }
  if (key) {
    localStorage.removeItem(key)
  }
  memoLastSavedAt.value = null
}

watch(memo, (newValue) => {
  if (!memoDraftKey.value) {
    return
  }
  if (memoSaveTimeout) {
    clearTimeout(memoSaveTimeout)
  }
  memoSaveTimeout = setTimeout(() => {
    saveMemoDraft(newValue)
  }, 500)
})

watch(
  () => callStore.currentCall.id,
  (newId, oldId) => {
    const nextKey = resolveMemoDraftKey(newId)
    const previousKey = memoDraftKey.value
    if (previousKey && previousKey !== nextKey) {
      clearMemoDraft(previousKey)
    }
    memoDraftKey.value = nextKey
    skipDraftSaveOnUnmount = false
    loadMemoDraft()
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  if (memoSaveTimeout) {
    clearTimeout(memoSaveTimeout)
    memoSaveTimeout = null
  }
  if (!skipDraftSaveOnUnmount && memo.value?.trim().length) {
    saveMemoDraft(memo.value)
  }
})

const getConsultationId = () => {
  return callStore.currentCall.consultationId ?? callStore.currentCall.id
}

const saveMemoToServer = async () => {
  const memoValue = memo.value?.trim()
  if (!memoValue) {
    return true
  }
  const consultationId = getConsultationId()
  if (!consultationId) {
    notificationStore.notifyWarning('상담 ID를 찾을 수 없어 메모를 저장하지 못했습니다')
    return false
  }

  try {
    await saveConsultationMemo(consultationId, memoValue)
    notificationStore.notifySuccess('메모가 저장되었습니다')
    return true
  } catch (error) {
    console.error('메모 저장 실패:', error)
    notificationStore.notifyError('메모 저장에 실패했습니다')
    return false
  }
}

// 고객 정보 로드
const loadCustomerData = async () => {
  try {
    isLoadingCustomerInfo.value = true;
    customerInfoError.value = null;

    // 1. 등록 ID 가져오기 (예시: 1)
    const registrationId = 1; 

    // 2. API 호출
    const response = await fetchCustomerData(registrationId);

    if (response && response.isSuccess) {
      const apiData = response.data; // 백엔드에서 온 데이터

      // 3. 스토어 및 로컬 상태 구조에 맞게 매핑
      const mappedData = {
        id: apiData.id,
        // 상담원 화면이 기대하는 평탄한 구조로 일단 customerInfo에 저장
        symptom: apiData.symptom,
        errorCode: apiData.errorCode,
        productName: apiData.productName,
        modelCode: apiData.modelCode,
        productImageUrl: apiData.productImageUrl,
        manufacturedAt: apiData.manufacturedAt,
        warrantyEndsAt: apiData.warrantyEndsAt
      };

      // 화면에 표시될 ref 객체에 할당
      customerInfo.value = mappedData;
      
      // (선택사항) 만약 공통 스토어에도 저장해야 한다면:
      // customerStore.setCustomerInfo(mappedData); 

      console.log('✅ 데이터 매핑 성공:', customerInfo.value);
    } else {
      throw new Error(response.message || '데이터 구조 이상');
    }
  } catch (error) {
    console.error('❌ 고객 정보 로드 실패:', error);
    customerInfoError.value = '고객 정보를 불러오는데 실패했습니다.';
    customerInfo.value = mockCustomerInfo; // 실패 시 폴백
  } finally {
    isLoadingCustomerInfo.value = false;
  }
};

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
    callStore.endCall()
    const saved = await saveMemoToServer()
    if (saved) {
      clearMemoDraft()
      skipDraftSaveOnUnmount = true
    }

    // LiveKit 연결 종료
    if (callStore.livekitRoom) {
      console.log('[CounselorCallView] LiveKit 연결 종료')
      await callStore.livekitRoom.disconnect()
      callStore.setLivekitRoom(null)
    }

    // call store 완전히 리셋
    callStore.resetCall()

    router.push({ name: 'dashboard' })
    console.log('통화 종료')
  } catch (error) {
    console.error('통화 종료 실패:', error)
    // TODO: 에러 토스트 표시
  }

}

// Manual end modal request
const handleManualEndRequest = () => {
  showManualEndModal.value = true
}

// Manual end modal confirm
const handleManualEndConfirm = async () => {
  showManualEndModal.value = false
  await handleEndCall()
}


// 자동 종료 모달 확인 핸들러
const handleAutoTerminationConfirm = async () => {
  showAutoTerminationModal.value = false

  try {
    // 통화 종료 처리
    const callData = callStore.endCall()

    // TODO: API 호출 - 블랙리스트 등록
    // await addToBlacklist(callData.customerId, callStore.currentCall.agentId)

    const saved = await saveMemoToServer()
    if (saved) {
      clearMemoDraft()
      skipDraftSaveOnUnmount = true
    }

    // TODO: 통화 기록 저장
    // await saveCallRecord(callData)

    // LiveKit 연결 종료
    if (callStore.livekitRoom) {
      console.log('[CounselorCallView] LiveKit 연결 종료 (자동 종료)')
      await callStore.livekitRoom.disconnect()
      callStore.setLivekitRoom(null)
    }

    // 상태 초기화
    callStore.resetCall()

    // 대시보드로 이동
    router.push({ name: 'dashboard' })

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

// 컴포넌트 마운트 시 고객 정보 로드 및 LiveKit 연결 확인
onMounted(() => {
  loadCustomerData()

  // call store에 저장된 LiveKit room 확인
  if (callStore.livekitRoom) {
    console.log('[CounselorCallView] 기존 LiveKit 연결 사용:', callStore.livekitRoom.name)

    // 고객이 통화를 종료했을 때 이벤트 리스너 추가
    callStore.livekitRoom.on(RoomEvent.ParticipantDisconnected, (participant) => {
      console.log('[CounselorCallView] 고객이 통화를 종료했습니다:', participant.identity)

      // 통화 종료 모달 표시 (통화 요약 및 메모 저장)
      isCallActive.value = false
      showManualEndModal.value = true

      notificationStore.notifyInfo('고객이 통화를 종료했습니다')
    })
  } else {
    console.warn('[CounselorCallView] LiveKit 연결이 없습니다. 대시보드로 돌아가세요.')
    // 선택적: 연결이 없으면 대시보드로 리다이렉트
    // router.push('/dashboard')
  }
})
</script>

<style scoped>
/* CounselorCallView 전용 스타일 */
</style>
