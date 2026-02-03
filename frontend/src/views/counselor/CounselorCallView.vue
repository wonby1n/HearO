<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 자동 종료 모달 (시스템 트리거) -->
    <AutoTerminationModal
      :show="showAutoTerminationModal"
      :ai-summary="aiSummary"
      v-model:memo="memo"
      @confirm="handleAutoTerminationConfirm"
    />

    <!-- 수동 종료 확인 모달 -->
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
            @mute-changed="handleMuteChanged"
            @call-end-requested="handleManualEndRequest"
          />
        </div>
      </div>
    </header>

    <!-- 메인 컨텐츠 -->
    <main class="max-w-[1920px] mx-auto p-6">
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 h-[calc(100vh-140px)]">
        <!-- 좌측: 고객 정보 패널 -->
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

        <!-- 우측: AI 가이드 및 메모 -->
        <div class="lg:col-span-3">
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 h-full p-6 flex flex-col">
            <div class="flex items-center gap-2 mb-4">
              <svg class="w-5 h-5 text-primary-600" fill="currentColor" viewBox="0 0 20 20">
                <path d="M11 3a1 1 0 10-2 0v1a1 1 0 102 0V3zM15.657 5.757a1 1 0 00-1.414-1.414l-.707.707a1 1 0 001.414 1.414l.707-.707zM18 10a1 1 0 01-1 1h-1a1 1 0 110-2h1a1 1 0 011 1zM5.05 6.464A1 1 0 106.464 5.05l-.707-.707a1 1 0 00-1.414 1.414l.707.707zM5 10a1 1 0 01-1 1H3a1 1 0 110-2h1a1 1 0 011 1zM8 16v-1h4v1a2 2 0 11-4 0zM12 14c.015-.34.208-.646.477-.859a4 4 0 10-4.954 0c.27.213.462.519.476.859h4.002z"/>
              </svg>
              <h3 class="text-lg font-semibold text-gray-900">AI 가이드</h3>
            </div>

            <!-- 검색 영역 -->
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

            <!-- 추천 영역 (스크롤 가능하게 처리 권장) -->
            <div class="flex-1 overflow-y-auto space-y-6 pr-1">
              <section>
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

              <section>
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

              <CallMemoPanel v-model="memo" :saved-label="memoSaveLabel" />
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, watch, computed, onBeforeUnmount, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { RoomEvent, Track } from 'livekit-client'
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
import { useDashboardStore } from '@/stores/dashboard'
import { useAudioStream } from '@/composables/useAudioStream'
import axios from 'axios'

// 로컬 AI 서버 엔드포인트 (Vite env로 덮어쓸 수 있음) 
const TOXIC_API_URL = import.meta.env.VITE_TOXIC_API_URL || 'http://127.0.0.1:8000/unsmile'
const WHISPER_API_URL = import.meta.env.VITE_WHISPER_API_URL || 'http://127.0.0.1:8000/stt'

// 상담원: 고객 오디오 딜레이(기본 3초) 
const CUSTOMER_AUDIO_DELAY_SEC = 3
const MUTE_POSTPAD_MS = 600

// 상담원 Whisper STT: 무음 감지(보수적으로 짧게) 
const VAD_SILENCE_MS = Number(import.meta.env.VITE_COUNSELOR_VAD_SILENCE_MS || 650)
const VAD_MIN_UTTER_MS = Number(import.meta.env.VITE_COUNSELOR_VAD_MIN_UTTER_MS || 800)

const router = useRouter()
const notificationStore = useNotificationStore()
const callStore = useCallStore()
const dashboardStore = useDashboardStore()
const { initAudioContext } = useAudioStream()

// --- 상태 정의 ---
const isCallActive = ref(true)
const isMuted = ref(false)

// 자동 종료 모달
const showAutoTerminationModal = ref(false)
const showManualEndModal = ref(false)

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
const searchQuery = ref('')
const aiSummary = ref('')

// --- 메모 드래프트 관리 (복구된 핵심 로직) ---
const memo = computed({
  get: () => callStore.callMemo,
  set: (val) => callStore.updateMemo(val)
})

