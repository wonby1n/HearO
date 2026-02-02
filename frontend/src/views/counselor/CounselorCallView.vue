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
          <div class="flex items-center gap-3">
            <h1 class="text-2xl font-bold text-primary-600">HearO</h1>
          </div>

          <CallTimer :isActive="isCallActive" />

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

    <!-- 메인 컨텐츠 -->
    <main class="max-w-[1920px] mx-auto p-6">
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 h-[calc(100vh-140px)]">
        <!-- 좌측: 고객 정보 패널 (데이터가 있을 때만 렌더링하거나 안전한 기본값 전달) -->
        <div class="lg:col-span-3">
          <CustomerInfoPanel
            :customerInfo="customerInfo || initialPlaceholder"
            :isLoading="isLoadingCustomerInfo"
            :error="customerInfoError"
          />
        </div>

        <!-- 중앙: STT 자막 영역 -->
        <div class="lg:col-span-6">
          <STTChatPanel
            :messages="sttMessages"
            @toggle-profanity="handleToggleProfanity"
          />
        </div>

        <!-- 우측: AI 가이드 -->
        <div class="lg:col-span-3">
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 h-full p-6">
            <div class="flex items-center gap-2 mb-4">
              <svg class="w-5 h-5 text-primary-600" fill="currentColor" viewBox="0 0 20 20">
                <path d="M11 3a1 1 0 10-2 0v1a1 1 0 102 0V3zM15.657 5.757a1 1 0 00-1.414-1.414l-.707.707a1 1 0 001.414 1.414l.707-.707zM18 10a1 1 0 01-1 1h-1a1 1 0 110-2h1a1 1 0 011 1zM5.05 6.464A1 1 0 106.464 5.05l-.707-.707a1 1 0 00-1.414 1.414l.707.707zM5 10a1 1 0 01-1 1H3a1 1 0 110-2h1a1 1 0 011 1zM8 16v-1h4v1a2 2 0 11-4 0zM12 14c.015-.34.208-.646.477-.859a4 4 0 10-4.954 0c.27.213.462.519.476.859h4.002z"/>
              </svg>
              <h3 class="text-lg font-semibold text-gray-900">AI 가이드</h3>
            </div>

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
              </div>
            </section>

            <CallMemoPanel v-model="memo" :saved-label="memoSaveLabel" />
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, watch, computed, onBeforeUnmount } from 'vue'
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
import { useNotificationStore } from '@/stores/notification'
import { useCallStore } from '@/stores/call'
import { useLiveKit } from '@/composables/useLiveKit'

const router = useRouter()

// 스토어
const notificationStore = useNotificationStore()
const callStore = useCallStore()

// LiveKit
const { setMuted: livekitSetMuted } = useLiveKit()

// 통화 및 모달 상태
const isCallActive = ref(true)
const isMuted = ref(false)
const isPaused = ref(false)
const showAutoTerminationModal = ref(false)
const showManualEndModal = ref(false)

// [수정] 크래시 방지를 위한 초기 플레이스홀더 설정
const initialPlaceholder = {
  customerName: '로딩 중...',
  phone: '-',
  productName: '-',
  warrantyStatus: { status: '-', isExpired: false, endDate: '-' },
  symptoms: []
}

const customerInfo = ref(null)
const isLoadingCustomerInfo = ref(false)
const customerInfoError = ref(null)
const sttMessages = ref(mockSttMessages)

// AI 가이드 검색어 변수 추가 (누락되었던 부분)
const searchQuery = ref('')

// 날짜 포맷팅 및 보증 상태 헬퍼
const formatDate = (dateString) => {
  if (!dateString) return '-';
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '-';
    return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
  } catch { return '-'; }
};

const getWarrantyStatus = (warrantyEndsAt) => {
  if (!warrantyEndsAt) return { status: '정보 없음', isExpired: null, endDate: null };
  const endDate = new Date(warrantyEndsAt);
  const today = new Date();
  const isExpired = endDate <= today;
  return {
    status: isExpired ? '만료' : '이내',
    isExpired: isExpired,
    endDate: formatDate(warrantyEndsAt)
  };
};

