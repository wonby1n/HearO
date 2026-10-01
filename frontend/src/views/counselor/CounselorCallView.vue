<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
    <!--숨겨진 음성 재생 컨테이너-->
    <div ref="audioContainer" style="display: none;"></div>
    <!-- 자동 종료 모달 (시스템 트리거) -->
    <AutoTerminationModal :show="showAutoTerminationModal" :ai-summary="aiSummary" v-model:memo="memo"
      @confirm="handleAutoTerminationConfirm" />

    <!-- 수동 종료 확인 모달 -->
    <ManualEndCallModal :show="showManualEndModal" :ai-summary="aiSummary" v-model:memo="memo"
      @confirm="handleManualEndConfirm" />

    <!-- 통화 종료 확인 모달 -->
    <Teleport to="body">
      <div v-if="showEndConfirmModal" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="handleEndConfirmCancel"></div>
        <div class="relative bg-white rounded-2xl shadow-2xl p-7 w-full max-w-sm mx-4">
          <h3 class="text-xl font-bold text-gray-900 mb-3">상담 종료</h3>
          <p class="text-gray-600 mb-6">정말 상담을 종료하시겠습니까?</p>
          <div class="flex gap-3">
            <button @click="handleEndConfirmCancel"
              class="flex-1 px-4 py-3 border-2 border-gray-300 rounded-xl text-gray-700 font-semibold hover:bg-gray-50 transition-all">
              취소
            </button>
            <button @click="handleEndConfirmOk"
              class="flex-1 px-4 py-3 bg-red-600 text-white rounded-xl font-semibold hover:bg-red-700 transition-all shadow-lg shadow-red-600/30">
              종료
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 상단 헤더 -->
    <header class="bg-white shadow-md border-b-2 border-primary-100">
      <div class="max-w-[1920px] mx-auto px-8 py-5">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-primary-600 rounded-xl flex items-center justify-center shadow-lg">
                <svg class="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2L4 5v6.09c0 5.05 3.41 9.76 8 10.91 4.59-1.15 8-5.86 8-10.91V5l-8-3zm0 18.5c-3.76-1.08-6.5-5.06-6.5-9.41V6.3l6.5-2.6 6.5 2.6v4.79c0 4.35-2.74 8.33-6.5 9.41z"/>
                </svg>
              </div>
              <h1 class="text-2xl font-black text-gray-900 tracking-tight">Hear<span class="text-primary-600">O</span></h1>
            </div>
            <div class="h-8 w-px bg-gray-300"></div>
            <span class="text-sm font-semibold text-gray-600">상담 진행 중</span>
          </div>

          <CallTimer :isActive="isCallActive" />

          <CounselorCallControls :isMuted="isMuted" @mute-changed="handleMuteChanged"
            @call-end-requested="handleManualEndRequest" />
        </div>
      </div>
    </header>

    <!-- 메인 컨텐츠 -->
    <main class="max-w-[1920px] mx-auto p-6">
      <GridLayout
        v-model:layout="layout"
        :col-num="12"
        :row-height="60"
        :is-draggable="false"
        :is-resizable="false"
        :vertical-compact="true"
        :margin="[24, 24]"
        :use-css-transforms="true"
      >
        <!-- 고객 정보 패널 -->
        <GridItem
          :x="layout[0].x"
          :y="layout[0].y"
          :w="layout[0].w"
          :h="layout[0].h"
          :i="layout[0].i"
          :min-w="2"
          :min-h="8"
          drag-allow-from=".drag-handle"
        >
          <div class="h-full overflow-hidden">
            <CustomerInfoSection />
          </div>
        </GridItem>

        <!-- STT 자막 영역 -->
        <GridItem
          :x="layout[1].x"
          :y="layout[1].y"
          :w="layout[1].w"
          :h="layout[1].h"
          :i="layout[1].i"
          :min-w="4"
          :min-h="8"
          drag-allow-from=".drag-handle"
        >
          <div class="h-full overflow-hidden">
            <STTChatPanel
              :messages="sttMessages"
              :is-call-active="isCallActive"
              :counselor-name="counselorName"
              @toggle-profanity="handleToggleProfanity"
              @cancel-profanity="handleCancelProfanity"
              @counselor-message="handleCounselorMessage"
            />
          </div>
        </GridItem>

        <!-- AI 가이드 패널 -->
        <GridItem
          :x="layout[2].x"
          :y="layout[2].y"
          :w="layout[2].w"
          :h="layout[2].h"
          :i="layout[2].i"
          :min-w="2"
          :min-h="4"
          drag-allow-from=".drag-handle"
        >
          <div class="bg-white rounded-2xl shadow-lg border border-gray-200 h-full flex flex-col overflow-hidden">
            <div class="px-5 py-4 border-b border-gray-100 bg-gradient-to-r from-primary-50 to-blue-50">
              <h3 class="text-lg font-bold text-gray-900">AI 가이드</h3>
            </div>
            <div class="flex-1 overflow-y-auto p-5">
              <AIGuidePanel class="h-full" />
            </div>
          </div>
        </GridItem>

        <!-- 메모 패널 -->
        <GridItem
          :x="layout[3].x"
          :y="layout[3].y"
          :w="layout[3].w"
          :h="layout[3].h"
          :i="layout[3].i"
          :min-w="2"
          :min-h="4"
          drag-allow-from=".drag-handle"
        >
          <div class="bg-white rounded-2xl shadow-lg border border-gray-200 h-full flex flex-col overflow-hidden">
            <div class="px-5 py-4 border-b border-gray-100 bg-gradient-to-r from-primary-50 to-blue-50">
              <h3 class="text-lg font-bold text-gray-900">메모</h3>
            </div>
            <div class="flex-1 overflow-hidden p-5">
              <CallMemoPanel v-model="memo" :saved-label="memoSaveLabel" />
            </div>
          </div>
        </GridItem>
      </GridLayout>
    </main>
  </div>
