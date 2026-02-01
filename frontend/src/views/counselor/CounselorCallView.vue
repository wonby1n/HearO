<template>
  <div class="min-h-screen bg-gray-50">
    <!--숨겨진 음성 재생 컨테이너-->
    <div ref="audioContainer" style="display: none;"></div>
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
import { mockCustomerInfo } from '@/mocks/counselor'
import { fetchCustomerData } from '@/services/customerService'
import { saveConsultationMemo } from '@/services/consultationService'
import { RoomEvent, Track } from 'livekit-client'
import { useNotificationStore } from '@/stores/notification'
import { useCallStore } from '@/stores/call'

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

//음성을 재생할 컨테이너
const audioContainer = ref(null)

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

// STT 메시지 (실시간)
const sttMessages = ref([])

// 1) 고객 STT → 폭력성(unsmile) 검사 → 자막 블러/차단 트리거
// 2) 고객 오디오: 3초 딜레이 재생 + 폭력성 감지 시 다음 STT까지 묵음
// 3) 상담원 STT: 로컬 Whisper(무음 기반 구간 전송)

// ---- 텍스트 마스킹(간단) ----
const maskText = (text) => {
  if (!text) return ''
  // 너무 공격적으로 지우기보단, 글자 일부만 블러 표시
  return text.replace(/[\S]/g, '•')
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

// ---- LiveKit DataReceived payload 파싱 ----
const safeParsePayload = (payload) => {
  try {
    const text = new TextDecoder().decode(payload)
    return JSON.parse(text)
  } catch {
    return null
  }
}

// ---- 고객 오디오 딜레이/묵음 파이프라인 ----
let audioCtx = null
const pipelines = new Map() // participantId -> { gain, delay, blocked, fallbackEl, analyser }

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
  // (딜레이 만큼 기다렸다가 해제)
  setTimeout(() => {
    // 아직도 blocked 상태면 해제
    const latest = pipelines.get(participantId)
    if (!latest) return
    latest.blocked = false
    setCustomerAudioMuted(participantId, false)
  }, CUSTOMER_AUDIO_DELAY_SEC * 1000 + MUTE_POSTPAD_MS)
}

// ---- Whisper STT (상담원 로컬) ----
let vadCtx = null
let vadStream = null
let vadSource = null
let vadProcessor = null
let vadBuffers = []
let vadSpeeching = false
let vadLastVoiceAt = 0
let vadStartAt = 0

const floatTo16BitPCM = (float32) => {
  const out = new Int16Array(float32.length)
  for (let i = 0; i < float32.length; i++) {
    let s = Math.max(-1, Math.min(1, float32[i]))
    out[i] = s < 0 ? s * 0x8000 : s * 0x7fff
  }
  return out
}

const encodeWav16kMono = async (float32, inputSampleRate) => {
  // 간단한 linear resample → 16k
  const targetRate = 16000
  const ratio = inputSampleRate / targetRate
  const targetLength = Math.floor(float32.length / ratio)
  const resampled = new Float32Array(targetLength)
  for (let i = 0; i < targetLength; i++) {
    const idx = i * ratio
    const i0 = Math.floor(idx)
    const i1 = Math.min(float32.length - 1, i0 + 1)
    const t = idx - i0
    resampled[i] = float32[i0] * (1 - t) + float32[i1] * t
  }

  const pcm16 = floatTo16BitPCM(resampled)
  const headerSize = 44
  const buffer = new ArrayBuffer(headerSize + pcm16.byteLength)
  const view = new DataView(buffer)
  const writeString = (offset, str) => {
    for (let i = 0; i < str.length; i++) view.setUint8(offset + i, str.charCodeAt(i))
  }

  writeString(0, 'RIFF')
  view.setUint32(4, 36 + pcm16.byteLength, true)
  writeString(8, 'WAVE')
  writeString(12, 'fmt ')
  view.setUint32(16, 16, true) // PCM
  view.setUint16(20, 1, true) // PCM
  view.setUint16(22, 1, true) // mono
  view.setUint32(24, targetRate, true)
  view.setUint32(28, targetRate * 2, true) // byte rate
  view.setUint16(32, 2, true) // block align
  view.setUint16(34, 16, true) // bits
  writeString(36, 'data')
  view.setUint32(40, pcm16.byteLength, true)
  new Uint8Array(buffer, headerSize).set(new Uint8Array(pcm16.buffer))
  return new Blob([buffer], { type: 'audio/wav' })
}