const memoLastSavedAt = ref(null)
const memoDraftKey = ref('')
let memoSaveTimeout = null
let skipDraftSaveOnUnmount = false

const memoSaveLabel = computed(() => {
  if (!memoLastSavedAt.value) return memo.value?.trim().length ? '임시 저장 전' : '';
  const timeLabel = new Date(memoLastSavedAt.value).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
  return `임시 저장됨 · ${timeLabel}`;
})

const getSessionDraftKey = () => {
  if (typeof window === 'undefined') return '';
  let storedKey = sessionStorage.getItem('callMemoDraftKey');
  if (!storedKey) {
    storedKey = `callMemoDraft:${Date.now()}`;
    sessionStorage.setItem('callMemoDraftKey', storedKey);
  }
  return storedKey;
}

const resolveMemoDraftKey = (callId) => {
  return callId ? `callMemoDraft:${callId}` : getSessionDraftKey();
}

const loadMemoDraft = () => {
  if (!memoDraftKey.value) return;
  try {
    const raw = localStorage.getItem(memoDraftKey.value);
    if (!raw) return;
    const parsed = JSON.parse(raw);
    const draftText = typeof parsed === 'string' ? parsed : parsed?.memo;
    if (draftText && memo.value.trim().length === 0) {
      callStore.updateMemo(draftText);
    }
    if (parsed?.savedAt) memoLastSavedAt.value = parsed.savedAt;
  } catch (error) {
    console.warn('메모 드래프트 로드 실패:', error);
  }
}

const saveMemoDraft = (value) => {
  if (!memoDraftKey.value) return;
  if (!value || value.trim().length === 0) {
    localStorage.removeItem(memoDraftKey.value);
    memoLastSavedAt.value = null;
    return;
  }
  const payload = { memo: value, savedAt: Date.now() };
  localStorage.setItem(memoDraftKey.value, JSON.stringify(payload));
  memoLastSavedAt.value = payload.savedAt;
}

const clearMemoDraft = (key = memoDraftKey.value) => {
  if (memoSaveTimeout) { clearTimeout(memoSaveTimeout); memoSaveTimeout = null; }
  if (key) localStorage.removeItem(key);
  memoLastSavedAt.value = null;
}

// 메모 변경 감시 (자동 저장)
watch(memo, (newValue) => {
  if (memoSaveTimeout) clearTimeout(memoSaveTimeout);
  memoSaveTimeout = setTimeout(() => saveMemoDraft(newValue), 500);
})

// 콜 변경 시 드래프트 키 갱신
watch(() => callStore.currentCall?.id, (newId) => {
  const nextKey = resolveMemoDraftKey(newId);
  const previousKey = memoDraftKey.value;
  if (previousKey && previousKey !== nextKey) clearMemoDraft(previousKey);
  memoDraftKey.value = nextKey;
  skipDraftSaveOnUnmount = false;
  loadMemoDraft();
}, { immediate: true });