</template>

<script setup>
import { ref, watch, computed, onBeforeUnmount, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { RoomEvent, Track } from 'livekit-client'
import { GridLayout, GridItem } from 'vue3-grid-layout-next'
import CallTimer from '@/components/counselor/CallTimer.vue'
import CustomerInfoSection from '@/components/counselor/CustomerInfoSection.vue'
import STTChatPanel from '@/components/counselor/STTChatPanel.vue'
import CounselorCallControls from '@/components/counselor/CounselorCallControls.vue'
import CallMemoPanel from '@/components/counselor/CallMemoPanel.vue'
import AIGuidePanel from '@/components/counselor/AIGuidePanel.vue'
import AutoTerminationModal from '@/components/call/AutoTerminationModal.vue'
import ManualEndCallModal from '@/components/call/ManualEndCallModal.vue'
import { startConsultation } from '@/services/consultationService'
import { generateAISummary } from '@/services/aiService'
import { analyzeToxicity } from '@/services/toxicityService'
import { useNotificationStore } from '@/stores/notification'
import { useCallStore } from '@/stores/call'
import { useDashboardStore } from '@/stores/dashboard'
import { useAuthStore } from '@/stores/auth'
import axios from 'axios'
import { useAudioRecorder } from '@/composables/useAudioRecorder'
import { useDelayedCustomerAudio } from '@/composables/useDelayedCustomerAudio'
import { useMemoDraft } from '@/composables/useMemoDraft'
import { PROFANITY_AUTO_TERMINATION_THRESHOLD } from '@/constants/call'

// 상담원: 고객 오디오 딜레이(기본 3초)
const CUSTOMER_AUDIO_DELAY_SEC = 3
const MUTE_POSTPAD_MS = 600

// 고객이 통화 화면에 늦게 들어와도 받을 수 있도록 consultationId를 여러 번 전송
const CONSULTATION_ID_SEND_DELAYS_MS = [500, 2000, 4000, 6000]

const router = useRouter()
const notificationStore = useNotificationStore()
const callStore = useCallStore()
const dashboardStore = useDashboardStore()
const authStore = useAuthStore()
const { startRecording, addTrack: addRecordingTrack, stopRecording, downloadRecording, cleanup: cleanupRecorder } = useAudioRecorder()
const customerAudio = useDelayedCustomerAudio({ delaySec: CUSTOMER_AUDIO_DELAY_SEC, mutePostpadMs: MUTE_POSTPAD_MS })
const { memo, memoSaveLabel, clearMemoDraft } = useMemoDraft(callStore)

// --- 레이아웃 (3열 구조: 고객정보 | 실시간자막 | AI가이드&메모) ---
const layout = ref([
  { i: 'customer-info', x: 0, y: 0, w: 4, h: 10, minW: 2, minH: 6 },
  { i: 'stt-chat', x: 4, y: 0, w: 4, h: 10, minW: 4, minH: 8 },
  { i: 'ai-guide', x: 8, y: 0, w: 4, h: 6, minW: 2, minH: 4 },
  { i: 'memo', x: 8, y: 8, w: 4, h: 4, minW: 2, minH: 3 }
])

const counselorName = computed(() => authStore.getUser?.name || '상담원')

// --- 상태 정의 ---
const isCallActive = ref(true)
const isMuted = ref(false)
const callStartTime = ref(null)
const audioContainer = ref(null)
const sttMessages = ref([])
const aiSummary = ref('')

const showAutoTerminationModal = ref(false)
const showManualEndModal = ref(false)
const showEndConfirmModal = ref(false)

let room = null // 리스너 해제용 LiveKit room 참조
let currentMicStream = null // getUserMedia stream 참조 — 종료 시 트랙 정리용
let callEndedAt = null // 종료 흐름(수동/자동/고객 종료)이 한 번만 실행되도록 막는 플래그 겸 종료 시각
const pendingTimers = []

// --- 공통 헬퍼 ---

// 대화록: AI 요약과 서버 저장에 같은 형식을 사용
const buildTranscript = () => sttMessages.value
  .map(msg => `[${msg.speaker === 'agent' ? '상담원' : '고객'}] ${msg.text}`)
  .join('\n')

const publishToCustomer = (payload) => {
  const bytes = new TextEncoder().encode(JSON.stringify(payload))
  return room.localParticipant.publishData(bytes, { reliable: true })
}

const safeParsePayload = (payload) => {
  try {
    return JSON.parse(new TextDecoder().decode(payload))
  } catch {
    return null
  }
}

// 너무 공격적으로 지우기보단, 글자를 가림 표시로 대체
const maskText = (text) => (text ? text.replace(/[\S]/g, '•') : '')

// 음성 녹음 종료 및 파일 다운로드
const stopAndSaveRecording = async () => {
  try {
    const recording = await stopRecording()
    if (recording) {
      const date = new Date().toISOString().slice(0, 10).replace(/-/g, '')
      downloadRecording(recording.blob, `녹음_${date}_${Date.now()}`)
    }
  } catch (e) {
    console.error('[CounselorCallView] 녹음 저장 실패:', e)
  }
}

// 마이크 정리: stored stream 트랙 stop + LiveKit 트랙 unpublish
const stopLocalMicrophone = async () => {
  if (currentMicStream) {
    currentMicStream.getTracks().forEach(track => track.stop())
    currentMicStream = null
  }

  if (callStore.livekitRoom) {
    try {
      const localParticipant = callStore.livekitRoom.localParticipant
      const audioPublication = localParticipant.getTrackPublication(Track.Source.Microphone)
      audioPublication?.track?.mediaStreamTrack?.stop()
      if (audioPublication) {
        await localParticipant.unpublishTrack(audioPublication.track)
      }
    } catch (err) {
      console.error('[CounselorCallView] 마이크 정리 실패:', err)
    }
  }

  isMuted.value = true
}

// --- 통화 종료 흐름 (수동 종료, 폭언 자동 종료, 고객 종료 공통) ---

const beginCallEnd = () => {
  if (callEndedAt) return false
  callEndedAt = Date.now()
  return true
}

// 녹음 저장 → 마이크 해제 → LiveKit 연결 종료
const teardownCall = async () => {
  isCallActive.value = false
  await stopAndSaveRecording()
  await stopLocalMicrophone()

  if (callStore.livekitRoom) {
    try {
      await callStore.livekitRoom.disconnect()
    } catch (err) {
      console.error('[CounselorCallView] LiveKit 연결 종료 실패:', err)
    }
    callStore.setLivekitRoom(null)
  }
}

// 종료 모달을 로딩 상태로 먼저 띄우고 AI 요약을 채움
const showEndModalWithSummary = async (modalRef, consultationId) => {
  aiSummary.value = null
  modalRef.value = true

  const transcript = buildTranscript()
  if (!consultationId || !transcript.trim()) {
    aiSummary.value = {
      title: '요약 생성 실패',
      subtitle: '상담 내용이 충분하지 않습니다',
      aiSummary: 'AI 요약을 생성할 수 없습니다.'
    }
    return
  }

  try {
    aiSummary.value = await generateAISummary(consultationId, transcript)
  } catch (aiError) {
    console.error('[CounselorCallView] AI 요약 생성 실패:', aiError)
    aiSummary.value = {
      title: '요약 생성 실패',
      subtitle: 'AI 요약 생성 중 오류가 발생했습니다',
      aiSummary: '잠시 후 다시 시도해주세요.'
    }
  }
}

// 폭언 누적 → 자동 종료
watch(() => callStore.autoTerminationTriggered, async (triggered) => {
  if (!triggered || !beginCallEnd()) return
  const consultationId = callStore.currentConsultationId

  // 고객에게 자동 종료 사유를 먼저 알리고, 수신을 보장하기 위해 잠시 대기 후 연결 종료
  if (room) {
    try {
      await publishToCustomer({ type: 'autoTermination', reason: 'profanity' })
      await new Promise(resolve => setTimeout(resolve, 500))
    } catch (e) {
      console.error('[CounselorCallView] 자동 종료 신호 전송 실패:', e)
    }
  }

  await teardownCall()
  await showEndModalWithSummary(showAutoTerminationModal, consultationId)
})

// 상담원이 종료 버튼 → 확인
const handleEndConfirmOk = async () => {
  showEndConfirmModal.value = false
  if (!beginCallEnd()) return
  const consultationId = callStore.currentConsultationId

  callStore.endCall()
  await teardownCall()
  await showEndModalWithSummary(showManualEndModal, consultationId)
}

// 고객이 먼저 통화 종료
const handleCustomerLeft = async (participant) => {
  if (!beginCallEnd()) return
  console.log('[CounselorCallView] 고객이 통화를 종료했습니다:', participant.identity)
  const consultationId = callStore.currentConsultationId

  await teardownCall()
  notificationStore.notifyInfo('고객이 통화를 종료했습니다')
  await showEndModalWithSummary(showManualEndModal, consultationId)
}

// 상담 결과(메모, 대화록, 폭언 횟수, 통화 시간) 서버 저장
const finalizeConsultation = async () => {
  const consultationId = callStore.currentCall?.consultationId ?? callStore.currentCall?.id
  if (!consultationId) {
    console.warn('[CounselorCallView] consultationId가 없어 상담 결과를 저장하지 않습니다')
    return true
  }

  const profanityCount = callStore.currentCall.profanityCount || 0
  const durationSeconds = callStartTime.value
    ? Math.floor(((callEndedAt ?? Date.now()) - callStartTime.value) / 1000)
    : 0

  try {
    await axios.patch(`/api/v1/consultations/${consultationId}/end`, {
      userMemo: memo.value?.trim() || '',
      fullTranscript: buildTranscript() || '상담 내용 없음',
      profanityCount,
      avgAggressionScore: 0.0,
      maxAggressionScore: 0.0,
      terminationReason: profanityCount >= PROFANITY_AUTO_TERMINATION_THRESHOLD ? 'PROFANITY_LIMIT' : 'NORMAL',
      durationSeconds
    })
    notificationStore.notifySuccess('메모가 저장되었습니다')
    return true
  } catch (error) {
    console.error('[CounselorCallView] 상담 결과 저장 실패:', error)
    notificationStore.notifyError('메모 저장에 실패했습니다')
    return false
  }
}

// 종료 모달 확인: 결과 저장 → 상담사 REST 전환 → 대시보드 이동
const finishCall = async ({ autoTerminated }) => {
  showManualEndModal.value = false
  showAutoTerminationModal.value = false

  try {
    if (autoTerminated) callStore.endCall()

    if (await finalizeConsultation()) clearMemoDraft()

    // 대시보드에서 상담사가 직접 다시 ON 하도록 REST로 전환
    try {
      await axios.patch('/api/v1/users/me/status', { status: 'REST' })
      dashboardStore.consultationStatus.isActive = false
    } catch (statusError) {
      console.error('[CounselorCallView] 상태 복구 실패:', statusError)
    }

    callStore.resetCall()

    // 자동 종료 시 대시보드에서 의무 휴식 모달(TimeModal) 표시
    if (autoTerminated) localStorage.setItem('triggerTimeModal', 'true')
    router.push({ name: 'dashboard' })

    if (autoTerminated) {
      notificationStore.notifyInfo('고객이 블랙리스트에 등록되었습니다. 10분간 의무 휴식이 필요합니다.')
    }
  } catch (error) {
    console.error('[CounselorCallView] 통화 종료 처리 실패:', error)
    notificationStore.notifyError('통화 종료 처리 중 오류가 발생했습니다')
    router.push({ name: 'dashboard' })
  }
}

const handleManualEndConfirm = () => finishCall({ autoTerminated: false })
const handleAutoTerminationConfirm = () => finishCall({ autoTerminated: true })

const handleManualEndRequest = () => {
  showEndConfirmModal.value = true
}

const handleEndConfirmCancel = () => {
  showEndConfirmModal.value = false
}

// --- 마이크 ---

const handleMuteChanged = async (muted) => {
  if (!callStore.livekitRoom) {
    console.warn('[CounselorCallView] LiveKit room이 없습니다')
    return
  }

  try {
    const localParticipant = callStore.livekitRoom.localParticipant
    const audioPublication = localParticipant.getTrackPublication(Track.Source.Microphone)

    if (audioPublication?.track) {
      if (muted) {
        await audioPublication.mute()
      } else {
        await audioPublication.unmute()
      }
      isMuted.value = muted
      return
    }

    // 트랙이 없으면 마이크를 새로 활성화
    try {
      currentMicStream?.getTracks().forEach(t => t.stop())
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      currentMicStream = stream
      const [audioTrack] = stream.getAudioTracks()

      if (audioTrack) {
        const publication = await localParticipant.publishTrack(audioTrack)
        if (muted) await publication.mute()
        isMuted.value = muted
      }
    } catch (micError) {
      console.error('[CounselorCallView] 마이크 활성화 실패:', micError)
      notificationStore.notifyError('마이크 권한을 허용해주세요')
    }
  } catch (error) {
    console.error('[CounselorCallView] 마이크 제어 실패:', error)
    isMuted.value = !muted
  }
}

// setMicrophoneEnabled 대신 직접 getUserMedia + publishTrack 사용 (DataCloneError 회피)
const enableMicrophone = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      audio: {
        echoCancellation: true,
        noiseSuppression: true,
        autoGainControl: true
      }
    })
    currentMicStream = stream

    const [audioTrack] = stream.getAudioTracks()
    if (audioTrack) {
      await room.localParticipant.publishTrack(audioTrack, {
        name: 'microphone',
        source: Track.Source.Microphone
      })
      isMuted.value = false
      addRecordingTrack(audioTrack)
    }
  } catch (err) {
    console.error('[CounselorCallView] 마이크 활성화 실패:', err)
    notificationStore.notifyError('마이크 권한을 허용해주세요')
    isMuted.value = true
  }
}

