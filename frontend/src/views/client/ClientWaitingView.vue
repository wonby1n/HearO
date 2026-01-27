<template>
  <div class="client-waiting-view">

    <!-- 메인 컨텐츠 -->
    <div class="main-content">
      <!-- 로딩 스피너 아이콘 -->
      <div class="loading-icon-container">
        <div class="loading-icon">
          <svg class="spinner" viewBox="0 0 50 50">
            <circle
              class="spinner-path"
              cx="25"
              cy="25"
              r="20"
              fill="none"
              stroke="currentColor"
              stroke-width="4"
              stroke-linecap="round"
            />
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
          <button
            @click="toggleSpeaker"
            :class="['control-btn', { active: isSpeakerOn }]"
            title="스피커"
          >
            <div class="control-icon-wrapper">
              <svg v-if="isSpeakerOn" class="control-icon" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M9.383 3.076A1 1 0 0110 4v12a1 1 0 01-1.707.707L4.586 13H2a1 1 0 01-1-1V8a1 1 0 011-1h2.586l3.707-3.707a1 1 0 011.09-.217zM14.657 2.929a1 1 0 011.414 0A9.972 9.972 0 0119 10a9.972 9.972 0 01-2.929 7.071 1 1 0 01-1.414-1.414A7.971 7.971 0 0017 10c0-2.21-.894-4.208-2.343-5.657a1 1 0 010-1.414zm-2.829 2.828a1 1 0 011.415 0A5.983 5.983 0 0115 10a5.984 5.984 0 01-1.757 4.243 1 1 0 01-1.415-1.415A3.984 3.984 0 0013 10a3.983 3.983 0 00-1.172-2.828 1 1 0 010-1.415z" clip-rule="evenodd"/>
              </svg>
              <svg v-else class="control-icon" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M9.383 3.076A1 1 0 0110 4v12a1 1 0 01-1.707.707L4.586 13H2a1 1 0 01-1-1V8a1 1 0 011-1h2.586l3.707-3.707a1 1 0 011.09-.217zM12.293 7.293a1 1 0 011.414 0L15 8.586l1.293-1.293a1 1 0 111.414 1.414L16.414 10l1.293 1.293a1 1 0 01-1.414 1.414L15 11.414l-1.293 1.293a1 1 0 01-1.414-1.414L13.586 10l-1.293-1.293a1 1 0 010-1.414z" clip-rule="evenodd"/>
              </svg>
            </div>
          </button>
          <span class="control-label">SPEAKER</span>
        </div>

        <!-- 통화 종료 버튼 -->
        <div class="control-item">
          <button
            @click="handleEndCall"
            class="control-btn end-call"
            title="통화 종료"
          >
            <div class="control-icon-wrapper end">
              <svg class="control-icon" fill="currentColor" viewBox="0 0 20 20">
                <path d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z"/>
                <path d="M16.707 3.293a1 1 0 010 1.414L15.414 6l1.293 1.293a1 1 0 01-1.414 1.414L14 7.414l-1.293 1.293a1 1 0 11-1.414-1.414L12.586 6l-1.293-1.293a1 1 0 011.414-1.414L14 4.586l1.293-1.293a1 1 0 011.414 0z"/>
              </svg>
            </div>
          </button>
          <span class="control-label end-label">END CALL</span>
        </div>

        <!-- 음소거 버튼 -->
        <div class="control-item">
          <button
            @click="toggleMute"
            :class="['control-btn', { active: isMuted }]"
            title="음소거"
          >
            <div class="control-icon-wrapper">
              <svg v-if="!isMuted" class="control-icon" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M7 4a3 3 0 016 0v4a3 3 0 11-6 0V4zm4 10.93A7.001 7.001 0 0017 8a1 1 0 10-2 0A5 5 0 015 8a1 1 0 00-2 0 7.001 7.001 0 006 6.93V17H6a1 1 0 100 2h8a1 1 0 100-2h-3v-2.07z" clip-rule="evenodd"/>
              </svg>
              <svg v-else class="control-icon" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M7 4a3 3 0 016 0v4a3 3 0 11-6 0V4zm4 10.93A7.001 7.001 0 0017 8a1 1 0 10-2 0A5 5 0 015 8a1 1 0 00-2 0 7.001 7.001 0 006 6.93V17H6a1 1 0 100 2h8a1 1 0 100-2h-3v-2.07z" clip-rule="evenodd"/>
                <path d="M3.293 3.293a1 1 0 011.414 0l12 12a1 1 0 01-1.414 1.414l-12-12a1 1 0 010-1.414z"/>
              </svg>
            </div>
          </button>
          <span class="control-label">MUTE</span>
        </div>
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
import { useCallStore } from '@/stores/call'
import { useCustomerStore } from '@/stores/customer'

const router = useRouter()
const callStore = useCallStore()
const customerStore = useCustomerStore()

// 상태 관리
const queuePosition = ref(3) // 대기 순번 (추후 백엔드 연동)
const isMuted = ref(false)
const isSpeakerOn = ref(true)
const showConfirmModal = ref(false)

let queuePollingInterval = null

// 대기열 순번 조회 (백엔드 연동용)
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
const updateQueuePosition = (newPosition) => {
  if (newPosition >= 0) {
    queuePosition.value = newPosition
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

  if (queuePollingInterval) {
    clearInterval(queuePollingInterval)
  }

  // TODO: 백엔드에 대기 취소 요청
  // await api.post('/queue/cancel', { customerId: customerStore.currentCustomer?.id })

  callStore.endCall()
  router.push('/client')
}

// 상담원 연결 시 호출될 함수
const onAgentConnected = () => {
  // 상담 화면으로 이동
  router.push('/client/call')
}

// 컴포넌트 마운트 시 초기화
onMounted(async () => {
  // 초기 대기 순번 조회
  await fetchQueuePosition()

  // 주기적으로 대기 순번 업데이트 (5초마다)
  queuePollingInterval = setInterval(fetchQueuePosition, 5000)

  // TODO: WebSocket 연결로 실시간 대기 순번 업데이트
  // socket.on('queue:update', (data) => {
  //   updateQueuePosition(data.position)
  // })

  // TODO: 상담원 연결 이벤트 수신
  // socket.on('agent:connected', onAgentConnected)
})

// 컴포넌트 언마운트 시 정리
onUnmounted(() => {
  if (queuePollingInterval) {
    clearInterval(queuePollingInterval)
  }

  // TODO: WebSocket 이벤트 해제
  // socket.off('queue:update')
  // socket.off('agent:connected')
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
