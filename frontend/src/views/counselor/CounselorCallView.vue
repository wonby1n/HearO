<template>
  <div class="min-h-screen bg-gray-50">
    <!--숨겨진 음성 재생 컨테이너-->
    <div ref="audioContainer" style="display: none;"></div>
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
        <div class="lg:col-span-3 h-full overflow-hidden">
          <CustomerInfoSection />
        </div>

        <!-- 중앙: STT 자막 영역 -->
        <div class="lg:col-span-6 h-full overflow-hidden">
          <STTChatPanel
            :messages="sttMessages"
            :is-call-active="isCallActive"
            :counselor-name="counselorName"
            @toggle-profanity="handleToggleProfanity"
            @counselor-message="handleCounselorMessage"
          />
        </div>

        <!-- 우측: AI 가이드 및 메모 -->
        <div class="lg:col-span-3 h-full overflow-hidden">
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 h-full flex flex-col overflow-hidden gap-4">
            <!-- AI 가이드 섹션 (상단, 스크롤 가능) -->
            <div class="flex-2 overflow-hidden flex flex-col border-b border-gray-200">
              <div class="p-4 border-b border-gray-100">
                <h3 class="text-lg font-semibold text-gray-900">AI 가이드</h3>
              </div>
              <div class="flex-1 overflow-y-auto p-4">
                <AIGuidePanel class="h-full" />
              </div>
            </div>

            <!-- 메모 섹션 (하단, 스크롤 가능) -->
            <div class="flex-1 overflow-hidden flex flex-col">
              <div class="p-4">
                <CallMemoPanel v-model="memo" :saved-label="memoSaveLabel" />
              </div>
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
import CustomerInfoSection from '@/components/counselor/CustomerInfoSection.vue'
import STTChatPanel from '@/components/counselor/STTChatPanel.vue'
import CounselorCallControls from '@/components/counselor/CounselorCallControls.vue'
import CallMemoPanel from '@/components/counselor/CallMemoPanel.vue'
import AIGuidePanel from '@/components/counselor/AIGuidePanel.vue'
import AutoTerminationModal from '@/components/call/AutoTerminationModal.vue'
import ManualEndCallModal from '@/components/call/ManualEndCallModal.vue'
import { startConsultation } from '@/services/consultationService'
import { useNotificationStore } from '@/stores/notification'
import { useCallStore } from '@/stores/call'
import { useDashboardStore } from '@/stores/dashboard'
import { useAuthStore } from '@/stores/auth'
import axios from 'axios'
import { useAudioRecorder } from '@/composables/useAudioRecorder'

// 로컬 AI 서버 엔드포인트 (Vite env로 덮어쓸 수 있음)
const TOXIC_API_URL = import.meta.env.VITE_TOXIC_API_URL || 'http://127.0.0.1:8000/unsmile'

// 상담원: 고객 오디오 딜레이(기본 3초)
const CUSTOMER_AUDIO_DELAY_SEC = 3
const MUTE_POSTPAD_MS = 600

const router = useRouter()
const notificationStore = useNotificationStore()
const callStore = useCallStore()
const dashboardStore = useDashboardStore()
const authStore = useAuthStore()
const { startRecording, addTrack: addRecordingTrack, stopRecording, downloadRecording, cleanup: cleanupRecorder } = useAudioRecorder()

// 상담원 이름
const counselorName = computed(() => authStore.getUser?.name || '상담원')

// 음성 녹음 종료 및 파일 다운로드 (공통 헬퍼 — 수동·자동종료·고객종료 공유)
const stopAndSaveRecording = async () => {
  const recording = await stopRecording()
  if (recording) {
    const date = new Date().toISOString().slice(0, 10).replace(/-/g, '')
    downloadRecording(recording.blob, `녹음_${date}_${Date.now()}`)
  }
}

// --- 상태 정의 ---
const isCallActive = ref(true)
const isMuted = ref(false)
let currentMicStream = null // getUserMedia stream 참조 — 종료 시 트랙 정리용

// 오디오 파이프라인 상태
let audioCtx = null
const pipelines = new Map() // participantId -> { gain, delay, blocked, fallbackEl, analyser }
const audioContainer = ref(null)

// 자동 종료 모달
const showAutoTerminationModal = ref(false)
const showManualEndModal = ref(false)

// 폭언 3회 → 자동 종료 트리거 감지
watch(() => callStore.autoTerminationTriggered, (triggered) => {
  if (triggered) {
    showAutoTerminationModal.value = true
  }
})