// --- STT 메시지 ---

const addSttMessage = (message) => {
  const timestamp = new Date().toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit'
  })

  sttMessages.value.push({
    ...message,
    timestamp,
    showOriginal: false,
    isProfanityCancelled: false
  })

  // 폭언 카운트 증가 (임계값 도달 시 callStore가 자동 종료 트리거)
  if (message.hasProfanity) {
    const newCount = callStore.incrementProfanityCount()
    notificationStore.notifyProfanity(newCount)
  }
}

const handleToggleProfanity = (index) => {
  sttMessages.value[index].showOriginal = !sttMessages.value[index].showOriginal
}

// 폭언 오탐 취소
const handleCancelProfanity = (index) => {
  const message = sttMessages.value[index]
  if (message.isProfanityCancelled) return

  message.isProfanityCancelled = true
  callStore.decrementProfanityCount()
  notificationStore.notifyInfo('폭언 감지가 취소되었습니다')
}

const handleCounselorMessage = (message) => {
  addSttMessage({
    speaker: 'agent',
    text: message,
    maskedText: '',
    hasProfanity: false,
    confidence: 1.0
  })
}

// --- LiveKit 이벤트 ---

const handleTrackSubscribed = async (track, publication, participant) => {
  if (track.kind !== Track.Kind.Audio) return
  await customerAudio.attach(track, participant.identity, audioContainer.value)
  addRecordingTrack(track.mediaStreamTrack)
}

