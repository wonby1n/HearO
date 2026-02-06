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
 * @param {Room} options.externalRoom - 외부에서 주입하는 Room 인스턴스 (이미 연결된 room 재사용)
 * @param {Function} options.onTrackSubscribed - 트랙 구독 시 콜백
 * @param {Function} options.onTrackUnsubscribed - 트랙 구독 해제 시 콜백
 * @param {Function} options.onDisconnected - 연결 해제 시 콜백
 * @param {Function} options.onParticipantDisconnected - 참가자 연결 해제 시 콜백
 * @param {Function} options.onError - 에러 발생 시 콜백
 */
export function useLiveKit(options = {}) {
  // 외부 room이 주입되면 그것을 사용, 아니면 새로 생성
  const room = ref(options.externalRoom || null)
  const connectionState = ref(options.externalRoom ? ConnectionState.Connected : ConnectionState.Disconnected)
  const localAudioTrack = ref(null)
  const remoteAudioTracks = ref([])
  const participants = ref([])
  const error = ref(null)
  const isMuted = ref(false)

  // 기본 동작: 원격 오디오 자동 attach
  // 상담원 측에서 딜레이/필터 파이프라인을 구성하려면 false로 두고,
  // options.onTrackSubscribed에서 별도 처리하면 됨.
  const autoAttachRemoteAudio = options.autoAttachRemoteAudio ?? true

  // 연결 상태 (외부 room이 주입되면 이미 연결된 상태)
  const isConnected = ref(options.externalRoom ? true : false)
  const isConnecting = ref(false)

  /**
   * LiveKit 룸에 연결
   * @param {string} serverUrl - LiveKit 서버 URL (예: wss://your-server.livekit.cloud)
   * @param {string} token - 인증 토큰
   * @param {Object} roomOptions - 룸 옵션
   */
  const connect = async (serverUrl, token, roomOptions = {}) => {
    // 중복 연결 방지: 이미 연결 중이면 무시
    if (isConnecting.value || isConnected.value) {
      console.warn('[LiveKit] 이미 연결 중이므로 중복 연결 시도 무시')
      return room.value
    }

    try {
      error.value = null
      isConnecting.value = true
      connectionState.value = ConnectionState.Connecting

      // 기존 룸이 있으면 정리 (Disconnected 상태일 때만)
      if (room.value && connectionState.value === ConnectionState.Disconnected) {
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

        // 오디오 요소 자동 attach (기본)
        if (autoAttachRemoteAudio) {
          const audioElement = track.attach()
          audioElement.id = `audio-${participant.identity}`
          document.body.appendChild(audioElement)
        }
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

        // 오디오 요소 제거 (auto attach를 켠 경우에만)
        if (autoAttachRemoteAudio) {
          track.detach().forEach(el => el.remove())
        }
      }

      options.onTrackUnsubscribed?.(track, publication, participant)
    })

    // 참가자 연결
    roomInstance.on(RoomEvent.ParticipantConnected, (participant) => {
      console.log('[LiveKit] 참가자 연결:', participant.identity)
      updateParticipants(roomInstance)

      // 외부 콜백 호출
      options.onParticipantConnected?.(participant)
    })

    // 참가자 연결 해제
    roomInstance.on(RoomEvent.ParticipantDisconnected, (participant) => {
      console.log('[LiveKit] 참가자 연결 해제:', participant.identity)
      updateParticipants(roomInstance)

      // 상대방이 나갔을 때 콜백 호출
      options.onParticipantDisconnected?.(participant)
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
      // DataCloneError 회피: 브라우저 API로 직접 MediaStream 가져오기
      console.log('[LiveKit] 마이크 권한 요청 중...')
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })

      console.log('[LiveKit] 마이크 스트림 획득, LiveKit에 publish 중...')
      const audioTracks = stream.getAudioTracks()
      if (audioTracks.length > 0) {
        // LiveKit에 트랙 publish
        const publication = await room.value.localParticipant.publishTrack(audioTracks[0])
        localAudioTrack.value = publication.track
        isMuted.value = false
        console.log('[LiveKit] 마이크 활성화 성공')
        return localAudioTrack.value
      } else {
        throw new Error('오디오 트랙을 찾을 수 없습니다')
      }
    } catch (err) {
      console.error('[LiveKit] 마이크 활성화 실패:', err)
      error.value = err

      // 권한 거부 에러인 경우 사용자에게 알림
      if (err.name === 'NotAllowedError') {
        console.warn('[LiveKit] 마이크 권한이 거부되었습니다. 브라우저 설정에서 마이크 권한을 허용해주세요.')
      }

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
      // 마이크 명시적으로 끄기
      try {
        await room.value.localParticipant.setMicrophoneEnabled(false)
        console.log('[LiveKit] 마이크 비활성화')
      } catch (micErr) {
        console.warn('[LiveKit] 마이크 비활성화 실패 (무시):', micErr)
      }

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
  // 주의: 자동 disconnect를 제거하여 페이지 이동 시에도 연결 유지
  // disconnect는 명시적으로만 호출되어야 함
  onUnmounted(() => {
    console.log('[LiveKit] useLiveKit 언마운트 - 자동 disconnect 생략')
    // disconnect() 제거 - call store가 room을 관리하므로 여기서 끊지 않음
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
