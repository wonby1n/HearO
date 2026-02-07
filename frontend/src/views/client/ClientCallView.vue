<template>
  <div class="client-call-view">
    <!-- 자동 종료 모달 -->
    <Teleport to="body">
      <div
        v-if="showAutoTerminationModal"
        class="modal-overlay"
      >
        <div class="modal-content auto-term">
          <div class="icon-container warning">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              />
            </svg>
          </div>
          <h3 class="modal-title">통화가 자동 종료되었습니다</h3>
          <p class="modal-message center">
            폭언 3회로 인해<br>
            통화가 자동으로 종료되었습니다.
          </p>
          <div class="modal-actions">
            <button @click="handleAutoTerminationConfirm" class="modal-btn confirm-auto">
              확인
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 메인 컨텐츠 -->
    <div class="main-content">
      <!-- 통화 시간 -->
      <div class="call-timer">
        <span class="timer-text">{{ formattedCallDuration }}</span>
        <!-- 음성 인식 막대 -->
        <div class="audio-visualizer">
          <div
            v-for="i in 5"
            :key="i"
            class="audio-bar"
            :class="{ active: callStore.isInCall }"
            :style="{ animationDelay: `${(i - 1) * 0.1}s` }"
          ></div>
        </div>
      </div>

      <!-- 상담원 아이콘 + 상태 -->
      <div class="counselor-status">
        <div class="counselor-icon">
          <svg class="headset-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 1c-4.97 0-9 4.03-9 9v7c0 1.66 1.34 3 3 3h3v-8H5v-2c0-3.87 3.13-7 7-7s7 3.13 7 7v2h-4v8h3c1.66 0 3-1.34 3-3v-7c0-4.97-4.03-9-9-9z"/>
          </svg>
        </div>
        <!-- 통화 상태 표시 (초록색 점) -->
        <div
          v-if="callStore.isInCall"
          class="status-indicator online"
        ></div>
        <div
          v-else-if="callStore.isConnecting"
          class="status-indicator connecting"
        ></div>
      </div>

      <!-- 상태 텍스트 -->
      <div class="status-text">
        <h2 class="status-title">
          {{ callStore.isInCall ? '상담 진행 중' : callStore.isConnecting ? '연결 중...' : '대기 중' }}
        </h2>
        <p class="status-description">
          {{ callStore.isInCall ? '실시간 음성 상담이 진행되고 있습니다.' : callStore.isConnecting ? '상담원을 연결하고 있습니다.' : '상담원 연결을 기다리고 있습니다.' }}
        </p>
      </div>

      <!-- 통화 컨트롤 버튼 -->
      <div class="call-controls">
        <!-- 스피커폰 버튼 (UI만) -->
        <button
          @click="toggleSpeaker"
          :class="['control-btn', { active: isSpeakerOn }]"
          :title="isSpeakerOn ? '스피커폰 끄기' : '스피커폰 켜기'"
        >
          <div class="control-icon-wrapper">
            <!-- 스피커폰 켜짐 -->
            <svg v-if="isSpeakerOn" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M9.383 3.076A1 1 0 0110 4v12a1 1 0 01-1.707.707L4.586 13H2a1 1 0 01-1-1V8a1 1 0 011-1h2.586l3.707-3.707a1 1 0 011.09-.217zM14.657 2.929a1 1 0 011.414 0A9.972 9.972 0 0119 10a9.972 9.972 0 01-2.929 7.071 1 1 0 01-1.414-1.414A7.971 7.971 0 0017 10c0-2.21-.894-4.208-2.343-5.657a1 1 0 010-1.414zm-2.829 2.828a1 1 0 011.415 0A5.983 5.983 0 0115 10a5.984 5.984 0 01-1.757 4.243 1 1 0 01-1.415-1.415A3.984 3.984 0 0013 10a3.983 3.983 0 00-1.172-2.828 1 1 0 010-1.415z" clip-rule="evenodd"/>
            </svg>
            <!-- 스피커 꺼짐 (소리 작음 - 단일 파동) -->
            <svg v-else class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M9.383 3.076A1 1 0 0110 4v12a1 1 0 01-1.707.707L4.586 13H2a1 1 0 01-1-1V8a1 1 0 011-1h2.586l3.707-3.707a1 1 0 011.09-.217zM11.828 5.757a1 1 0 011.415 0A5.983 5.983 0 0115 10a5.984 5.984 0 01-1.757 4.243 1 1 0 01-1.415-1.415A3.984 3.984 0 0013 10a3.983 3.983 0 00-1.172-2.828 1 1 0 010-1.415z" clip-rule="evenodd"/>
            </svg>
          </div>
        </button>

        <!-- 통화 종료 버튼 -->
        <button
          @click="endCall"
          class="control-btn end-call"
          title="통화 종료"
        >
          <div class="control-icon-wrapper end">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z"/>
              <path d="M16.707 3.293a1 1 0 010 1.414L15.414 6l1.293 1.293a1 1 0 01-1.414 1.414L14 7.414l-1.293 1.293a1 1 0 11-1.414-1.414L12.586 6l-1.293-1.293a1 1 0 011.414-1.414L14 4.586l1.293-1.293a1 1 0 011.414 0z"/>
            </svg>
          </div>
        </button>

        <!-- 음소거 버튼 -->
        <button
          @click="toggleMute"
          :class="['control-btn', { active: isMuted }]"
          :title="isMuted ? '음소거 해제' : '음소거'"
        >
          <div class="control-icon-wrapper">
            <svg v-if="!isMuted" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M7 4a3 3 0 016 0v4a3 3 0 11-6 0V4zm4 10.93A7.001 7.001 0 0017 8a1 1 0 10-2 0A5 5 0 015 8a1 1 0 00-2 0 7.001 7.001 0 006 6.93V17H6a1 1 0 100 2h8a1 1 0 100-2h-3v-2.07z" clip-rule="evenodd"/>
            </svg>
            <svg v-else class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M7 4a3 3 0 016 0v4a3 3 0 11-6 0V4zm4 10.93A7.001 7.001 0 0017 8a1 1 0 10-2 0A5 5 0 015 8a1 1 0 00-2 0 7.001 7.001 0 006 6.93V17H6a1 1 0 100 2h8a1 1 0 100-2h-3v-2.07z" clip-rule="evenodd"/>
              <path d="M3.293 3.293a1 1 0 011.414 0l12 12a1 1 0 01-1.414 1.414l-12-12a1 1 0 010-1.414z"/>
            </svg>
          </div>
        </button>
      </div>
    </div>

    <!-- 통화 종료 확인 모달 -->
    <Teleport to="body">
      <div
        v-if="showConfirmModal"
        class="modal-overlay"
        @click.self="closeConfirmModal"
      >
        <div class="modal-content">
          <h3 class="modal-title">통화 종료</h3>
          <p class="modal-message">상담을 종료하시겠습니까?</p>
          <div class="modal-actions">
            <button @click="closeConfirmModal" class="modal-btn cancel">취소</button>
            <button @click="confirmEndCall" class="modal-btn confirm">종료</button>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCallStore } from '@/stores/call'