const sttMessages = ref([])
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

// 메모 서버 저장 (통화 종료 시 /end API로 전송)
const saveMemoToServer = async () => {
  const memoValue = memo.value?.trim()
  const consultationId = callStore.currentCall?.consultationId ?? callStore.currentCall?.id

  if (!consultationId) {
    console.warn('[CounselorCallView] consultationId가 없어 메모를 저장하지 않습니다')
    return true
  }

  if (!memoValue) {
    console.log('[CounselorCallView] 메모가 비어있어 저장하지 않습니다')
    return true
  }

  try {
    // STT 메시지를 fullTranscript로 변환
    const fullTranscript = sttMessages.value
      .map(msg => `[${msg.speaker === 'agent' ? '상담원' : '고객'}] ${msg.text}`)
      .join('\n') || '상담 내용 없음'

    // 통화 종료 시 메모를 포함하여 finalizeConsultation API 호출
    await axios.patch(`/api/v1/consultations/${consultationId}/end`, {
      userMemo: memoValue,
      fullTranscript: fullTranscript,
      profanityCount: callStore.profanityCount || 0,
      avgAggressionScore: 0.0,
      maxAggressionScore: 0.0,
      terminationReason: 'NORMAL',
      durationSeconds: 0 // TODO: 실제 통화 시간 계산
    }, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('accessToken')}`
      }
    })

    console.log('✅ [CounselorCallView] 메모 저장 성공')
    notificationStore.notifySuccess('메모가 저장되었습니다')
    return true
  } catch (error) {
    console.error('❌ [CounselorCallView] 메모 저장 실패:', error)
    notificationStore.notifyError('메모 저장에 실패했습니다')
    return false
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
      if (audioPublication?.track?.mediaStreamTrack) {
        audioPublication.track.mediaStreamTrack.stop()
      }
      if (audioPublication) {
        await localParticipant.unpublishTrack(audioPublication.track)
      }
    } catch (err) {
      console.error('[CounselorCallView] 마이크 정리 실패:', err)
    }
  }

  isMuted.value = true
}

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
        // 기존 stream 정리 후 새로운 마이크 권한 요청
        if (currentMicStream) {
          currentMicStream.getTracks().forEach(t => t.stop())
        }
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
        currentMicStream = stream
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

// Manual end modal request
const handleManualEndRequest = async () => {
  console.log('[CounselorCallView] 통화 종료 버튼 클릭')

  try {
    // 통화 종료 버튼을 누르는 즉시 LiveKit 연결 종료 (고객에게 즉시 알림)
    isCallActive.value = false
    callStore.endCall()

    // 음성 녹음 종료 및 파일 다운로드
    await stopAndSaveRecording()

    if (callStore.livekitRoom) {
      console.log('[CounselorCallView] LiveKit 연결 즉시 종료 (통화 종료 버튼)')

      try {
        // 1. 마이크 정리 (stream 및 LiveKit 트랙)
        await stopLocalMicrophone()
        console.log('[CounselorCallView] 마이크 정리 완료')

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

    // 음성 녹음 종료 및 파일 다운로드
    await stopAndSaveRecording()

    // LiveKit 연결 종료
    if (callStore.livekitRoom) {
      console.log('[CounselorCallView] LiveKit 연결 종료 (자동 종료)')

      try {
        await stopLocalMicrophone()
        await callStore.livekitRoom.disconnect()
      } catch (disconnectError) {
        console.error('[CounselorCallView] LiveKit 연결 종료 실패 (자동 종료):', disconnectError)
      }

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

// 상담사 메시지 입력 핸들러
const handleCounselorMessage = (message) => {
  addSttMessage({
    speaker: 'agent',
    text: message,
    maskedText: '',
    hasProfanity: false,
    confidence: 1.0
  })
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

// ---- 오디오 파이프라인 헬퍼 ----
const ensureAudioContext = async () => {
  if (!audioCtx) {
    audioCtx = new (window.AudioContext || window.webkitAudioContext)()
  }
  if (audioCtx.state === 'suspended') {
    try { await audioCtx.resume() } catch {}
  }
  return audioCtx
}

const attachDelayedCustomerAudio = async (track, participantId) => {
  if (!track?.mediaStreamTrack) return

  const ctx = await ensureAudioContext()
  if (pipelines.has(participantId)) return

  // 1. 즉시 재생용 Fallback 오디오 생성 (지연 없음)
  // Web Audio가 활성화되기 전에도 소리가 나게 하여 끊김을 방지합니다.
  const fallbackEl = track.attach()
  audioContainer.value?.appendChild(fallbackEl)

  // 2. Web Audio 파이프라인 구성
  const stream = new MediaStream([track.mediaStreamTrack])
  const source = ctx.createMediaStreamSource(stream)

  const delayNode = ctx.createDelay(10)
  delayNode.delayTime.value = CUSTOMER_AUDIO_DELAY_SEC

  const gainNode = ctx.createGain()
  gainNode.gain.value = 1

  // 3. 신호 감지용 Analyser 추가 (TestRTC의 핵심)
  const analyser = ctx.createAnalyser()
  analyser.fftSize = 256
  const pcmData = new Float32Array(analyser.fftSize)

  // 연결: Source -> Delay -> Gain -> Destination & Analyser
  source.connect(delayNode)
  delayNode.connect(gainNode)
  gainNode.connect(ctx.destination)
  gainNode.connect(analyser)

  pipelines.set(participantId, {
    gain: gainNode,
    delay: delayNode,
    blocked: false,
    fallbackEl,
    analyser
  })

  // 4. 지연된 소리가 나오기 시작하는지 감시 루프
  const checkSignal = () => {
    const pipe = pipelines.get(participantId)
    if (!pipe) return

    analyser.getFloatTimeDomainData(pcmData)
    let sumSquares = 0
    for (const amplitude of pcmData) {
      sumSquares += amplitude * amplitude
    }
    const rms = Math.sqrt(sumSquares / pcmData.length)

    // 지연된 소리(RMS)가 일정 크기 이상 감지되면 원본 소리를 끔
    if (rms > 0.01) {
      console.log(`[Audio] ${participantId} 지연 신호 감지 -> 원본 음소거`)
      pipe.fallbackEl.muted = true
    } else {
      // 신호가 올 때까지 계속 확인
      requestAnimationFrame(checkSignal)
    }
  }

  checkSignal()
}

const setCustomerAudioMuted = (participantId, muted) => {
  const p = pipelines.get(participantId)
  if (!p) return
  const target = muted ? 0 : 1
  try {
    p.gain.gain.setTargetAtTime(target, audioCtx.currentTime, 0.02)
  } catch {
    p.gain.gain.value = target
  }
}

const blockCustomerAudioUntilNextStt = (participantId) => {
  const p = pipelines.get(participantId)
  if (!p) return
  p.blocked = true
  setCustomerAudioMuted(participantId, true)
}

const scheduleUnblockOnNextStt = (participantId) => {
  const p = pipelines.get(participantId)
  if (!p) return
  if (!p.blocked) return

  // 다음 STT가 왔을 때, "그 다음 구간"부터 다시 들리게 하는 보수적 방식
  // (딜레이 만큼 기다렐다가 해제)
  setTimeout(() => {
    // 아직도 blocked 상태면 해제
    const latest = pipelines.get(participantId)
    if (!latest) return
    latest.blocked = false
    setCustomerAudioMuted(participantId, false)
  }, CUSTOMER_AUDIO_DELAY_SEC * 1000 + MUTE_POSTPAD_MS)
}

// ---- LiveKit DataReceived payload 파싱 ----
const safeParsePayload = (payload) => {
  try {
    const text = new TextDecoder().decode(payload)
    return JSON.parse(text)
  } catch {
    return null
  }
}

// ---- unsmile 폭력성 검사 ----
const analyzeToxicity = async (text) => {
  if (!text?.trim()) return { toxic: false, score: 0 }
  try {
    const res = await fetch(TOXIC_API_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ text })
    })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const data = await res.json()
    return {
      toxic: !!data.toxic,
      score: typeof data.score === 'number' ? data.score : (typeof data.prob === 'number' ? data.prob : 0)
    }
  } catch (e) {
    console.warn('[CounselorCallView] 폭력성 검사 실패(우회):', e)
    return { toxic: false, score: 0 }
  }
}

// ---- 텍스트 마스킹 ----
const maskText = (text) => {
  if (!text) return ''
  // 너무 공격적으로 지우기보단, 글자 일부만 블러 표시
  return text.replace(/[\S]/g, '•')
}


onBeforeUnmount(() => {
  if (memoSaveTimeout) clearTimeout(memoSaveTimeout);
  if (!skipDraftSaveOnUnmount && memo.value?.trim().length) saveMemoDraft(memo.value);
})

defineExpose({ addSttMessage })

onMounted(() => {
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

    // === 상담 시작 API 호출 ===
    ;(async () => {
      try {
        const matchedData = dashboardStore.matchedData
        if (matchedData && matchedData.customerId && matchedData.registrationId) {
          console.log('[CounselorCallView] 상담 시작 API 호출:', {
            customerId: matchedData.customerId,
            registrationId: matchedData.registrationId
          })

          const response = await startConsultation({
            customerId: matchedData.customerId,
            registrationId: matchedData.registrationId
          })

          console.log('[CounselorCallView] 상담 시작 성공, consultationId:', response.consultationId)

          // consultationId를 callStore에 저장
          if (callStore.currentCall) {
            callStore.currentCall.consultationId = response.consultationId
            console.log('[CounselorCallView] consultationId 저장 완료:', callStore.currentCall)
          }

          // consultationId를 고객에게 전달 (LiveKit DataChannel)
          if (room && response.consultationId) {
            try {
              const payload = {
                type: 'consultationId',
                consultationId: response.consultationId
              }
              const bytes = new TextEncoder().encode(JSON.stringify(payload))
              await room.localParticipant.publishData(bytes, { reliable: true })
              console.log('[CounselorCallView] consultationId를 고객에게 전달:', response.consultationId)
            } catch (error) {
              console.error('[CounselorCallView] consultationId 전달 실패:', error)
            }
          }
        } else {
          console.warn('[CounselorCallView] 매칭 데이터 없음, 상담 시작 API 호출 불가')
        }
      } catch (error) {
        console.error('[CounselorCallView] 상담 시작 API 호출 실패:', error)
        notificationStore.notifyError('상담 시작 중 오류가 발생했습니다')
      }
    })()

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
        currentMicStream = stream

        const audioTrack = stream.getAudioTracks()[0]
        if (audioTrack) {
          await room.localParticipant.publishTrack(audioTrack, {
            name: 'microphone',
            source: Track.Source.Microphone
          })

          // 마이크가 활성화되었으므로 음소거 상태는 false
          isMuted.value = false
          console.log('[CounselorCallView] 마이크 활성화 완료 (음소거 해제 상태)')

          // 상담원 마이크를 녹음 믹스에 추가
          addRecordingTrack(audioTrack)
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
      await ensureAudioContext()

      for (const p of room.remoteParticipants.values()) {
        for (const pub of p.audioTrackPublications.values()) {
          if (pub.track) {
            await attachDelayedCustomerAudio(pub.track, p.identity)
            addRecordingTrack(pub.track.mediaStreamTrack)
          }
        }
      }

      // 2) 이후 새로 구독되는 트랙에 대해서도 적용
      room.on(RoomEvent.TrackSubscribed, async (track, publication, participant) => {
        if (track.kind === Track.Kind.Audio) {
          await attachDelayedCustomerAudio(track, participant.identity)
          addRecordingTrack(track.mediaStreamTrack)
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
    })()

    // 고객이 통화를 종료했을 때 이벤트 리스너 추가
    callStore.livekitRoom.on(RoomEvent.ParticipantDisconnected, async (participant) => {
      console.log('[CounselorCallView] 고객이 통화를 종료했습니다:', participant.identity)

      isCallActive.value = false

      // 음성 녹음 종료 및 파일 다운로드
      await stopAndSaveRecording()

      // 마이크 정리 및 LiveKit 연결 종료
      await stopLocalMicrophone()
      if (callStore.livekitRoom) {
        try {
          await callStore.livekitRoom.disconnect()
        } catch (err) {
          console.error('[CounselorCallView] LiveKit 연결 종료 실패 (고객 종료):', err)
        }
        callStore.setLivekitRoom(null)
      }

      // 통화 종료 모달 표시 (메모 저장용)
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

  // 마이크 stream 정리 (동기: 즉시 실행하여 브라우저 마이크 점유 해제)
  if (currentMicStream) {
    currentMicStream.getTracks().forEach(track => track.stop())
    currentMicStream = null
  }

  // 매칭 데이터 정리 (대시보드로 돌아갈 때)
  dashboardStore.clearMatchedData()
  console.log('[CounselorCallView] 매칭 데이터 정리 완료')

  // 음성 녹음 정리
  cleanupRecorder()

  // 오디오 파이프라인 정리
  try {
    for (const pipe of pipelines.values()) {
      pipe.fallbackEl?.pause()
      pipe.fallbackEl?.remove()
    }
    pipelines.clear()
    audioCtx?.close?.()
  } catch {
    // ignore
  } finally {
    audioCtx = null
  }
})
</script>