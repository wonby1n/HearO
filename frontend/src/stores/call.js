import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCallStore = defineStore('call', () => {
  // 현재 통화 정보
  const currentCall = ref({
    id: null,
    customerId: null,
    roomToken: null,
    startTime: null,
    status: 'idle', // idle, connecting, active, ended
    profanityCount: 0
  })

  // 통화 세션
  const livekitRoom = ref(null)
  const audioTrack = ref(null)

  // 실시간 STT 텍스트
  const transcripts = ref([])

  // 통화 메모
  const callMemo = ref('')

  // 통화 중 여부
  const isInCall = computed(() => {
    return currentCall.value.status === 'active'
  })

  // 통화 시작
  const startCall = (callData) => {
    currentCall.value = {
      ...callData,
      startTime: new Date(),
      status: 'active',
      profanityCount: 0
    }
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
      customerId: null,
      roomToken: null,
      startTime: null,
      status: 'idle',
      profanityCount: 0
    }
    transcripts.value = []
    callMemo.value = ''
    livekitRoom.value = null
    audioTrack.value = null
  }

  // STT 텍스트 추가
  const addTranscript = (transcript) => {
    transcripts.value.push({
      ...transcript,
      timestamp: new Date().toISOString()
    })
  }

  // 욕설 카운트 증가
  const incrementProfanityCount = () => {
    currentCall.value.profanityCount++
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
    // Getters
    isInCall,
    // Actions
    startCall,
    endCall,
    resetCall,
    addTranscript,
    incrementProfanityCount,
    decrementProfanityCount,
    updateMemo,
    setLivekitRoom,
    setAudioTrack
  }
})