import { useCustomerStore } from '@/stores/customer'
import { useLiveKit } from '@/composables/useLiveKit'
import { AUTO_TERMINATION_REDIRECT_DELAY_MS } from '@/constants/call'
import { RoomEvent } from 'livekit-client'

// =========================
// 고객 STT(Web Speech) → 상담원으로 전송
// =========================
let recognition = null
let sttEnabled = true // STT 활성화 상태 (음소거 시 false)

const getSpeechRecognition = () => {
  return window.SpeechRecognition || window.webkitSpeechRecognition || null
}

const stopCustomerSTT = () => {
  sttEnabled = false
  try {
    if (recognition) {
      recognition.onresult = null
      recognition.onerror = null
      recognition.onend = null
      recognition.stop()
      recognition = null
    }
  } catch (e) {
    // ignore
  }
}

const sendCustomerSttToCounselor = async (text) => {
  const r = room.value || callStore.livekitRoom
  if (!r) return
  const payload = {
    type: 'stt',
    from: r.localParticipant?.identity ?? 'customer',
    text,
    ts: Date.now()
  }
  try {
    const bytes = new TextEncoder().encode(JSON.stringify(payload))
    r.localParticipant.publishData(bytes, { reliable: true })
  } catch (e) {
    console.warn('[ClientCallView] STT 전송 실패:', e)
  }
}

