/**
 * 음성 통화 통합 Composable
 * LiveKit + STT + 오디오 스트림 처리를 통합
 */
import { ref, onUnmounted } from 'vue'
import { livekitService } from '@/services/livekitService'
import { sttService } from '@/services/sttService'
import { useAudioStream } from './useAudioStream'

export function useVoiceCall() {
  const isConnected = ref(false)
  const isSTTActive = ref(false)
  const { processAudioStream, cleanup: cleanupAudio } = useAudioStream()

  /**
   * 통화 초기화 및 연결
   * @param {Object} config
   * @param {string} config.livekitToken - LiveKit 토큰
   * @param {string} config.livekitUrl - LiveKit 서버 URL
   * @param {string} config.sttEngine - STT 엔진 ('web-speech')
   * @param {Function} config.onTranscript - 트랜스크립트 콜백
   */
  const initializeCall = async (config) => {
    const {
      livekitToken,
      livekitUrl,
      sttEngine = 'web-speech',
      onTranscript,
      onError
    } = config

    try {
      // 1. LiveKit 연결
      await livekitService.connect(livekitToken, livekitUrl)
      isConnected.value = true

      // 2. STT 초기화
      await sttService.initialize(sttEngine, {
        lang: 'ko-KR',
        continuous: true,
        interimResults: true
      })

      // 3. STT 콜백 등록
      sttService.onTranscript(onTranscript)
      if (onError) {
        sttService.onError(onError)
      }

      // 4. 원격 오디오 트랙 구독 설정
      livekitService.onRemoteAudioTrack(({ track, participant }) => {
        console.log(`Processing audio from: ${participant.identity}`)

        // TODO: 오디오 스트림 처리
        // const mediaTrack = track.mediaStreamTrack
        // processAudioStream(mediaTrack, {
        //   bufferSize: 200,
        //   sampleRate: 16000,
        //   onAudioData: (audioData) => {
        //     // STT 엔진으로 전달
        //   }
        // })
      })

      console.log('Voice call initialized')
    } catch (error) {
      console.error('Failed to initialize call:', error)
      throw error
    }
  }

  /**
   * STT 시작
   */
  const startSTT = async () => {
    try {
      await sttService.start()
      isSTTActive.value = true
      console.log('STT started')
    } catch (error) {
      console.error('Failed to start STT:', error)
      throw error
    }
  }

  /**
   * STT 중지
   */
  const stopSTT = async () => {
    try {
      await sttService.stop()
      isSTTActive.value = false
      console.log('STT stopped')
    } catch (error) {
      console.error('Failed to stop STT:', error)
    }
  }

  /**
   * 통화 종료
   */
  const endCall = async () => {
    try {
      await stopSTT()
      await livekitService.disconnect()
      await cleanupAudio()
      isConnected.value = false
      console.log('Call ended')
    } catch (error) {
      console.error('Failed to end call:', error)
    }
  }

  // 컴포넌트 언마운트 시 정리
  onUnmounted(async () => {
    await endCall()
  })

  return {
    isConnected,
    isSTTActive,
    initializeCall,
    startSTT,
    stopSTT,
    endCall
  }
}
