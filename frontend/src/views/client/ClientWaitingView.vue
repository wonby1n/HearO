<template>
  <div class="client-waiting-view">

    <!-- 메인 컨텐츠 -->
    <div class="main-content">
      <!-- 로딩 스피너 아이콘 -->
      <div class="loading-icon-container">
        <div class="loading-icon">
          <svg class="spinner" viewBox="0 0 50 50">
            <circle class="spinner-path" cx="25" cy="25" r="20" fill="none" stroke="currentColor" stroke-width="4"
              stroke-linecap="round" />
          </svg>
        </div>
      </div>

      <!-- 상태 텍스트 -->
      <div class="status-text">
        <h2 class="status-title">잠시만 기다려주세요</h2>

        <!-- 대기 순번 배지 -->
        <div class="queue-badge">
          <span>대기 순번 {{ queuePosition }}번</span>
        </div>

        <p class="status-description">
          보다 정확한 상담을 위해<br />
          전문 상담사를 배정 중입니다.
        </p>
      </div>

      <!-- 통화 컨트롤 버튼 -->
      <div class="call-controls">
        <!-- 스피커 버튼 -->
        <div class="control-item">
          <button @click="toggleSpeaker" :class="['control-btn', { active: isSpeakerOn }]"
            :aria-label="isSpeakerOn ? '스피커폰 끄기' : '스피커폰 켜기'" title="스피커">
            <div class="control-icon-wrapper">
              <svg v-if="isSpeakerOn" class="control-icon" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd"
                  d="M9.383 3.076A1 1 0 0110 4v12a1 1 0 01-1.707.707L4.586 13H2a1 1 0 01-1-1V8a1 1 0 011-1h2.586l3.707-3.707a1 1 0 011.09-.217zM14.657 2.929a1 1 0 011.414 0A9.972 9.972 0 0119 10a9.972 9.972 0 01-2.929 7.071 1 1 0 01-1.414-1.414A7.971 7.971 0 0017 10c0-2.21-.894-4.208-2.343-5.657a1 1 0 010-1.414zm-2.829 2.828a1 1 0 011.415 0A5.983 5.983 0 0115 10a5.984 5.984 0 01-1.757 4.243 1 1 0 01-1.415-1.415A3.984 3.984 0 0013 10a3.983 3.983 0 00-1.172-2.828 1 1 0 010-1.415z"
                  clip-rule="evenodd" />
              </svg>
              <svg v-else class="control-icon" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd"
                  d="M9.383 3.076A1 1 0 0110 4v12a1 1 0 01-1.707.707L4.586 13H2a1 1 0 01-1-1V8a1 1 0 011-1h2.586l3.707-3.707a1 1 0 011.09-.217zM12.293 7.293a1 1 0 011.414 0L15 8.586l1.293-1.293a1 1 0 111.414 1.414L16.414 10l1.293 1.293a1 1 0 01-1.414 1.414L15 11.414l-1.293 1.293a1 1 0 01-1.414-1.414L13.586 10l-1.293-1.293a1 1 0 010-1.414z"
                  clip-rule="evenodd" />
              </svg>
            </div>
          </button>
          <span class="control-label">SPEAKER</span>
        </div>

        <!-- 통화 종료 버튼 -->
        <div class="control-item">
          <button @click="handleEndCall" class="control-btn end-call" aria-label="대기 취소" title="통화 종료">
            <div class="control-icon-wrapper end">
              <svg class="control-icon" fill="currentColor" viewBox="0 0 20 20">
                <path
                  d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z" />
                <path
                  d="M16.707 3.293a1 1 0 010 1.414L15.414 6l1.293 1.293a1 1 0 01-1.414 1.414L14 7.414l-1.293 1.293a1 1 0 11-1.414-1.414L12.586 6l-1.293-1.293a1 1 0 011.414-1.414L14 4.586l1.293-1.293a1 1 0 011.414 0z" />
              </svg>
            </div>
          </button>
          <span class="control-label end-label">END CALL</span>
        </div>

        <!-- 음소거 버튼 -->
        <div class="control-item">
          <button @click="toggleMute" :class="['control-btn', { active: isMuted }]"
            :aria-label="isMuted ? '음소거 해제' : '음소거'" title="음소거">
            <div class="control-icon-wrapper">
              <svg v-if="!isMuted" class="control-icon" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd"
                  d="M7 4a3 3 0 016 0v4a3 3 0 11-6 0V4zm4 10.93A7.001 7.001 0 0017 8a1 1 0 10-2 0A5 5 0 015 8a1 1 0 00-2 0 7.001 7.001 0 006 6.93V17H6a1 1 0 100 2h8a1 1 0 100-2h-3v-2.07z"
                  clip-rule="evenodd" />
              </svg>
              <svg v-else class="control-icon" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd"
                  d="M7 4a3 3 0 016 0v4a3 3 0 11-6 0V4zm4 10.93A7.001 7.001 0 0017 8a1 1 0 10-2 0A5 5 0 015 8a1 1 0 00-2 0 7.001 7.001 0 006 6.93V17H6a1 1 0 100 2h8a1 1 0 100-2h-3v-2.07z"
                  clip-rule="evenodd" />
                <path d="M3.293 3.293a1 1 0 011.414 0l12 12a1 1 0 01-1.414 1.414l-12-12a1 1 0 010-1.414z" />
              </svg>
            </div>
          </button>
          <span class="control-label">MUTE</span>
        </div>
      </div>
    </div>

    <!-- 통화 종료 확인 모달 -->
    <Teleport to="body">
      <div v-if="showConfirmModal" class="modal-overlay" @click.self="closeConfirmModal">
        <div class="modal-content">
          <h3 class="modal-title">대기 취소</h3>
          <p class="modal-message">상담 대기를 취소하시겠습니까?</p>
          <div class="modal-actions">
            <button @click="closeConfirmModal" class="modal-btn cancel">취소</button>
            <button @click="confirmEndCall" class="modal-btn confirm">확인</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCallConnection } from '@/composables/useCallConnection'