const startCustomerSTT = async () => {
  console.log('[ClientCallView] startCustomerSTT 호출됨')
  console.log('[ClientCallView] room.value:', room.value)
  console.log('[ClientCallView] callStore.livekitRoom:', callStore.livekitRoom)

  // room 연결된 이후에만
  if (!(room.value || callStore.livekitRoom)) {
    console.warn('[ClientCallView] STT 스킵: room 연결 안됨')
    return
  }

  const SR = getSpeechRecognition()
  console.log('[ClientCallView] SpeechRecognition API:', SR)
  if (!SR) {
    console.warn('[ClientCallView] Web Speech STT 미지원 브라우저')
    return
  }

  // 기존 인식 정리 (sttEnabled 유지)
  if (recognition) {
    recognition.onresult = null
    recognition.onerror = null
    recognition.onend = null
    try { recognition.stop() } catch {}
    recognition = null
  }

  sttEnabled = true

  recognition = new SR()
  recognition.lang = 'ko-KR'
  recognition.interimResults = true
  recognition.continuous = true

  recognition.onerror = (ev) => {
    console.warn('[ClientCallView] STT 오류:', ev?.error, ev)
  }

  recognition.onstart = () => {
    console.log('[ClientCallView] STT onstart 이벤트 발생')
  }

  recognition.onend = () => {
    console.log('[ClientCallView] STT onend 이벤트 발생, isInCall:', callStore.isInCall, 'sttEnabled:', sttEnabled)
    // 통화 중이고 STT가 활성화되어 있으면 자동 재시작
    if (callStore.isInCall && sttEnabled) {
      console.log('[ClientCallView] STT 재시작 시도')
      setTimeout(() => {
        try { recognition?.start?.() } catch (e) { console.warn('[ClientCallView] STT 재시작 실패:', e) }
      }, 300) // Android에서 즉시 재시작 시 실패할 수 있어 딜레이 추가
    }
  }

  recognition.onresult = async (event) => {
    console.log('[ClientCallView] STT onresult 이벤트:', event.results.length, '개 결과')
    let finalText = ''
    for (let i = event.resultIndex; i < event.results.length; i++) {
      const res = event.results[i]
      const t = res[0]?.transcript ?? ''
      console.log('[ClientCallView] STT 결과:', t, 'isFinal:', res.isFinal)
      if (res.isFinal) finalText += t
    }

    const cleaned = finalText.trim()
    if (cleaned) await sendCustomerSttToCounselor(cleaned)
  }

  try {
    recognition.start()
    console.log('[ClientCallView] 고객 STT 시작')
  } catch (e) {
    console.warn('[ClientCallView] 고객 STT 시작 실패:', e)
  }
}

const router = useRouter()
const callStore = useCallStore()
const customerStore = useCustomerStore()

// LiveKit composable
const {
  room,
  isConnected,
  isConnecting,
  error: livekitError,
  connectionState,
  connect,
  disconnect,
  enableMicrophone,
  startAudioPlayback
} = useLiveKit({
  onParticipantDisconnected: (participant) => {
    // 상담원이 통화를 종료했을 때
    console.log('[ClientCallView] 상담원이 통화를 종료했습니다:', participant.identity)

    // 폭언 자동 종료 진행 중이면 watcher에서 처리하므로 여기서는 무시
    if (receivedAutoTermination.value || showAutoTerminationModal.value) return

    // 타이머 정리
    if (timerInterval) {
      clearInterval(timerInterval)
    }

    // consultationId를 resetCall 전에 저장
    const consultationId = callStore.currentCall?.consultationId || callStore.currentCall?.registrationId

    // call store 정리
    callStore.setLivekitRoom(null)
    callStore.endCall()
    callStore.resetCall()

    // 통화 종료 페이지로 이동
    router.push({
      name: 'client-call-end',
      query: {
        duration: callDuration.value,
        reason: 'counselor_ended',
        consultationId
      }
    })
  },
  onTrackSubscribed: (track, publication, participant) => {
    console.log('[Client] 원격 오디오 트랙 수신:', participant.identity)
  },
  onDisconnected: (reason) => {
    console.log('[Client] 연결 해제됨:', reason)
    handleDisconnected(reason)
  },
  onError: (err) => {
    console.error('[Client] LiveKit 에러:', err)
    callStore.setConnectionError(err.message)
  }
})

// 상태 관리
const callDuration = ref(0)
const queuePosition = ref(3) // 테스트용 대기 순번
// isMuted는 useLiveKit의 livekitMuted 사용 (중복 제거)
// isSpeakerOn은 useLiveKit의 isSpeakerEnabled 사용 (기본값: true - 켜짐)
const showConfirmModal = ref(false)
const showAutoTerminationModal = ref(false)
const receivedAutoTermination = ref(false)
const autoTerminationAudio = ref(null)

let timerInterval = null
let autoRedirectTimer = null