// 고객 STT 수신 → 폭력성 검사 → 폭언이면 지연 재생 중인 음성 차단
const handleDataReceived = async (payload, participant) => {
  const parsed = safeParsePayload(payload)
  if (parsed?.type !== 'stt') return

  const participantId = participant?.identity
  // 다음 STT가 왔으면, 이전 차단이 있었다면 해제 타이머를 걸어둠
  if (participantId) customerAudio.scheduleUnblock(participantId)

  const text = String(parsed.text || '').trim()
  if (!text) return

  const { toxic, score } = await analyzeToxicity(text)
  if (toxic && participantId) customerAudio.blockUntilNextStt(participantId)

  addSttMessage({
    speaker: 'customer',
    text,
    maskedText: toxic ? maskText(text) : '',
    hasProfanity: toxic,
    confidence: 1 - score,
    participantId: participantId || null
  })
}

// 이미 구독된 고객 오디오 트랙에 지연 파이프라인 연결
const attachExistingCustomerAudio = async () => {
  await customerAudio.ensureAudioContext()
  for (const participant of room.remoteParticipants.values()) {
    for (const pub of participant.audioTrackPublications.values()) {
      if (pub.track) {
        await customerAudio.attach(pub.track, participant.identity, audioContainer.value)
        addRecordingTrack(pub.track.mediaStreamTrack)
      }
    }
  }
}

