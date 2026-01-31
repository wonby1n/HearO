<template>
  <div class="header-wrapper bg-white shadow-sm border-b border-gray-100 px-8 py-5">
    <div class="flex justify-between items-center">
      <!-- 좌측: 인사말 + 날짜/시간 -->
      <div class="info-section">
        <h2 class="greeting-text text-2xl font-bold text-gray-900 tracking-tight">
          어서오세요, <span class="user-name text-primary-main">{{ authStore.user?.name || '로그인안됨' }}</span>님
        </h2>
        <p class="datetime-text text-xs font-medium text-gray-400 mt-1.5 flex items-center gap-1.5">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {{ formattedDateTime }}
        </p>
      </div>

      <!-- 중앙: 상담 상태 토글 버튼 -->
      <div class="status-toggle-section">
        <button
          @click="toggleConsultationStatus"
          class="status-btn px-6 py-2.5 rounded-xl shadow-sm transition-all duration-300 hover:shadow-md active:scale-95 focus:outline-none"
          :data-active="dashboardStore.consultationStatus.isActive"
        >
          <div class="flex items-center text-white">
            <div class="icon-circle mr-3 flex items-center justify-center bg-white/20 rounded-lg p-1">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
              </svg>
            </div>
            <div class="text-left">
              <p class="font-bold text-sm leading-tight">
                상담 {{ dashboardStore.consultationStatus.isActive ? 'ON' : 'OFF' }}
              </p>
              <p class="text-[10px] opacity-80 font-medium">
                {{ formattedCallStatus }}
              </p>
            </div>
          </div>
        </button>
      </div>

      <!-- 우측: 대기 고객 수 -->
      <div class="waiting-section text-right relative">
        <div class="flex items-center justify-end gap-2.5">
          <div class="p-2 bg-gray-50 rounded-full">
            <svg class="w-5 h-5 text-gray-500" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
            </svg>
          </div>
          <div class="relative">
            <span
              class="count-text text-2xl font-black text-primary-main tracking-tighter transition-all duration-300 inline-block"
              :class="{ 'customer-count-pulse': isCountAnimating }"
            >
              {{ dashboardStore.waitingCustomers }}<small class="text-xs font-bold ml-0.5">명</small>
            </span>
            <Transition name="count-change">
              <span
                v-if="countChange !== 0"
                class="count-indicator absolute -top-1.5 -right-6 text-[10px] font-black px-1.5 py-0.5 rounded-full"
                :class="countChange > 0 ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'"
              >
                {{ countChange > 0 ? '+' : '' }}{{ countChange }}
              </span>
            </Transition>
          </div>
        </div>
        <p class="text-[11px] font-bold text-gray-400 mt-0.5 uppercase tracking-widest">현재 대기 고객 수</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { useDashboardStore } from '@/stores/dashboard'
import { useAgentStore } from '@/stores/agent'
import { useCallConnection } from '@/composables/useCallConnection'

const authStore = useAuthStore()
const dashboardStore = useDashboardStore()
const agentStore = useAgentStore()

// 통화 연결 관리 (상담원용)
const { startListening, disconnect: disconnectCall, matchedData, navigateToCall } = useCallConnection('counselor', {
  onMatched: (data) => {
    console.log('[DashboardHeader] 매칭 알림 수신:', data)
    // DashboardStore에 매칭 데이터 저장 (모달 표시용)
    dashboardStore.setMatchedData(data)
  }
})

// --- 상태 변수 ---
const currentTime = ref(new Date())
const isCountAnimating = ref(false)
const countChange = ref(0)
let countChangeTimer = null
let countAnimationTimer = null
let clockInterval = null
let heartbeatInterval = null
let isRequestPending = false

// --- 포맷팅 ---
const formattedDateTime = computed(() => {
  const d = currentTime.value
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const weekdays = ['일요일', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일']
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return `${year}.${month}.${day} ${weekdays[d.getDay()]} ${hours}:${minutes}:${seconds}`
})

const formattedCallStatus = computed(() => {
  return dashboardStore.consultationStatus.isActive ? 'READY FOR CALL' : 'CLICK TO START'
})

// --- 하트비트 로직 ---
const sendHeartbeat = async (forceStatus = null) => {
  if (isRequestPending && forceStatus === null) return
  isRequestPending = true
  try {
    const status = forceStatus !== null ? forceStatus : !!dashboardStore.consultationStatus.isActive
    const response = await axios.post('/api/v1/users/me/heartbeat', { isHeartbeatActive: status })

    console.log('[Heartbeat] 응답 성공:', response.data)
  } catch (error) {
    console.error('[Heartbeat] 전송 실패:', error.response?.data || error.message)
  } finally {
    isRequestPending = false
  }
}

const startHeartbeat = () => {
  if (heartbeatInterval) {
    clearInterval(heartbeatInterval)
    heartbeatInterval = null
  }
  sendHeartbeat()
  heartbeatInterval = setInterval(() => sendHeartbeat(), 10000)
}

const stopHeartbeat = () => {
  if (heartbeatInterval) {
    console.log('[Heartbeat] 중지')
    clearInterval(heartbeatInterval)
    heartbeatInterval = null
  }
}

const handleBeforeUnload = () => {
  if (dashboardStore.consultationStatus.isActive) {
    // 병렬 처리로 페이지 종료 전 완료 확률 향상
    Promise.all([
      fetch('/api/v1/users/me/heartbeat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ isHeartbeatActive: false }),
        keepalive: true
      }),
      fetch('/api/v1/users/me/status', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: 'REST' }),
        keepalive: true
      })
    ]).catch(err => console.error('[BeforeUnload] 요청 실패:', err))
  }
}