// 자동 종료 감지
watch(() => callStore.autoTerminationTriggered, (triggered) => {
  if (triggered) {
    showAutoTerminationModal.value = true
    // 자동 리다이렉트 제거 - 사용자가 확인 버튼을 눌러야 함
  }
})

// 종료 페이지로 이동하는 공통 함수
const proceedToEndPage = async () => {
  const finalDuration = callDuration.value
  const consultationId = callStore.currentCall?.consultationId || callStore.currentCall?.registrationId

  if (timerInterval) {
    clearInterval(timerInterval)
  }

  if (autoRedirectTimer) {
    clearTimeout(autoRedirectTimer)
  }

  // 마이크 종료 (STT 중지)
  stopCustomerSTT()

  await disconnect()
  callStore.resetCall()

  router.push({
    name: 'client-call-end',
    query: {
      duration: finalDuration,
      autoTerminated: 'true',
      consultationId
    }
  })
}

// 자동 종료 모달 확인 버튼 핸들러
const handleAutoTerminationConfirm = async () => {
  try {
    // 1. 음성 재생
    if (!autoTerminationAudio.value) {
      autoTerminationAudio.value = new Audio('/src/assets/Endvoice.wav')
    }
    
    console.log('[ClientCall] 자동 종료 음성 재생 시작')
    await autoTerminationAudio.value.play()
    
    // 2. 음성 재생 완료 후 종료 페이지로 이동
    autoTerminationAudio.value.onended = async () => {
      console.log('[ClientCall] 음성 재생 완료, 종료 페이지로 이동')
      await proceedToEndPage()
    }
  } catch (error) {
    console.error('[ClientCall] 음성 재생 실패:', error)
    // 음성 재생 실패 시에도 종료 페이지로 이동
    await proceedToEndPage()
  }
}