// 메모 서버 저장
const saveMemoToServer = async () => {
  const memoValue = memo.value?.trim()
  if (!memoValue) {
    return true
  }

  const consultationId = callStore.currentCall?.consultationId ?? callStore.currentCall?.id
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

// --- 데이터 로딩 로직 ---
const formatDate = (dateString) => {
  if (!dateString) return '-';
  try {
    const date = new Date(dateString);
    return isNaN(date) ? '-' : `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
  } catch { return '-'; }
}

const getWarrantyStatus = (warrantyEndsAt) => {
  if (!warrantyEndsAt) return { status: '정보 없음', isExpired: null, endDate: null };
  const endDate = new Date(warrantyEndsAt);
  const today = new Date();
  const isExpired = endDate <= today;
  return { status: isExpired ? '만료' : '이내', isExpired, endDate: formatDate(warrantyEndsAt) };
}

const loadCustomerData = async (regId) => {
  if (!regId || String(regId) === 'undefined') return;
  try {
    isLoadingCustomerInfo.value = true;
    customerInfoError.value = null;

    // 1. 매칭 데이터에서 registrationId 가져오기
    const registrationId = dashboardStore.matchedData?.registrationId;

    if (!registrationId) {
      console.warn('[CounselorCallView] registrationId를 찾을 수 없습니다. 목 데이터 사용');
      customerInfo.value = mockCustomerInfo;
      return;
    }

    console.log('[CounselorCallView] Registration ID:', registrationId);

    const response = await fetchCustomerData(regId);
    customerInfo.value = {
      id: response.id,
      customerId: response.customerId,
      customerName: response.customerName || '고객 정보 없음',
      phone: response.customerPhone || response.phone || '정보 없음',
      productName: response.productName || '정보 없음',
      productCategory: response.productCategory || '정보 없음',
      modelCode: response.modelCode || '정보 없음',
      purchaseDate: formatDate(response.manufacturedAt || response.purchaseDate),
      warrantyStatus: getWarrantyStatus(response.warrantyEndsAt),
      productImage: response.productImageUrl || '/images/default-product.png',
      errorCode: response.errorCode || '정보 없음',
      symptoms: response.symptom ? [response.symptom] : (response.symptoms || ['정보 없음']),
      consultationHistory: response.consultationHistory || []
    };
  } catch (error) {
    console.error('고객 로드 실패:', error);
    customerInfoError.value = '데이터를 불러올 수 없습니다.';
    customerInfo.value = mockCustomerInfo;
  } finally {
    isLoadingCustomerInfo.value = false;
  }
};

// 통화 컨트롤 핸들러
// 음소거 토글 핸들러
const handleMuteChanged = async (muted) => {
  console.log(`[CounselorCallView] handleMuteChanged 호출: muted=${muted}`)

  if (!callStore.livekitRoom) {
    console.warn('[CounselorCallView] LiveKit room이 없습니다')
    return
  }

  try {
    const localParticipant = callStore.livekitRoom.localParticipant

    // 로컬 참가자의 오디오 트랙 찾기
    const audioPublication = localParticipant.getTrackPublication(Track.Source.Microphone)
    console.log(`[CounselorCallView] 오디오 트랙 조회 결과: ${audioPublication ? '있음' : '없음'}`)

    if (audioPublication && audioPublication.track) {
      // 기존 트랙이 있으면 mute/unmute
      if (muted) {
        await audioPublication.mute()
      } else {
        await audioPublication.unmute()
      }

      isMuted.value = muted
      console.log(`[CounselorCallView] 마이크 ${muted ? '음소거' : '음소거 해제'}`)
    } else {
      // 트랙이 없으면 마이크 활성화 먼저 시도
      console.log('[CounselorCallView] 오디오 트랙이 없어서 마이크 활성화 시도')

      try {
        // 마이크 권한 요청 및 활성화
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
        const audioTracks = stream.getAudioTracks()

        if (audioTracks.length > 0) {
          // publishTrack의 반환값(LocalTrackPublication)을 직접 사용
          const publication = await localParticipant.publishTrack(audioTracks[0])
          console.log('[CounselorCallView] 마이크 활성화 성공')

          // 활성화 후 즉시 mute 처리 (음소거 버튼을 눌렀으므로)
          if (muted) {
            await publication.mute()
            console.log('[CounselorCallView] 마이크 활성화 후 즉시 음소거 처리 완료')
          } else {
            console.log('[CounselorCallView] 마이크 활성화 완료 (음소거 해제 상태)')
          }

          isMuted.value = muted
        }
      } catch (micError) {
        console.error('[CounselorCallView] 마이크 활성화 실패:', micError)
        notificationStore.notifyError('마이크 권한을 허용해주세요')
        // 상태 복원하지 않음 (마이크가 없는 상태 유지)
      }
    }
  } catch (error) {
    console.error('[CounselorCallView] 마이크 제어 실패:', error)
    // 에러 발생 시 상태 복원
    isMuted.value = !muted
  }
}

const handleEndCall = async () => {
  try {
    isCallActive.value = false

    // 메모 저장
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

    // 상담사 상태를 AVAILABLE로 복구 (새 매칭 가능하도록)
    try {
      await axios.patch('/api/v1/users/me/status', { status: 'AVAILABLE' })
      console.log('[CounselorCallView] 상담사 상태 AVAILABLE로 복구')
    } catch (statusError) {
      console.error('[CounselorCallView] 상태 복구 실패:', statusError)
      // 상태 복구 실패해도 통화 종료는 계속 진행
    }

    // call store 완전히 리셋
    callStore.resetCall()

    router.push({ name: 'dashboard' })
  } catch (error) {
    console.error('통화 종료 실패:', error)
    // TODO: 에러 토스트 표시
  }

}

// Manual end modal request
const handleManualEndRequest = async () => {
  console.log('[CounselorCallView] 통화 종료 버튼 클릭')

  try {
    // 통화 종료 버튼을 누르는 즉시 LiveKit 연결 종료 (고객에게 즉시 알림)
    isCallActive.value = false
    callStore.endCall()

    if (callStore.livekitRoom) {
      console.log('[CounselorCallView] LiveKit 연결 즉시 종료 (통화 종료 버튼)')

      try {
        // 1. 마이크 트랙 즉시 정리 (다음 상담 시 연결 실패 방지)
        const localParticipant = callStore.livekitRoom.localParticipant
        const audioPublication = localParticipant.getTrackPublication(Track.Source.Microphone)

        if (audioPublication) {
          console.log('[CounselorCallView] 마이크 트랙 unpublish 시작')

          // 트랙 unpublish
          await localParticipant.unpublishTrack(audioPublication.track)

          // MediaStreamTrack 완전히 중지
          if (audioPublication.track?.mediaStreamTrack) {
            audioPublication.track.mediaStreamTrack.stop()
            console.log('[CounselorCallView] 마이크 MediaStreamTrack 중지 완료')
          }
        }

        // 2. LiveKit 룸 연결 종료
        await callStore.livekitRoom.disconnect()
        console.log('[CounselorCallView] LiveKit 연결 종료 완료')
      } catch (disconnectError) {
        console.error('[CounselorCallView] LiveKit 연결 종료 실패:', disconnectError)
      }

      callStore.setLivekitRoom(null)
    }

    // 마이크 상태를 음소거로 설정 (UI 동기화)
    isMuted.value = true

    // 모달 표시 (메모 작성 및 요약 확인용)
    showManualEndModal.value = true
    console.log('[CounselorCallView] 수동 종료 모달 표시')
  } catch (error) {
    console.error('[CounselorCallView] 통화 종료 버튼 처리 실패:', error)
    notificationStore.notifyError('통화 종료 중 오류가 발생했습니다')
  }
}

// Manual end modal confirm
const handleManualEndConfirm = async () => {
  console.log('[CounselorCallView] 수동 종료 모달 확인 버튼 클릭')

  try {
    showManualEndModal.value = false

    // 메모 저장
    const saved = await saveMemoToServer()
    if (saved) {
      clearMemoDraft()
      skipDraftSaveOnUnmount = true
    }

    // 상담사 상태를 AVAILABLE로 복구
    try {
      await axios.patch('/api/v1/users/me/status', { status: 'AVAILABLE' })
      console.log('[CounselorCallView] 상담사 상태 AVAILABLE로 복구')
    } catch (statusError) {
      console.error('[CounselorCallView] 상태 복구 실패:', statusError)
    }

    // call store 리셋 및 대시보드 이동
    callStore.resetCall()
    console.log('[CounselorCallView] 통화 종료 완료, 대시보드로 이동')
    router.push({ name: 'dashboard' })
  } catch (error) {
    console.error('[CounselorCallView] 수동 종료 확인 처리 실패:', error)
    notificationStore.notifyError('통화 종료 처리 중 오류가 발생했습니다')
    // 에러가 발생해도 대시보드로 이동
    router.push({ name: 'dashboard' })
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

    // 상담사 상태를 AVAILABLE로 복구 (새 매칭 가능하도록)
    try {
      await axios.patch('/api/v1/users/me/status', { status: 'AVAILABLE' })
      console.log('[CounselorCallView] 상담사 상태 AVAILABLE로 복구 (자동 종료)')
    } catch (statusError) {
      console.error('[CounselorCallView] 상태 복구 실패:', statusError)
      // 상태 복구 실패해도 통화 종료는 계속 진행
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

onBeforeUnmount(() => {
  if (memoSaveTimeout) clearTimeout(memoSaveTimeout);
  if (!skipDraftSaveOnUnmount && memo.value?.trim().length) saveMemoDraft(memo.value);
})

defineExpose({ addSttMessage })

// 컴포넌트 마운트 시 고객 정보 로드
onMounted(() => {
  loadCustomerData()

  // call store에 저장된 LiveKit room 확인
  if (callStore.livekitRoom) {
    console.log('[CounselorCallView] 기존 LiveKit 연결 사용:', callStore.livekitRoom.name)

    const room = callStore.livekitRoom

    // === 고객이 방에 있는지 확인 (1:1 연결) ===
    if (room.remoteParticipants.size === 0) {
      console.warn('[CounselorCallView] 고객이 방에 없음 - 이미 나간 것으로 판단')
      isCallActive.value = false
      showManualEndModal.value = true
      notificationStore.notifyWarning('고객이 이미 나갔습니다')
      return
    }

    console.log('[CounselorCallView] 고객 확인됨')

    // === 마이크 활성화 (통화 화면 진입 시) ===
    ;(async () => {
      try {
        // setMicrophoneEnabled 대신 직접 getUserMedia + publishTrack 사용
        // (DataCloneError 회피)
        const stream = await navigator.mediaDevices.getUserMedia({
          audio: {
            echoCancellation: true,
            noiseSuppression: true,
            autoGainControl: true
          }
        })

        const audioTrack = stream.getAudioTracks()[0]
        if (audioTrack) {
          await room.localParticipant.publishTrack(audioTrack, {
            name: 'microphone',
            source: Track.Source.Microphone
          })

          // 마이크가 활성화되었으므로 음소거 상태는 false
          isMuted.value = false
          console.log('[CounselorCallView] 마이크 활성화 완료 (음소거 해제 상태)')
        }
      } catch (err) {
        console.error('[CounselorCallView] 마이크 활성화 실패:', err)
        notificationStore.notifyError('마이크 권한을 허용해주세요')
        // 실패 시 음소거 상태로 설정
        isMuted.value = true
      }
    })()

    // === 고객 오디오 딜레이/차단 파이프라인 구성 ===
    // 1) 이미 구독된 트랙이 있으면 즉시 파이프라인 생성
    ;(async () => {
      initAudioContext()

      for (const p of room.remoteParticipants.values()) {
        for (const pub of p.audioTrackPublications.values()) {
          if (pub.track) {
            await attachDelayedCustomerAudio(pub.track, p.identity)
          }
        }
      }

      // 2) 이후 새로 구독되는 트랙에 대해서도 적용
      room.on(RoomEvent.TrackSubscribed, async (track, publication, participant) => {
        if (track.kind === Track.Kind.Audio) {
          await attachDelayedCustomerAudio(track, participant.identity)
        }
      })

      // 3) 고객 STT 수신 → 폭력성 검사
      room.on(RoomEvent.DataReceived, async (payload, participant) => {
        const parsed = safeParsePayload(payload)
        if (!parsed || parsed.type !== 'stt') return

        // 다음 STT가 왔으면, 이전 차단이 있었다면 해제 타이머를 걸어둠
        if (participant?.identity) scheduleUnblockOnNextStt(participant.identity)

        const text = String(parsed.text || '').trim()
        if (!text) return

        const { toxic, score } = await analyzeToxicity(text)
        if (toxic && participant?.identity) {
          blockCustomerAudioUntilNextStt(participant.identity)
        }

        addSttMessage({
          speaker: 'customer',
          text,
          maskedText: toxic ? maskText(text) : '',
          hasProfanity: toxic,
          confidence: 1 - score,
          participantId: participant?.identity || null
        })
      })

      // 4) 상담원 STT(Whisper) 시작
      await startCounselorWhisperVad()
    })()

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

onBeforeUnmount(() => {
  console.log('[CounselorCallView] 컴포넌트 unmount 시작')

  // TODO: Whisper/VAD 정리 로직 필요 시 추가
  // stopCounselorWhisperVad()

  // TODO: 오디오 파이프라인 정리 로직 필요 시 추가
  // try {
  //   pipelines.clear()
  //   audioCtx?.close?.()
  // } catch {
  //   // ignore
  // } finally {
  //   audioCtx = null
  // }
})
</script>