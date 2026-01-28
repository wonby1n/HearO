<template>
  <div class="bg-white shadow-sm px-8 py-6">
    <div class="flex justify-between items-center">
      <!-- 좌측: 인사말 + 날짜/시간 -->
      <div>
        <h2 class="text-3xl font-bold text-gray-900">
          어서오세요, {{ agentStore.agentInfo?.name || '상담원' }}님
        </h2>
        <p class="text-base text-gray-500 mt-2">
          {{ formattedDateTime }}
        </p>
      </div>

      <!-- 중앙: 상담 상태 토글 버튼 -->
      <button
        @click="toggleConsultationStatus"
        :class="[
          'px-6 py-3 rounded-lg shadow-md transition-all duration-300 hover:shadow-lg active:scale-95 focus:outline-none focus:ring-2 focus:ring-offset-2',
          dashboardStore.consultationStatus.isActive ? 'bg-red-600 hover:bg-red-700 focus:ring-red-500' : 'bg-gray-600 hover:bg-gray-700 focus:ring-gray-500',
          dashboardStore.consultationStatus.isActive && 'animate-pulse'
        ]"
      >
        <div class="flex items-center text-white">
          <svg class="w-6 h-6 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
          </svg>
          <div>
            <p class="font-semibold text-lg">
              상담 {{ dashboardStore.consultationStatus.isActive ? 'ON' : 'OFF' }}
            </p>
            <p class="text-xs">
              {{ formattedCallStatus }}
            </p>
          </div>
        </div>
      </button>

      <!-- 우측: 대기 고객 수 -->
      <div class="text-right relative">
        <div class="flex items-center justify-end gap-2 mb-1">
          <svg class="w-6 h-6 text-gray-700" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
          </svg>
          <div class="relative">
            <span
              :class="[
                'text-3xl font-bold text-primary-600 transition-all duration-300',
                { 'customer-count-pulse': isCountAnimating }
              ]"
            >
              {{ dashboardStore.waitingCustomers }}명
            </span>
            <Transition name="count-change">
              <span
                v-if="countChange !== 0"
                :class="[
                  'absolute -top-2 -right-8 text-sm font-bold',
                  countChange > 0 ? 'text-green-600' : 'text-red-600'
                ]"
              >
                {{ countChange > 0 ? '+' : '' }}{{ countChange }}
              </span>
            </Transition>
          </div>
        </div>
        <p class="text-sm text-gray-600">현재 대기 고객</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { useAgentStore } from '@/stores/agent'
import { useDashboardStore } from '@/stores/dashboard'

const agentStore = useAgentStore()
const dashboardStore = useDashboardStore()

// --- 상태 및 타이머 변수 ---
const currentTime = ref(new Date())
const isCountAnimating = ref(false)
const countChange = ref(0)
let countChangeTimer = null
let countAnimationTimer = null
let clockInterval = null
let heartbeatInterval = null

// --- 포맷팅 로직 ---
const formattedDateTime = computed(() => {
  const date = currentTime.value
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const weekdays = ['일요일', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일']
  const weekday = weekdays[date.getDay()]
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return `${year}년 ${month}월 ${day}일 ${weekday} ${hours}:${minutes}:${seconds}`
})

const formattedCallStatus = computed(() => {
  return dashboardStore.consultationStatus.isActive ? '통화 대기 중' : '클릭하여 상담 시작'
})


// --- Heartbeat 핵심 로직 ---

const sendHeartbeat = async () => {
  try {
    const status = !!dashboardStore.consultationStatus.isActive
    
    await axios.post('/api/v1/users/me/heartbeat', {
      isHeartbeatActive: status 
    })
    console.log(`[Heartbeat] 전송됨: ${status}`)
  } catch (error) {
    console.error('[Heartbeat] 전송 실패:', error)
  }
}

const startHeartbeat = () => {
  stopHeartbeat() 
  sendHeartbeat() 
  heartbeatInterval = setInterval(sendHeartbeat, 10000)
}

const stopHeartbeat = () => {
  // 피드백 반영: 조건부 체크 강화
  if (heartbeatInterval) {
    clearInterval(heartbeatInterval)
    heartbeatInterval = null
    console.log('[Heartbeat] 중지됨')
  }
}

// --- 감시자 (Watchers) ---

watch(
  () => dashboardStore.consultationStatus.isActive,
  (isActive) => {
    if (isActive) {
      startHeartbeat()
    } else {
      sendHeartbeat() // 마지막 상태값(false) 전송
      stopHeartbeat()
    }
  }
  // immediate: false 제거 (기본값 활용)
)

// --- 생명주기 ---

onUnmounted(() => {
  if (clockInterval) clearInterval(clockInterval)
  stopHeartbeat() // 내부에서 이미 null 체크를 수행하므로 안전함
  if (countChangeTimer) clearTimeout(countChangeTimer)
  if (countAnimationTimer) clearTimeout(countAnimationTimer)
})
</script>

<style scoped>
.customer-count-pulse {
  animation: customerCountPulse 0.6s ease-out;
}

@keyframes customerCountPulse {
  0% { transform: scale(1); }
  25% { transform: scale(1.2); color: #2563eb; }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.count-change-enter-active {
  animation: countChangeIn 0.3s ease-out;
}
.count-change-leave-active {
  animation: countChangeOut 0.3s ease-in;
}

@keyframes countChangeIn {
  from { opacity: 0; transform: translateY(10px) scale(0.8); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
@keyframes countChangeOut {
  from { opacity: 1; transform: translateY(0) scale(1); }
  to { opacity: 0; transform: translateY(-10px) scale(0.8); }
}
</style>