import { useCallStore } from '@/stores/call'
import { useCustomerStore } from '@/stores/customer'
import ARSvoiceFile from '@/assets/ARSvoice.mp3'

const router = useRouter()
const callStore = useCallStore()
const customerStore = useCustomerStore()
const { connectionState, startWaiting } = useCallConnection('customer')

// 상태 관리
const queuePosition = ref(customerStore.queueInfo.position || 0) // 대기 순번 (추후 백엔드 연동)
const isMuted = ref(false)
const isSpeakerOn = ref(true)
const showConfirmModal = ref(false)
const arsAudio = ref(null)
const isARSPlaying = ref(false)
const queueSocket = ref(null)

let queuePollingInterval = null
let queueSocketReconnectTimeout = null
let shouldReconnect = true
const QUEUE_SOCKET_RECONNECT_DELAY = 3000

const getQueueSocketUrl = () => {
  const configuredBase = import.meta.env.VITE_WS_BASE_URL
  let baseUrl = configuredBase?.replace(/\/$/, '')

  if (baseUrl) {
    if (baseUrl.startsWith('http://')) {
      baseUrl = `ws://${baseUrl.slice(7)}`
    } else if (baseUrl.startsWith('https://')) {
      baseUrl = `wss://${baseUrl.slice(8)}`
    }
  } else {
    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
    baseUrl = `${protocol}://${window.location.host}`
  }

  return `${baseUrl}/api/v1/consultations/wait`
}

const startQueuePolling = () => {
  if (queuePollingInterval) return
  queuePollingInterval = setInterval(fetchQueuePosition, 5000)
}

const stopQueuePolling = () => {
  if (queuePollingInterval) {
    clearInterval(queuePollingInterval)
    queuePollingInterval = null
  }
}

const parseQueueMessage = (rawData) => {
  try {
    return JSON.parse(rawData)
  } catch (error) {
    console.error('[QueueWS] message parse failed:', error)
    return null
  }
}

const handleQueueStatusMessage = (payload) => {
  if (!payload) return
  if (payload.type && payload.type !== 'queue_status') return

  if (typeof payload.queue_position === 'number') {
    const isWaiting = payload.status ? payload.status === 'waiting' : true
    updateQueuePosition(payload.queue_position, isWaiting)
  } else if (typeof payload.status === 'string') {
    customerStore.updateQueueInfo({ isWaiting: payload.status === 'waiting' })
  }
}

