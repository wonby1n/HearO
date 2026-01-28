<template>
  <div class="card h-full flex flex-col p-6 bg-white rounded-2xl shadow-sm">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-semibold text-gray-800">에너지 지수</h3>
      <button
        @click="refreshStressLevel"
        class="p-2 rounded-lg hover:bg-gray-100 transition-colors group"
        title="새로고침"
      >
        <svg 
          xmlns="http://www.w3.org/2000/svg" 
          class="h-5 w-5 text-gray-500 group-active:animate-spin-once" 
          fill="none" 
          viewBox="0 0 24 24" 
          stroke="currentColor"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
        </svg>
      </button>
    </div>

    <!-- 원형 프로그레스 바 영역 -->
    <div class="relative flex-1 flex items-center justify-center min-h-[300px]">
      <div class="relative w-full max-w-[280px] aspect-square">
        <!-- SVG 원형 프로그레스 -->
        <svg class="transform -rotate-90 w-full h-full" viewBox="0 0 280 280">
          <!-- 배경 원 -->
          <circle
            cx="140"
            cy="140"
            r="120"
            stroke="#e5e7eb"
            stroke-width="20"
            fill="none"
          />
          <!-- 진행 원 (4단계 색상 적용) -->
          <circle
            cx="140"
            cy="140"
            r="120"
            :stroke="statusColor"
            stroke-width="20"
            fill="none"
            stroke-linecap="round"
            :stroke-dasharray="circumference"
            :stroke-dashoffset="dashOffset"
            class="transition-all duration-1000 ease-in-out"
            :style="{ filter: `drop-shadow(0 0 8px ${statusColor}60)` }"
          />
        </svg>

        <!-- 중앙 아이콘 -->
        <div class="absolute inset-0 flex items-center justify-center">
          <div 
            class="p-6 rounded-full transition-all duration-500"
            :style="{ backgroundColor: `${statusColor}15` }"
          >
            <svg
              class="w-16 h-16 animate-pulse-slow"
              fill="none"
              viewBox="0 0 24 24"
              stroke-width="1.5"
              :stroke="statusColor"
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

    <!-- 하단 메시지 섹션 -->
    <div class="mt-4 text-center">
      <div 
        class="inline-block px-4 py-1 rounded-full text-xs font-bold mb-2 transition-colors duration-500" 
        :style="{ backgroundColor: `${statusColor}20`, color: statusColor }"
      >
        ENERGY LEVEL: {{ batteryLevel }}%
      </div>
      <p class="text-2xl font-bold tracking-tight transition-colors duration-500" :style="{ color: statusColor }">
        {{ statusContent.message }}
      </p>
      <p class="text-sm text-gray-500 mt-1 font-medium">{{ statusContent.description }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useAgentStore } from '@/stores/agent'

const agentStore = useAgentStore()

// 원의 둘레 고정값 (r=120)
const circumference = 2 * Math.PI * 120

// 에너지 레벨 (100 - 스트레스 지수)
const batteryLevel = computed(() => {
  return Math.max(0, Math.min(100, 100 - (40 || 0)))
})

/**
 * 4단계 상태 컬러 로직
 * - 80% 이상: 파란색 (Best)
 * - 55% ~ 79%: 노란색 (Warning/Stretch) -> 새로 추가된 단계
 * - 30% ~ 54%: 주황색 (Tired)
 * - 30% 미만: 빨간색 (Critical)
 */
const statusColor = computed(() => {
  const level = batteryLevel.value
  if (level >= 80) return '#3d5abe' // 최상 (Blue)
  if (level >= 55) return '#f59e0b' // 주의/스트레칭 필요 (Yellow/Amber)
  if (level >= 30) return '#f97316' // 휴식 필요 (Orange)
  return '#ef4444' // 긴급 (Red)
})

// 차트 오프셋 계산
const dashOffset = computed(() => {
  return circumference * (1 - batteryLevel.value / 100)
})

// 4단계 메시지 및 설명 관리
const statusContent = computed(() => {
  const level = batteryLevel.value
  
  if (level >= 80) {
    return {
      message: '최상의 컨디션!',
      description: '아주 좋은 상태입니다. 지금처럼 유지하세요!'
    }
  } else if (level >= 55) {
    return {
      message: '스트레칭 어때요?',
      description: '조금씩 피로가 쌓이고 있어요. 몸을 가볍게 풀어주세요.'
    }
  } else if (level >= 30) {
    return {
      message: '휴식이 필요해요',
      description: '집중력이 떨어질 수 있습니다. 잠시 차 한 잔 어떠세요?'
    }
  } else {
    return {
      message: '긴급 충전 필요!',
      description: '즉시 휴식을 취하고 심호흡을 통해 안정을 찾으세요.'
    }
  }
})

const refreshStressLevel = async () => {
  // 백엔드 API 호출 로직
  if (agentStore.fetchStressLevel) {
    await agentStore.fetchStressLevel()
  }
}
</script>

<style scoped>
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

circle {
  /* 4단계 전환이 부드럽게 보이도록 transition 설정 */
  transition: stroke-dashoffset 1s cubic-bezier(0.4, 0, 0.2, 1), stroke 0.8s ease-in-out;
}
</style>