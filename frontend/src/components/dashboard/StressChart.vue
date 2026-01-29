<template>
  <!-- [수정] 전체적인 패딩과 크기를 컴팩트하게 조정 -->
  <div 
    class="energy-card card h-full flex flex-col p-5 bg-white rounded-2xl shadow-sm transition-all duration-300"
    :data-status="statusKey"
  >
    <div class="flex items-center justify-between mb-2">
      <h3 class="text-base font-semibold text-gray-800">에너지 지수</h3>
      <button
        @click="refreshStressLevel"
        class="refresh-btn p-1.5 rounded-lg hover:bg-gray-100 transition-colors group"
        title="새로고침"
      >
        <svg 
          xmlns="http://www.w3.org/2000/svg" 
          class="h-4 w-4 text-gray-500 group-active:animate-spin-once" 
          fill="none" 
          viewBox="0 0 24 24" 
          stroke="currentColor"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
        </svg>
      </button>
    </div>

    <!-- 원형 프로그레스 바 영역: 사이즈를 280에서 220으로 축소 -->
    <div class="relative flex-1 flex items-center justify-center min-h-0">
      <div class="relative w-full max-w-[220px] aspect-square">
        <!-- SVG 원형 프로그레스 -->
        <svg class="progress-svg transform -rotate-90 w-full h-full" viewBox="0 0 240 240">
          <circle
            cx="120"
            cy="120"
            r="100"
            class="circle-bg"
          />
          <circle
            cx="120"
            cy="120"
            r="100"
            class="circle-progress"
            :stroke-dasharray="circumference"
            :stroke-dashoffset="dashOffset"
          />
        </svg>

        <!-- 중앙 아이콘: 크기 조정 -->
        <div class="absolute inset-0 flex items-center justify-center">
          <div class="icon-container p-4 rounded-full transition-all duration-500">
            <svg
              class="w-12 h-12 animate-pulse-slow status-icon"
              fill="none"
              viewBox="0 0 24 24"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z"
              />
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- 하단 메시지 섹션: 텍스트 사이즈 소폭 축소 -->
    <div class="mt-4 text-center">
      <div class="status-badge inline-block px-3 py-0.5 rounded-full text-[10px] font-bold mb-2 transition-colors duration-500">
        ENERGY LEVEL
      </div>
      <p class="status-message text-xl font-bold tracking-tight transition-colors duration-500">
        {{ statusContent.message }}
      </p>
      <p class="text-xs text-gray-500 mt-1 font-medium leading-relaxed">{{ statusContent.description }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useAgentStore } from '@/stores/agent'
import { useDashboardStore } from '@/stores/dashboard'

const agentStore = useAgentStore()
const dashboardStore = useDashboardStore()

// 컴포넌트 마운트 시 에너지 애니메이션 시작
onMounted(() => {
  agentStore.startEnergyAnimation()
})

// 컴포넌트 언마운트 시 에너지 애니메이션 중지
onUnmounted(() => {
  agentStore.stopEnergyAnimation()
})

// [수정] 반지름 100 기준 둘레 계산
const circumference = 2 * Math.PI * 100

const batteryLevel = computed(() => {
  return Math.max(0, Math.min(100, 100 - (agentStore.stressLevel || 0)))
})

const statusKey = computed(() => {
  const level = batteryLevel.value
  if (level >= 80) return 'best'
  if (level >= 55) return 'stretch'
  if (level >= 30) return 'tired'
  return 'critical'
})

const dashOffset = computed(() => {
  return circumference * (1 - batteryLevel.value / 100)
})

const statusContent = computed(() => {
  const key = statusKey.value
  const contentMap = {
    best: { message: '최상의 컨디션!', description: '현재 상태를 유지하세요!' },
    stretch: { message: '스트레칭 어때요?', description: '피로가 쌓이기 전 몸을 풀어주세요.' },
    tired: { message: '휴식이 필요해요', description: '잠시 차 한 잔의 여유를 가져보세요.' },
    critical: { message: '긴급 휴식 권고!', description: '심호흡을 하고 잠시 멈추세요.' }
  }
  return contentMap[key]
})

const refreshStressLevel = async () => {
  console.log('[StressChart] 새로고침 버튼 클릭 - 스트레스 데이터만 갱신')
  await dashboardStore.refreshStressData()
}
</script>

<style scoped>
.energy-card {
  --energy-theme-color: #3d5abe;
  --energy-bg-opacity: rgba(61, 90, 190, 0.1);
  --energy-badge-opacity: rgba(61, 90, 190, 0.15);
}

.energy-card[data-status="best"] {
  --energy-theme-color: #3d5abe;
  --energy-bg-opacity: rgba(61, 90, 190, 0.1);
}

.energy-card[data-status="stretch"] {
  --energy-theme-color: #f59e0b;
  --energy-bg-opacity: rgba(245, 158, 11, 0.1);
}

.energy-card[data-status="tired"] {
  --energy-theme-color: #f97316;
  --energy-bg-opacity: rgba(249, 115, 22, 0.1);
}

.energy-card[data-status="critical"] {
  --energy-theme-color: #ef4444;
  --energy-bg-opacity: rgba(239, 68, 68, 0.1);
}

.circle-bg {
  stroke: #f3f4f6;
  stroke-width: 16;
  fill: none;
}

.circle-progress {
  stroke: var(--energy-theme-color);
  stroke-width: 16;
  fill: none;
  stroke-linecap: round;
  transition: stroke-dashoffset 1s cubic-bezier(0.4, 0, 0.2, 1), stroke 0.8s ease-in-out;
  filter: drop-shadow(0 0 6px var(--energy-theme-color));
}

.icon-container {
  background-color: var(--energy-bg-opacity);
}

.status-icon {
  stroke: var(--energy-theme-color);
}

.status-badge {
  background-color: var(--energy-badge-opacity);
  color: var(--energy-theme-color);
}

.status-message {
  color: var(--energy-theme-color);
}

@keyframes spin-once {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.animate-spin-once {
  animation: spin-once 0.6s ease-in-out;
}

@keyframes pulse-slow {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(0.95); }
}
.animate-pulse-slow {
  animation: pulse-slow 3s ease-in-out infinite;
}
</style>