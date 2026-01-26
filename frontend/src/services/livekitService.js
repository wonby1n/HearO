/**
 * LiveKit 서비스
 * WebRTC 통화 연결 및 오디오 스트림 관리
 */
import { Room, RoomEvent } from 'livekit-client'

class LiveKitService {
  constructor() {
    this.room = null
    this.localAudioTrack = null
    this.remoteAudioCallbacks = []
  }

  /**
   * LiveKit Room에 연결
   * @param {string} token - LiveKit 토큰
   * @param {string} url - LiveKit 서버 URL
   * @returns {Promise<Room>}
   */
  async connect(token, url) {
    try {
      // 기존 연결 정리
      await this.disconnect()

      this.room = new Room()
      this._setupRoomEventHandlers()

      await this.room.connect(url, token)
      console.log('LiveKit connected:', this.room.name)

      return this.room
    } catch (error) {
      console.error('LiveKit connection failed:', error)
      throw error
    }
  }

  /**
   * Room 이벤트 핸들러 설정
   * @private
   */
  _setupRoomEventHandlers() {
    if (!this.room) return

    // 원격 오디오 트랙 구독
    this.room.on(RoomEvent.TrackSubscribed, (track, publication, participant) => {
      if (track.kind !== 'audio') return

      console.log(`Remote audio subscribed: ${participant.identity}`)

      // 콜백 실행
      this.remoteAudioCallbacks.forEach(callback => {
        callback({
          track,
          participant,
          publication
        })
      })
    })

    // 원격 오디오 트랙 구독 해제
    this.room.on(RoomEvent.TrackUnsubscribed, (track, publication, participant) => {
      if (track.kind !== 'audio') return
      console.log(`Remote audio unsubscribed: ${participant.identity}`)
    })

    // 참가자 연결
    this.room.on(RoomEvent.ParticipantConnected, (participant) => {
      console.log(`Participant connected: ${participant.identity}`)
    })

    // 참가자 연결 해제
    this.room.on(RoomEvent.ParticipantDisconnected, (participant) => {
      console.log(`Participant disconnected: ${participant.identity}`)
    })

    // Room 연결 해제
    this.room.on(RoomEvent.Disconnected, () => {
      console.log('LiveKit disconnected')
    })
  }

  /**
   * 원격 오디오 트랙 구독 콜백 등록
   * @param {Function} callback - ({ track, participant, publication }) => void
   */
  onRemoteAudioTrack(callback) {
    this.remoteAudioCallbacks.push(callback)
  }

  /**
   * 로컬 마이크 발행
   * @returns {Promise<void>}
   */
  async publishLocalAudio() {
    // TODO: TestRTC.vue의 createLocalAudioTrack 참고하여 구현
    // const { createLocalAudioTrack } = require('livekit-client')
    // const track = await createLocalAudioTrack()
    // await this.room.localParticipant.publishTrack(track)
    // this.localAudioTrack = track
    console.warn('publishLocalAudio() not implemented yet')
  }

  /**
   * 로컬 마이크 중지
   * @returns {Promise<void>}
   */
  async unpublishLocalAudio() {
    // TODO: 구현
    console.warn('unpublishLocalAudio() not implemented yet')
  }

  /**
   * Room 연결 해제
   * @returns {Promise<void>}
   */
  async disconnect() {
    if (this.localAudioTrack) {
      this.localAudioTrack.stop()
      this.localAudioTrack = null
    }

    if (this.room) {
      await this.room.disconnect()
      this.room = null
    }

    this.remoteAudioCallbacks = []
  }

  /**
   * 현재 Room 반환
   * @returns {Room|null}
   */
  getRoom() {
    return this.room
  }

  /**
   * 연결 상태 확인
   * @returns {boolean}
   */
  isConnected() {
    return this.room?.state === 'connected'
  }
}

// Singleton 인스턴스 export
export const livekitService = new LiveKitService()