// 통화 시간 포맷팅 (mm:ss)
const formattedCallDuration = computed(() => {
  const minutes = Math.floor(callDuration.value / 60)
  const seconds = callDuration.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

// 스피커폰 상태 (UI만 - 실제 기능 없음)
const isSpeakerOn = ref(false)

// 스피커폰 토글 (UI만)
const toggleSpeaker = () => {
  isSpeakerOn.value = !isSpeakerOn.value
  console.log('[Client] 스피커폰 상태 (UI):', isSpeakerOn.value)
}

// 음소거 상태 (로컬 관리)
const isMuted = ref(false)

// 음소거 토글
const toggleMute = async () => {
  const currentRoom = callStore.livekitRoom
  if (!currentRoom) {
    console.warn('[Client] 음소거 토글 실패: LiveKit room 없음')
    return
  }

  const newMuteState = !isMuted.value

  try {
    // 발행된 오디오 트랙을 직접 mute/unmute
    const audioPublications = currentRoom.localParticipant.audioTrackPublications
    for (const [, publication] of audioPublications) {
      if (publication.track) {
        if (newMuteState) {
          await publication.mute()
        } else {
          await publication.unmute()
        }
      }
    }

    isMuted.value = newMuteState

    // STT 제어
    if (newMuteState) {
      stopCustomerSTT()
    } else {
      startCustomerSTT()
    }

    console.log('[Client] 음소거 상태:', newMuteState)
  } catch (err) {
    console.error('[Client] 음소거 토글 실패:', err)
  }
}


// 모달 닫기
const closeConfirmModal = () => {
  showConfirmModal.value = false
}

// 통화 종료 확인
const endCall = async () => {
  // 현재 통화 시간 저장
  const finalDuration = callDuration.value

  if (timerInterval) {
    clearInterval(timerInterval)
  }

  // 마이크 종료 (STT 중지)
  stopCustomerSTT()

  // call store의 LiveKit 연결 종료 (ClientWaitingView에서 만든 연결)
  if (callStore.livekitRoom) {
    console.log('[ClientCallView] LiveKit 연결 종료')
    await callStore.livekitRoom.disconnect()
    callStore.setLivekitRoom(null)
  }

  // 자체 LiveKit 연결도 종료 (혹시 있다면)
  await disconnect()

  const consultationId = callStore.currentCall?.consultationId || callStore.currentCall?.registrationId

  callStore.endCall()
  callStore.resetCall()

  // 통화 종료 페이지로 이동 (통화 시간 전달)
  router.push({
    name: 'client-call-end',
    query: {
      duration: finalDuration,
      consultationId
    }
  })
}

// 연결 해제 핸들러
const handleDisconnected = (reason) => {
  if (callStore.isInCall) {
    console.warn('[Client] 예상치 못한 연결 해제:', reason)
  }
}

// 컴포넌트 마운트 시 초기화
onMounted(async () => {
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


    const customerId = sessionStorage.getItem('clientCustomerId') || customerStore.currentCustomer.id
    const registrationId = sessionStorage.getItem('clientRegistrationId')

    startCustomerSTT()
    callStore.startCall({
      id: `client-call-${Date.now()}`,
      customerId: customerId ? parseInt(customerId) : null,
      registrationId: registrationId ? parseInt(registrationId) : null,
      roomToken: 'test-token'
    })

    console.log('[ClientCallView] callStore.currentCall:', callStore.currentCall)


  // 통화 시간 타이머
  timerInterval = setInterval(() => {
    if (callStore.isInCall) {
      callDuration.value++
    }
  }, 1000)

  // 마이크 활성화 (통화 화면 진입 시)
  if (callStore.livekitRoom) {
    console.log('[ClientCallView] 기존 LiveKit 연결 사용:', callStore.livekitRoom.name)

    // 상담원으로부터 데이터 수신 (consultationId, autoTermination 등)
    callStore.livekitRoom.on(RoomEvent.DataReceived, (payload, participant) => {
      try {
        const data = JSON.parse(new TextDecoder().decode(payload))
        if (data.type === 'consultationId' && data.consultationId) {
          callStore.setConsultationId(data.consultationId)
          console.log('[ClientCallView] consultationId 수신:', data.consultationId)
        }
        // 폭언 자동 종료 신호 수신
        if (data.type === 'autoTermination') {
          console.log('[ClientCallView] 자동 종료 신호 수신:', data.reason)
          receivedAutoTermination.value = true
          callStore.autoTerminationTriggered = true
        }
      } catch (e) {
        // JSON 파싱 실패 무시
      }
    })

    try {
      // callStore.livekitRoom을 직접 사용해서 마이크 활성화
      // (useLiveKit의 room.value와 callStore.livekitRoom은 다른 객체)
      console.log('[ClientCallView] 마이크 권한 요청 중...')
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true
        }
      })

      const audioTrack = stream.getAudioTracks()[0]
      if (audioTrack) {
        await callStore.livekitRoom.localParticipant.publishTrack(audioTrack)
        console.log('[ClientCallView] ✅ 마이크 활성화 완료')

        // 고객 STT 시작
        startCustomerSTT()
      }
    } catch (err) {
      console.error('[ClientCallView] ❌ 마이크 활성화 실패:', err)
      alert('마이크 권한을 허용해주세요')
    }

    // consultationId 수신 (상담원으로부터 DataChannel을 통해 받음)
    callStore.livekitRoom.on(RoomEvent.DataReceived, (payload, participant) => {
      try {
        const text = new TextDecoder().decode(payload)
        const data = JSON.parse(text)

        if (data.type === 'consultationId' && data.consultationId) {
          console.log('[ClientCallView] consultationId 수신:', data.consultationId)
          if (callStore.currentCall) {
            callStore.currentCall.consultationId = data.consultationId
            console.log('[ClientCallView] consultationId 저장 완료:', callStore.currentCall)
          }
        }
      } catch (error) {
        // STT 데이터 등 다른 데이터는 무시
      }
    })

    callStore.livekitRoom.on(RoomEvent.ParticipantDisconnected, (participant) => {
      console.log('[ClientCallView] 상담원이 통화를 종료했습니다:', participant.identity)

      // 폭언 자동 종료 진행 중이면 watcher에서 처리하므로 여기서는 무시
      if (receivedAutoTermination.value || showAutoTerminationModal.value) return

      // 타이머 정리
      if (timerInterval) {
        clearInterval(timerInterval)
      }

      // 마이크 종료 (STT 중지)
      stopCustomerSTT()

      // consultationId를 resetCall 전에 저장
      const consultationId = callStore.currentCall?.consultationId || callStore.currentCall?.registrationId

      // call store 정리
      callStore.setLivekitRoom(null)
      callStore.endCall()
      callStore.resetCall()

      // 통화 종료 페이지로 이동
      router.push({
        name: 'client-call-end',
        query: {
          duration: callDuration.value,
          reason: 'counselor_ended',
          consultationId
        }
      })
    })
  }
})

// 컴포넌트 언마운트 시 정리
onUnmounted(async () => {
  if (timerInterval) {
    clearInterval(timerInterval)
  }

  if (autoRedirectTimer) {
    clearTimeout(autoRedirectTimer)
  }

  // call store의 LiveKit 연결 종료
  if (callStore.livekitRoom) {
    console.log('[ClientCallView] 언마운트 - LiveKit 연결 종료')
    await callStore.livekitRoom.disconnect()
    callStore.setLivekitRoom(null)
  }

  stopCustomerSTT()

  // 자체 LiveKit 연결 종료
  if (isConnected.value) {
    await disconnect()
  }
})
</script>