const scheduleQueueSocketReconnect = () => {
  if (!shouldReconnect || queueSocketReconnectTimeout) return
  queueSocketReconnectTimeout = setTimeout(() => {
    queueSocketReconnectTimeout = null
    connectQueueSocket()
  }, QUEUE_SOCKET_RECONNECT_DELAY)
}

const clearQueueSocketReconnect = () => {
  if (queueSocketReconnectTimeout) {
    clearTimeout(queueSocketReconnectTimeout)
    queueSocketReconnectTimeout = null
  }
}

const connectQueueSocket = () => {
  const customerId = customerStore.currentCustomer?.id
  if (!customerId) {
    console.warn('[QueueWS] customerId missing - skip WebSocket connect')
    return
  }

  const socketUrl = getQueueSocketUrl()

  if (queueSocket.value) {
    queueSocket.value.close()
    queueSocket.value = null
  }

  shouldReconnect = true
  clearQueueSocketReconnect()

  const socket = new WebSocket(socketUrl)
  queueSocket.value = socket

  socket.addEventListener('open', () => {
    stopQueuePolling()
    clearQueueSocketReconnect()
  })

  socket.addEventListener('message', (event) => {
    handleQueueStatusMessage(parseQueueMessage(event.data))
  })

  socket.addEventListener('close', () => {
    queueSocket.value = null
    if (shouldReconnect) {
      startQueuePolling()
      scheduleQueueSocketReconnect()
    }
  })

  socket.addEventListener('error', (error) => {
    console.error('[QueueWS] socket error:', error)
  })
}

const disconnectQueueSocket = () => {
  shouldReconnect = false
  clearQueueSocketReconnect()

  if (queueSocket.value) {
    queueSocket.value.close()
    queueSocket.value = null
  }
}
// Queue position fetch (TODO: backend integration)
const fetchQueuePosition = async () => {
  try {
    // TODO: 백엔드 API 연동
    // const response = await api.get('/queue/position', {
    //   params: { customerId: customerStore.currentCustomer?.id }
    // })
    // queuePosition.value = response.data.position

    // 테스트용: 랜덤하게 순번 감소 시뮬레이션
    if (queuePosition.value > 1) {
      // 실제 환경에서는 백엔드에서 받아온 값으로 대체
      // queuePosition.value = response.data.position
    }
  } catch (error) {
    console.error('[Client] 대기열 조회 실패:', error)
  }
}

// 대기열 순번 업데이트 (외부에서 호출 가능)
const updateQueuePosition = (newPosition, isWaiting = true) => {
  if (Number.isFinite(newPosition) && newPosition >= 0) {
    queuePosition.value = newPosition
    customerStore.updateQueueInfo({ position: newPosition, isWaiting })
  }
}

// 스피커 토글
const toggleSpeaker = () => {
  isSpeakerOn.value = !isSpeakerOn.value
  console.log('[Client] 스피커 상태:', isSpeakerOn.value)
}

// 음소거 토글
const toggleMute = () => {
  isMuted.value = !isMuted.value
  console.log('[Client] 음소거 상태:', isMuted.value)
}

// 통화 종료 버튼 클릭
const handleEndCall = () => {
  showConfirmModal.value = true
}

// 모달 닫기
const closeConfirmModal = () => {
  showConfirmModal.value = false
}

// 대기 취소 확인
const confirmEndCall = async () => {
  showConfirmModal.value = false

  stopQueuePolling()
  disconnectQueueSocket()

  // ARS 음성 정리
  if (arsAudio.value) {
    arsAudio.value.pause()
    arsAudio.value.removeEventListener('ended', onARSAudioEnded)
    arsAudio.value = null
  }

  // TODO: 백엔드에 대기 취소 요청

  callStore.endCall()
  router.push('/client')
}

// ARS 음성 재생
const playARSAudio = () => {
  // 기존 오디오 정리
  if (arsAudio.value) {
    arsAudio.value.pause()
    arsAudio.value.currentTime = 0
    arsAudio.value.removeEventListener('ended', onARSAudioEnded)
  }

  // 새 Audio 객체 생성
  arsAudio.value = new Audio(ARSvoiceFile)
  isARSPlaying.value = true
  console.log('[Client] ARS 음성 재생 시작')

  arsAudio.value.play()
    .then(() => {
      console.log('[Client] ARS 음성 재생 성공')
    })
    .catch((error) => {
      console.error('[Client] ARS 음성 재생 실패:', error)
      isARSPlaying.value = false
    })

  // 음성 재생 완료 이벤트
  arsAudio.value.addEventListener('ended', onARSAudioEnded)
}

