import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { PROFANITY_AUTO_TERMINATION_THRESHOLD } from '@/constants/call'

export const useCallStore = defineStore('call', () => {
  // 현재 통화 정보
  const currentCall = ref({
    id: null,
    consultationId: null, // 상담 ID (POST /api/v1/consultations 응답)
    registrationId: null, // 접수 ID (POST /api/v1/registrations 응답)
    customerId: null,
    roomToken: null,
    serverUrl: null,
    startTime: null,
    status: 'idle', // idle, connecting, active, ended
    profanityCount: 0
  })

  // 통화 세션
  const livekitRoom = ref(null)
  const audioTrack = ref(null)
  const connectionError = ref(null)

  // 실시간 STT 텍스트
  const transcripts = ref([])

  // 통화 메모
  const callMemo = ref('')

  // 통화 중 여부
  const isInCall = computed(() => {
    return currentCall.value.status === 'active'
  })

  // 통화 연결 시작 (connecting 상태)
  const initiateCall = (callData) => {
    currentCall.value = {
      ...callData,
      startTime: null,
      status: 'connecting',
      profanityCount: 0
    }
    connectionError.value = null
    transcripts.value = []
    callMemo.value = ''
  }

  // 통화 연결 완료 (active 상태)
  const activateCall = () => {
    currentCall.value.startTime = new Date()
    currentCall.value.status = 'active'
  }

  // 연결 실패
  const setConnectionError = (error) => {
    connectionError.value = error
    currentCall.value.status = 'idle'
  }

  // 통화 시작 (레거시 - 기존 코드 호환용)
  const startCall = (callData) => {
    currentCall.value = {
      ...callData,
      startTime: new Date(),
      status: 'active',
      profanityCount: 0
    }
    connectionError.value = null
    transcripts.value = []
    callMemo.value = ''
  }

  // 통화 종료
  const endCall = () => {
    const duration = currentCall.value.startTime
      ? Math.floor((new Date() - currentCall.value.startTime) / 1000)
      : 0

    currentCall.value.status = 'ended'

    return {
      callId: currentCall.value.id,
      duration,
      transcripts: transcripts.value,
      memo: callMemo.value,
      profanityCount: currentCall.value.profanityCount
    }
  }

  // 통화 초기화
  const resetCall = () => {
    currentCall.value = {
      id: null,
      consultationId: null,
      registrationId: null,
      customerId: null,
      roomToken: null,
      serverUrl: null,
      startTime: null,
      status: 'idle',
      profanityCount: 0
    }
    transcripts.value = []
    callMemo.value = ''
    livekitRoom.value = null
    audioTrack.value = null
    connectionError.value = null
    autoTerminationTriggered.value = false
  }

  // consultationId 설정 (상담원으로부터 수신 시)
  const setConsultationId = (id) => {
    currentCall.value.consultationId = id
    console.log('[CallStore] consultationId 설정:', id)
  }

  // 연결 중 여부
  const isConnecting = computed(() => {
    return currentCall.value.status === 'connecting'
  })

  // 현재 consultationId getter
  const currentConsultationId = computed(() => {
    return currentCall.value.consultationId
  })

  // STT 텍스트 추가
  const addTranscript = (transcript) => {
    transcripts.value.push({
      ...transcript,
      timestamp: new Date().toISOString()
    })
  }

  // 자동 종료 트리거 상태
  const autoTerminationTriggered = ref(false)

  // 욕설 카운트 증가
  const incrementProfanityCount = () => {
    currentCall.value.profanityCount++

    // 임계값 도달 시 자동 종료 트리거
    if (currentCall.value.profanityCount >= PROFANITY_AUTO_TERMINATION_THRESHOLD && !autoTerminationTriggered.value) {
      autoTerminationTriggered.value = true
    }

    return currentCall.value.profanityCount
  }

  // 욕설 카운트 감소 (취소)
  const decrementProfanityCount = () => {
    if (currentCall.value.profanityCount > 0) {
      currentCall.value.profanityCount--
    }
  }

  // 메모 업데이트
  const updateMemo = (memo) => {
    callMemo.value = memo
  }

  // LiveKit 룸 설정
  const setLivekitRoom = (room) => {
    livekitRoom.value = room
  }

  // 오디오 트랙 설정
  const setAudioTrack = (track) => {
    audioTrack.value = track
  }

  return {
    // State
    currentCall,
    livekitRoom,
    audioTrack,
    transcripts,
    callMemo,
    connectionError,
    autoTerminationTriggered,
    // Getters
    isInCall,
    isConnecting,
    currentConsultationId,
    // Actions
    initiateCall,
    activateCall,
    setConnectionError,
    startCall,
    endCall,
    resetCall,
    setConsultationId,
    addTranscript,
    incrementProfanityCount,
    decrementProfanityCount,
    updateMemo,
    setLivekitRoom,
    setAudioTrack
  }
})