// --- 상담사 상태 업데이트 ---
const updateCounselorStatus = async (status) => {

  try {
    const response = await axios.patch('/api/v1/users/me/status', {
      status: status
    })

    if (response.data.isSuccess) {
    }
  } catch (error) {
    // 401은 interceptor가 처리하므로 여기서는 다른 에러만 처리
    if (error.code === 'ECONNABORTED' || error.code === 'ERR_NETWORK') {
      console.error('[Status Update] 네트워크 연결 불안정:', error.message)
    } else if (error.response?.status !== 401) {
      console.error('[Status Update] 실패:', error.response?.data || error.message)
    }
  }
}





const toggleConsultationStatus = () => {
  const newStatus = !dashboardStore.consultationStatus.isActive
  dashboardStore.consultationStatus.isActive = newStatus
}

// --- Watchers ---
watch(
  () => dashboardStore.consultationStatus.isActive,
  (isActive, oldValue) => {
    // 초기 로드 시 실행 방지 (undefined → false 변경 시 무시)
    if (oldValue === undefined && !isActive) {
      return
    }

    const status = isActive ? 'AVAILABLE' : 'REST'
    updateCounselorStatus(status)

    // 하트비트 관리
    if (isActive) {
      startHeartbeat()
      // 상담 대기 상태로 변경
      agentStore.currentStatus = 'AVAILABLE'

      // 매칭 알림 구독 시작
      const counselorId = authStore.user?.id
      if (counselorId) {
        console.log('[DashboardHeader] 매칭 알림 구독 시작, counselorId:', counselorId)
        startListening(counselorId)
      }
    } else {
      sendHeartbeat(false)
      stopHeartbeat()
      // 휴식 상태로 변경
      agentStore.currentStatus = 'REST'

      // 매칭 알림 구독 해제
      disconnectCall()
      console.log('[DashboardHeader] 매칭 알림 구독 해제')
    }
  }
)

watch(
  () => dashboardStore.waitingCustomers,
  (newCount, oldCount) => {
    if (oldCount !== undefined && newCount !== oldCount) {
      if (countAnimationTimer) clearTimeout(countAnimationTimer)
      isCountAnimating.value = true
      countAnimationTimer = setTimeout(() => { isCountAnimating.value = false }, 600)

      const change = newCount - oldCount
      countChange.value = change
      if (countChangeTimer) clearTimeout(countChangeTimer)
      countChangeTimer = setTimeout(() => { countChange.value = 0 }, 2000)
    }
  }
)

onMounted(() => {
  clockInterval = setInterval(() => { currentTime.value = new Date() }, 1000)
  window.addEventListener('beforeunload', handleBeforeUnload)
  if (dashboardStore.consultationStatus.isActive) startHeartbeat()
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
  if (clockInterval) clearInterval(clockInterval)
  stopHeartbeat()
  if (countChangeTimer) clearTimeout(countChangeTimer)
  if (countAnimationTimer) clearTimeout(countAnimationTimer)
})
</script>

<style scoped>
/**
 * 테마 변수 설정
 */
.header-wrapper {
  --header-primary: #1F3A8C;
  --header-danger: #ef4444;
  --header-inactive: #6b7280;
  --header-bg-soft: #f9fafb;
}

.text-primary-main {
  color: var(--header-primary);
}

/* 상태 버튼 스타일 */
.status-btn {
  background-color: var(--header-inactive);
}

.status-btn[data-active="true"] {
  background-color: var(--header-danger);
  animation: pulse-red 2s infinite;
}

@keyframes pulse-red {
  0% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(239, 68, 68, 0); }
  100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); }
}

/* 대기 고객 수 애니메이션 */
.customer-count-pulse {
  animation: customerCountPulse 0.6s ease-out;
}

@keyframes customerCountPulse {
  0% { transform: scale(1); }
  25% { transform: scale(1.15); color: #2563eb; }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

/* 지표 증감 애니메이션 */
.count-change-enter-active { animation: countChangeIn 0.3s ease-out; }
.count-change-leave-active { animation: countChangeOut 0.3s ease-in; }

@keyframes countChangeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes countChangeOut {
  from { opacity: 1; transform: translateY(0); }
  to { opacity: 0; transform: translateY(-5px); }
}
</style>