// ARS 음성 재생 완료 처리
const onARSAudioEnded = async () => {
  console.log('[Client] ARS 음성 재생 완료')
  isARSPlaying.value = false

  // TODO: 상담원에게 통화 알림 전송
  try {
    // 백엔드 API 호출하여 상담원에게 알림
    // const response = await fetch('/api/v1/calls/notify-agent', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({
    //     customerId: customerStore.currentCustomer?.id
    //   })
    // })
    // if (!response.ok) throw new Error('상담원 알림 실패')

    console.log('[Client] 상담원에게 통화 알림 전송 완료 (대기 중)')
  } catch (error) {
    console.error('[Client] 상담원 알림 전송 실패:', error)
  }
}

// 상담원 연결 시 호출될 함수
const onAgentConnected = () => {
  // ARS 음성 정리
  if (arsAudio.value) {
    arsAudio.value.pause()
    arsAudio.value.removeEventListener('ended', onARSAudioEnded)
  }

  // 상담 화면으로 이동
  router.push('/client/call')
}
onMounted(() => {
  const customerId = customerStore.currentCustomer?.id
  if (customerId) {
    startWaiting(customerId)
  }
})
// 컴포넌트 마운트 시 초기화
onMounted(async () => {
  // ARS 음성 자동 재생
  playARSAudio()

  // 고객 ID 확인
  const customerId = customerStore.currentCustomer?.id
  if (!customerId) {
    console.error('[ClientWaiting] 고객 ID가 없습니다')
    return
  }

  // STOMP 기반 매칭 알림 시작 (LiveKit 자동 연결 포함)
  startWaiting(customerId)

  // 초기 대기 순번 조회 (순번 표시용)
  await fetchQueuePosition()

  // 주기적으로 대기 순번 업데이트 (5초마다) - 순번 표시용
  startQueuePolling()
})

// 컴포넌트 언마운트 시 정리
onUnmounted(() => {
  stopQueuePolling()
  disconnectQueueSocket()

  // ARS 오디오 정리
  if (arsAudio.value) {
    arsAudio.value.pause()
    arsAudio.value.removeEventListener('ended', onARSAudioEnded)
    arsAudio.value = null
  }

})

// 외부에서 사용할 수 있도록 노출
defineExpose({
  updateQueuePosition,
  queuePosition
})
</script>

<style scoped>
.client-waiting-view {
  min-height: 100vh;
  max-width: 430px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  display: flex;
  flex-direction: column;
  position: relative;
}

/* 메인 컨텐츠 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  gap: 32px;
}

/* 로딩 아이콘 컨테이너 */
.loading-icon-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-icon {
  width: 80px;
  height: 80px;
  background-color: #e0e7ff;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 40px;
  height: 40px;
  color: #6366f1;
  animation: rotate 1.5s linear infinite;
}

.spinner-path {
  stroke-dasharray: 90, 150;
  stroke-dashoffset: 0;
  animation: dash 1.5s ease-in-out infinite;
}

@keyframes rotate {
  100% {
    transform: rotate(360deg);
  }
}

@keyframes dash {
  0% {
    stroke-dasharray: 1, 150;
    stroke-dashoffset: 0;
  }

  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -35;
  }

  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -124;
  }
}

/* 상태 텍스트 */
.status-text {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.status-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

/* 대기 순번 배지 */
.queue-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background-color: #3b82f6;
  color: white;
  font-size: 14px;
  font-weight: 600;
  padding: 8px 20px;
  border-radius: 20px;
}

.status-description {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  margin: 0;
}

/* 통화 컨트롤 버튼 */
.call-controls {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  gap: 32px;
  margin-top: 48px;
}

.control-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
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

.control-icon {
  width: 24px;
  height: 24px;
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

.control-label {
  font-size: 11px;
  font-weight: 500;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.control-label.end-label {
  color: #ef4444;
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
</style>