const sendToWhisper = async (wavBlob) => {
  try {
    const form = new FormData()
    form.append('file', wavBlob, 'audio.wav')
    const res = await fetch(WHISPER_API_URL, {
      method: 'POST',
      body: form
    })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const data = await res.json().catch(() => null)

    // 서버가 {text:"..."} 또는 {transcript:"..."} 등을 반환한다고 가정
    const text = data?.text ?? data?.transcript ?? data?.result ?? ''
    return String(text || '').trim()
  } catch (e) {
    console.warn('[CounselorCallView] Whisper STT 실패:', e)
    return ''
  }
}

const startCounselorWhisperVad = async () => {
  try {
    if (vadCtx) return
    vadCtx = new (window.AudioContext || window.webkitAudioContext)()
    vadStream = await navigator.mediaDevices.getUserMedia({ audio: true })
    vadSource = vadCtx.createMediaStreamSource(vadStream)
    // ScriptProcessor는 deprecated지만 호환성 좋음
    vadProcessor = vadCtx.createScriptProcessor(2048, 1, 1)

    vadProcessor.onaudioprocess = async (e) => {
      const input = e.inputBuffer.getChannelData(0)

      // RMS 계산
      let sum = 0
      for (let i = 0; i < input.length; i++) sum += input[i] * input[i]
      const rms = Math.sqrt(sum / input.length)
      const now = performance.now()

      const isVoice = rms > 0.02
      if (isVoice) {
        if (!vadSpeeching) {
          vadSpeeching = true
          vadStartAt = now
          vadBuffers = []
        }
        vadLastVoiceAt = now
        vadBuffers.push(new Float32Array(input))
      } else if (vadSpeeching) {
        // 말하던 중 무음이 VAD_SILENCE_MS 이상이면 한 구간 종료
        if (now - vadLastVoiceAt >= VAD_SILENCE_MS) {
          const dur = now - vadStartAt
          vadSpeeching = false

          if (dur >= VAD_MIN_UTTER_MS && vadBuffers.length) {
            const total = vadBuffers.reduce((acc, a) => acc + a.length, 0)
            const merged = new Float32Array(total)
            let off = 0
            for (const b of vadBuffers) {
              merged.set(b, off)
              off += b.length
            }
            vadBuffers = []

            const wav = await encodeWav16kMono(merged, vadCtx.sampleRate)
            const text = await sendToWhisper(wav)
            if (text) {
              addSttMessage({
                speaker: 'agent',
                text,
                maskedText: '',
                hasProfanity: false,
                confidence: 0.9
              })
            }
          } else {
            vadBuffers = []
          }
        }
      }
    }

    vadSource.connect(vadProcessor)
    vadProcessor.connect(vadCtx.destination) // 처리 구동용(출력 음량은 거의 무시됨)
    console.log('[CounselorCallView] 상담원 Whisper VAD 시작')
  } catch (e) {
    console.warn('[CounselorCallView] Whisper VAD 시작 실패:', e)
  }
}

const stopCounselorWhisperVad = async () => {
  try {
    vadProcessor?.disconnect?.()
    vadSource?.disconnect?.()
    vadStream?.getTracks?.().forEach(t => t.stop())
    await vadCtx?.close?.()
  } catch {
    // ignore
  } finally {
    vadCtx = null
    vadStream = null
    vadSource = null
    vadProcessor = null
    vadBuffers = []
    vadSpeeching = false
  }
}

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

  stopCounselorWhisperVad()
  try {
    for (const pipe of pipelines.values()) {
      pipe.fallbackEl?.pause()
      pipe.fallbackEl?.remove()
    }
    pipelines.clear()
    audioCtx?.close?.()
  } catch (e) {
    console.warn(e)
  } finally {
    audioCtx = null
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

  // ✅ 오탐 가능성: 사용자가 '확인'을 눌렀다면(원문 표시) 해당 구간을 들을 수 있게 차단을 해제
  const msg = sttMessages.value[index]
  if (msg?.hasProfanity && msg?.showOriginal && msg.participantId) {
    const p = pipelines.get(msg.participantId)
    if (p) {
      p.blocked = false
      setCustomerAudioMuted(msg.participantId, false)
    }
  }
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
    showOriginal: false,
    participantId: message.participantId || null
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

    // === 고객 오디오 딜레이/차단 파이프라인 구성 ===
    // 1) 이미 구독된 트랙이 있으면 즉시 파이프라인 생성
    ;(async () => {
      const room = callStore.livekitRoom
      await ensureAudioContext()

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
  // Whisper/VAD 정리
  stopCounselorWhisperVad()

  // 오디오 파이프라인 정리
  try {
    pipelines.clear()
    audioCtx?.close?.()
  } catch {
    // ignore
  } finally {
    audioCtx = null
  }
})
</script>

<style scoped>
/* CounselorCallView 전용 스타일 */
</style>