// 상담 레코드 생성 후 consultationId를 고객에게 전달
const startConsultationAndNotifyCustomer = async () => {
  const matchedData = dashboardStore.matchedData
  if (!matchedData?.customerId || !matchedData?.registrationId) {
    console.warn('[CounselorCallView] matchedData에 customerId 또는 registrationId 없음:', matchedData)
    return
  }

  try {
    const { consultationId } = await startConsultation({
      customerId: matchedData.customerId,
      registrationId: matchedData.registrationId
    })
    if (!consultationId) return

    callStore.setConsultationId(consultationId)

    const send = async () => {
      try {
        await publishToCustomer({ type: 'consultationId', consultationId, ts: Date.now() })
      } catch (sendErr) {
        console.error('[CounselorCallView] consultationId 전송 실패:', sendErr)
      }
    }
    CONSULTATION_ID_SEND_DELAYS_MS.forEach(delay => pendingTimers.push(setTimeout(send, delay)))
  } catch (err) {
    console.error('[CounselorCallView] 상담 시작 API 호출 실패:', err)
  }
}

onMounted(async () => {
  callStartTime.value = Date.now()

  room = callStore.livekitRoom
  if (!room) {
    console.warn('[CounselorCallView] LiveKit 연결이 없습니다. 대시보드로 돌아가세요.')
    return
  }

  room.on(RoomEvent.TrackSubscribed, handleTrackSubscribed)
  room.on(RoomEvent.DataReceived, handleDataReceived)
  room.on(RoomEvent.ParticipantDisconnected, handleCustomerLeft)

  // 녹음 믹스(고객 + 상담원)를 먼저 준비해야 이후 트랙이 녹음에 추가됨
  startRecording()
  attachExistingCustomerAudio()
  enableMicrophone()

  await startConsultationAndNotifyCustomer()
})

onBeforeUnmount(() => {
  if (room) {
    room.off(RoomEvent.TrackSubscribed, handleTrackSubscribed)
    room.off(RoomEvent.DataReceived, handleDataReceived)
    room.off(RoomEvent.ParticipantDisconnected, handleCustomerLeft)
  }
  pendingTimers.forEach(clearTimeout)

  // 브라우저 마이크 점유 즉시 해제
  currentMicStream?.getTracks().forEach(track => track.stop())
  currentMicStream = null

  dashboardStore.clearMatchedData()
  cleanupRecorder()
  customerAudio.cleanup()
})
</script>