<style scoped>
.client-call-view {
  min-height: 100vh;
  max-width: 430px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  display: flex;
  flex-direction: column;
  position: relative;
}

/* 상단 대기 순번 */
.queue-info {
  padding: 16px 20px;
  text-align: left;
  font-size: 14px;
  color: #64748b;
}

/* 메인 컨텐츠 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  gap: 24px;
}

/* 통화 시간 */
.call-timer {
  text-align: center;
}

.timer-text {
  font-size: 48px;
  font-weight: 300;
  color: #6366f1;
  font-family: 'SF Mono', Monaco, monospace;
  letter-spacing: 2px;
}

/* 음성 인식 막대 */
.audio-visualizer {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 4px;
  height: 24px;
  margin-top: 12px;
}

.audio-bar {
  width: 4px;
  height: 8px;
  background-color: #cbd5e1;
  border-radius: 2px;
  transition: height 0.1s ease;
}

.audio-bar.active {
  background-color: #6366f1;
  animation: audioWave 0.8s ease-in-out infinite;
}

@keyframes audioWave {
  0%, 100% {
    height: 8px;
  }
  50% {
    height: 20px;
  }
}

.audio-bar:nth-child(1).active { animation-delay: 0s; }
.audio-bar:nth-child(2).active { animation-delay: 0.1s; }
.audio-bar:nth-child(3).active { animation-delay: 0.2s; }
.audio-bar:nth-child(4).active { animation-delay: 0.1s; }
.audio-bar:nth-child(5).active { animation-delay: 0s; }

/* 상담원 아이콘 + 상태 */
.counselor-status {
  position: relative;
  display: inline-block;
}

.counselor-icon {
  width: 80px;
  height: 80px;
  background-color: #e0e7ff;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.headset-icon {
  width: 40px;
  height: 40px;
  color: #6366f1;
}

.status-indicator {
  position: absolute;
  bottom: -4px;
  right: -4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 3px solid #f8fafc;
}

.status-indicator.online {
  background-color: #22c55e;
}

.status-indicator.connecting {
  background-color: #eab308;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 상태 텍스트 */
.status-text {
  text-align: center;
}

.status-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.status-description {
  font-size: 14px;
  color: #64748b;
}

/* 통화 컨트롤 버튼 */
.call-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  margin-top: 32px;
}

.control-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  transition: transform 0.2s ease;
}

.control-btn:active {
  transform: scale(0.95);
}

.control-icon-wrapper {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f1f5f9;
  border-radius: 50%;
  color: #475569;
  transition: all 0.2s ease;
}

.control-btn:hover .control-icon-wrapper {
  background-color: #e2e8f0;
}

.control-btn.active .control-icon-wrapper {
  background-color: #dbeafe;
  color: #3b82f6;
}

.control-icon-wrapper.end {
  background-color: #ef4444;
  color: white;
}

.control-btn:hover .control-icon-wrapper.end {
  background-color: #dc2626;
}

/* 모달 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: 24px;
  max-width: 320px;
  width: 100%;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12px;
}

.modal-message {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 24px;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.modal-btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

.modal-btn.cancel {
  background-color: #f1f5f9;
  color: #475569;
}

.modal-btn.cancel:hover {
  background-color: #e2e8f0;
}

.modal-btn.confirm {
  background-color: #ef4444;
  color: white;
}

.modal-btn.confirm:hover {
  background-color: #dc2626;
}

/* 자동 종료 모달 */
.modal-content.auto-term {
  max-width: 360px;
  text-align: center;
}

.icon-container.warning {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: #fef2f2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-container.warning svg {
  width: 32px;
  height: 32px;
  color: #dc2626;
}

.modal-message.center {
  text-align: center;
  line-height: 1.6;
}

.modal-btn.confirm-auto {
  width: 100%;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: white;
  padding: 14px 24px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  transition: all 0.2s ease;
}

.modal-btn.confirm-auto:hover {
  background: linear-gradient(135deg, #4f46e5 0%, #4338ca 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
}

.modal-btn.confirm-auto:active {
  transform: scale(0.98);
}

.modal-content.auto-term .modal-actions {
  justify-content: center;
  margin-top: 8px;
}

</style>