// 고객 데이터 로드 함수
const loadCustomerData = async (regId) => {
  if (!regId || regId === 'undefined') return;

  try {
    isLoadingCustomerInfo.value = true;
    customerInfoError.value = null;
    console.log('🔍 [CounselorCallView] Loading registrationId:', regId);

    const response = await fetchCustomerData(regId);
    
    // API 응답 데이터 안전하게 매핑
    customerInfo.value = {
      id: response.id,
      customerId: response.customerId,
      customerName: response.customerName || '고객 정보 없음',
      phone: response.customerPhone || response.phone || '정보 없음',
      productName: response.productName || '정보 없음',
      productCategory: response.productCategory || '정보 없음',
      modelCode: response.modelCode || '정보 없음',
      modelNumber: response.modelNumber || response.modelCode || '정보 없음',
      purchaseDate: formatDate(response.manufacturedAt || response.purchaseDate),
      warrantyStatus: getWarrantyStatus(response.warrantyEndsAt),
      productImage: response.productImageUrl || response.productImage || '/images/default-product.png',
      errorCode: response.errorCode || '정보 없음',
      symptoms: response.symptom ? [response.symptom] : (response.symptoms || ['정보 없음']),
      consultationHistory: response.consultationHistory || []
    };
    
    console.log('✅ [CounselorCallView] Load Success:', customerInfo.value);
  } catch (error) {
    console.error('❌ [CounselorCallView] Load Failed:', error);
    customerInfoError.value = '고객 정보를 불러오는 중 오류가 발생했습니다.';
    customerInfo.value = mockCustomerInfo; 
  } finally {
    isLoadingCustomerInfo.value = false;
  }
};

// [중요] ID 감시 및 데이터 로드 실행
watch(
  () => callStore.currentCall?.registrationId || router.currentRoute.value.params.registrationId,
  (newId) => {
    // 문자열 'undefined' 체크 추가
    if (newId && String(newId) !== 'undefined') {
      loadCustomerData(newId);
    }
  },
  { immediate: true }
);

// 메모 및 AI 관련 로직
const memo = computed({
  get: () => callStore.callMemo,
  set: (val) => callStore.updateMemo(val)
})
const aiSummary = ref('')
const memoLastSavedAt = ref(null)
const memoDraftKey = computed(() => `callMemoDraft:${callStore.currentCall?.id || 'temp'}`)
let memoSaveTimeout = null
let skipDraftSaveOnUnmount = false

const memoSaveLabel = computed(() => {
  if (!memoLastSavedAt.value) return memo.value?.trim().length ? '임시 저장 전' : '';
  return `임시 저장됨 · ${new Date(memoLastSavedAt.value).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}`;
})

// 통화 종료 핸들러
const handleEndCall = async () => {
  try {
    isCallActive.value = false
    const consultationId = callStore.currentCall.consultationId ?? callStore.currentCall.id;
    if (consultationId && memo.value?.trim()) {
      await saveConsultationMemo(consultationId, memo.value.trim());
    }
    callStore.endCall()
    skipDraftSaveOnUnmount = true
    router.push({ name: 'dashboard' })
  } catch (error) {
    console.error('종료 처리 실패:', error)
  }
}

// 라이프사이클 및 기타 핸들러
onBeforeUnmount(() => {
  if (memoSaveTimeout) clearTimeout(memoSaveTimeout);
})

const handleMuteChanged = async (muted) => {
  isMuted.value = muted
  try { await livekitSetMuted(muted); } catch { isMuted.value = !muted; }
}

const handlePauseChanged = (paused) => { isPaused.value = paused }
const handleManualEndRequest = () => { showManualEndModal.value = true }
const handleManualEndConfirm = async () => { showManualEndModal.value = false; await handleEndCall(); }
const handleAutoTerminationConfirm = async () => { 
  showAutoTerminationModal.value = false; 
  await handleEndCall(); 
  notificationStore.notifyInfo('비정상 통화로 종료되었습니다.');
}
const handleToggleProfanity = (idx) => { sttMessages.value[idx].showOriginal = !sttMessages.value[idx].showOriginal }

const addSttMessage = (msg) => {
  const timestamp = new Date().toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
  sttMessages.value.push({ ...msg, timestamp, showOriginal: false });
  if (msg.hasProfanity) {
    const count = callStore.incrementProfanityCount();
    notificationStore.notifyProfanity(count);
  }
}

defineExpose({ addSttMessage })
</script>