import { ref, readonly, onUnmounted } from 'vue'
import {
  Room,
  RoomEvent,
  Track,
  ConnectionState,
  DisconnectReason
} from 'livekit-client'

/**
 * LiveKit 룸 연결 및 관리를 위한 Composable
 * @param {Object} options - 옵션
 * @param {Function} options.onTrackSubscribed - 트랙 구독 시 콜백
 * @param {Function} options.onTrackUnsubscribed - 트랙 구독 해제 시 콜백
 * @param {Function} options.onDisconnected - 연결 해제 시 콜백
 * @param {Function} options.onError - 에러 발생 시 콜백
 */
export function useLiveKit(options = {}) {
  // 상태
  const room = ref(null)
  const connectionState = ref(ConnectionState.Disconnected)
  const localAudioTrack = ref(null)
  const remoteAudioTracks = ref([])
  const participants = ref([])
  const error = ref(null)
  const isMuted = ref(false)

  // 연결 상태 computed
  const isConnected = ref(false)
  const isConnecting = ref(false)

  /**
   * LiveKit 룸에 연결
   * @param {string} serverUrl - LiveKit 서버 URL (예: wss://your-server.livekit.cloud)
   * @param {string} token - 인증 토큰
   * @param {Object} roomOptions - 룸 옵션
   */
  const connect = async (serverUrl, token, roomOptions = {}) => {
    try {
      error.value = null
      isConnecting.value = true
      connectionState.value = ConnectionState.Connecting

      // 기존 룸이 있으면 정리
      if (room.value) {
        await disconnect()
      }

      // 새 룸 인스턴스 생성
      const newRoom = new Room({
        adaptiveStream: true,
        dynacast: true,
        ...roomOptions
      })

      // 이벤트 리스너 등록
      setupRoomEventListeners(newRoom)

      // 룸에 연결
      await newRoom.connect(serverUrl, token)

      room.value = newRoom
      isConnected.value = true
      isConnecting.value = false
      connectionState.value = ConnectionState.Connected

      console.log('[LiveKit] 룸 연결 성공:', newRoom.name)

      return newRoom
    } catch (err) {
      error.value = err
      isConnecting.value = false
      connectionState.value = ConnectionState.Disconnected
      console.error('[LiveKit] 연결 실패:', err)
      options.onError?.(err)
      throw err
    }
  }

  /**
   * 룸 이벤트 리스너 설정
   */
  const setupRoomEventListeners = (roomInstance) => {
    // 연결 상태 변경
    roomInstance.on(RoomEvent.ConnectionStateChanged, (state) => {
      connectionState.value = state
      isConnected.value = state === ConnectionState.Connected
      console.log('[LiveKit] 연결 상태 변경:', state)
    })

    // 트랙 구독됨 (원격 참가자의 오디오/비디오)
    roomInstance.on(RoomEvent.TrackSubscribed, (track, publication, participant) => {
      console.log('[LiveKit] 트랙 구독:', track.kind, participant.identity)

      if (track.kind === Track.Kind.Audio) {
        remoteAudioTracks.value.push({
          track,
          participant: participant.identity,
          publication
        })

        // 오디오 요소에 연결
        const audioElement = track.attach()
        audioElement.id = `audio-${participant.identity}`
        document.body.appendChild(audioElement)
      }

      options.onTrackSubscribed?.(track, publication, participant)
    })

    // 트랙 구독 해제
    roomInstance.on(RoomEvent.TrackUnsubscribed, (track, publication, participant) => {
      console.log('[LiveKit] 트랙 구독 해제:', track.kind, participant.identity)

      if (track.kind === Track.Kind.Audio) {
        remoteAudioTracks.value = remoteAudioTracks.value.filter(
          t => t.participant !== participant.identity
        )

        // 오디오 요소 제거
        track.detach().forEach(el => el.remove())
      }

      options.onTrackUnsubscribed?.(track, publication, participant)
    })

    // 참가자 연결
    roomInstance.on(RoomEvent.ParticipantConnected, (participant) => {
      console.log('[LiveKit] 참가자 연결:', participant.identity)
      updateParticipants(roomInstance)
    })

    // 참가자 연결 해제
    roomInstance.on(RoomEvent.ParticipantDisconnected, (participant) => {
      console.log('[LiveKit] 참가자 연결 해제:', participant.identity)
      updateParticipants(roomInstance)
    })

    // 연결 해제
    roomInstance.on(RoomEvent.Disconnected, (reason) => {
      console.log('[LiveKit] 연결 해제:', reason)
      isConnected.value = false
      connectionState.value = ConnectionState.Disconnected

      // 리소스 정리
      cleanupResources()

      options.onDisconnected?.(reason)
    })

    // 재연결 시도
    roomInstance.on(RoomEvent.Reconnecting, () => {
      console.log('[LiveKit] 재연결 시도 중...')
      connectionState.value = ConnectionState.Reconnecting
    })

    // 재연결 성공
    roomInstance.on(RoomEvent.Reconnected, () => {
      console.log('[LiveKit] 재연결 성공')
      connectionState.value = ConnectionState.Connected
    })

    // 오디오 재생 상태 변경
    roomInstance.on(RoomEvent.AudioPlaybackStatusChanged, () => {
      if (!roomInstance.canPlaybackAudio) {
        console.warn('[LiveKit] 오디오 재생 차단됨 - 사용자 상호작용 필요')
      }
    })
  }

  /**
   * 참가자 목록 업데이트
   */
  const updateParticipants = (roomInstance) => {
    if (!roomInstance) return

    participants.value = Array.from(roomInstance.remoteParticipants.values()).map(p => ({
      identity: p.identity,
      name: p.name,
      isSpeaking: p.isSpeaking,
      connectionQuality: p.connectionQuality
    }))
  }

  /**
   * 마이크 활성화 및 오디오 트랙 발행
   */
  const enableMicrophone = async () => {
    if (!room.value) {
      throw new Error('룸에 연결되어 있지 않습니다')
    }

    try {
      await room.value.localParticipant.setMicrophoneEnabled(true)

      const micTrack = room.value.localParticipant.getTrackPublication(Track.Source.Microphone)
      if (micTrack?.track) {
        localAudioTrack.value = micTrack.track
      }

      isMuted.value = false
      console.log('[LiveKit] 마이크 활성화')

      return localAudioTrack.value
    } catch (err) {
      console.error('[LiveKit] 마이크 활성화 실패:', err)
      error.value = err
      throw err
    }
  }

  /**
   * 마이크 비활성화
   */
  const disableMicrophone = async () => {
    if (!room.value) return

    try {
      await room.value.localParticipant.setMicrophoneEnabled(false)
      localAudioTrack.value = null
      console.log('[LiveKit] 마이크 비활성화')
    } catch (err) {
      console.error('[LiveKit] 마이크 비활성화 실패:', err)
      error.value = err
    }
  }

  /**
   * 음소거 토글
   */
  const toggleMute = async () => {
    if (!room.value) return

    const newMuteState = !isMuted.value

    try {
      await room.value.localParticipant.setMicrophoneEnabled(!newMuteState)
      isMuted.value = newMuteState
      console.log('[LiveKit] 음소거:', newMuteState)
      return newMuteState
    } catch (err) {
      console.error('[LiveKit] 음소거 토글 실패:', err)
      error.value = err
      throw err
    }
  }

  /**
   * 음소거 설정
   */
  const setMuted = async (muted) => {
    if (!room.value) return

    try {
      await room.value.localParticipant.setMicrophoneEnabled(!muted)
      isMuted.value = muted
      console.log('[LiveKit] 음소거 설정:', muted)
    } catch (err) {
      console.error('[LiveKit] 음소거 설정 실패:', err)
      error.value = err
      throw err
    }
  }

  /**
   * 리소스 정리
   */
  const cleanupResources = () => {
    // 원격 오디오 트랙 정리
    remoteAudioTracks.value.forEach(({ track }) => {
      track.detach().forEach(el => el.remove())
    })
    remoteAudioTracks.value = []

    // 로컬 트랙 정리
    localAudioTrack.value = null

    // 참가자 목록 초기화
    participants.value = []
  }

  /**
   * 룸 연결 해제
   */
  const disconnect = async () => {
    if (!room.value) return

    try {
      cleanupResources()
      await room.value.disconnect()
      room.value = null
      isConnected.value = false
      connectionState.value = ConnectionState.Disconnected
      console.log('[LiveKit] 룸 연결 해제')
    } catch (err) {
      console.error('[LiveKit] 연결 해제 실패:', err)
      error.value = err
    }
  }

  /**
   * 오디오 재생 시작 (브라우저 정책으로 차단된 경우)
   */
  const startAudioPlayback = async () => {
    if (room.value && !room.value.canPlaybackAudio) {
      await room.value.startAudio()
      console.log('[LiveKit] 오디오 재생 시작')
    }
  }

  // 컴포넌트 언마운트 시 정리
  onUnmounted(() => {
    disconnect()
  })

  return {
    // 상태 (readonly)
    room: readonly(room),
    connectionState: readonly(connectionState),
    localAudioTrack: readonly(localAudioTrack),
    remoteAudioTracks: readonly(remoteAudioTracks),
    participants: readonly(participants),
    error: readonly(error),
    isMuted: readonly(isMuted),
    isConnected: readonly(isConnected),
    isConnecting: readonly(isConnecting),

    // 메서드
    connect,
    disconnect,
    enableMicrophone,
    disableMicrophone,
    toggleMute,
    setMuted,
    startAudioPlayback
  }
}
