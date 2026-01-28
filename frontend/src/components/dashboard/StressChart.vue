<template>
  <!-- [피드백 반영] data-status 속성을 통해 현재 상태에 따른 CSS 변수를 제어합니다. -->
  <div 
    class="energy-card card h-full flex flex-col p-6 bg-white rounded-2xl shadow-sm"
    :data-status="statusKey"
  >
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-semibold text-gray-800">에너지 지수</h3>
      <button
        @click="refreshStressLevel"
        class="refresh-btn p-2 rounded-lg hover:bg-gray-100 transition-colors group"
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
        <svg class="progress-svg transform -rotate-90 w-full h-full" viewBox="0 0 280 280">
          <!-- 배경 원 -->
          <circle
            cx="140"
            cy="140"
            r="120"
            class="circle-bg"
          />
          <!-- [피드백 반영] 진행 원: 인라인 스타일 대신 클래스를 사용합니다. -->
          <circle
            cx="140"
            cy="140"
            r="120"
            class="circle-progress"
            :stroke-dasharray="circumference"
            :stroke-dashoffset="dashOffset"
          />
        </svg>

        <!-- 중앙 아이콘 -->
        <div class="absolute inset-0 flex items-center justify-center">
          <div class="icon-container p-6 rounded-full transition-all duration-500">
            <svg
              class="w-16 h-16 animate-pulse-slow status-icon"
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

    <!-- 하단 메시지 섹션 -->
    <div class="mt-4 text-center">
      <div class="status-badge inline-block px-4 py-1 rounded-full text-xs font-bold mb-2 transition-colors duration-500">
        ENERGY LEVEL: {{ batteryLevel }}%
      </div>
      <p class="status-message text-2xl font-bold tracking-tight transition-colors duration-500">
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
  return Math.max(0, Math.min(100, 100 - (agentStore.stressLevel || 0)))
})

/**
 * [피드백 반영] 상태 키워드 반환
 * 직접적인 색상값 대신 상태를 나타내는 키워드만 반환하여 스타일과 로직을 분리합니다.
 */
const statusKey = computed(() => {
  const level = batteryLevel.value
  if (level >= 80) return 'best'
  if (level >= 55) return 'stretch'
  if (level >= 30) return 'tired'
  return 'critical'
})

// 차트 오프셋 계산
const dashOffset = computed(() => {
  return circumference * (1 - batteryLevel.value / 100)
})

// 메시지 매핑
const statusContent = computed(() => {
  const key = statusKey.value
  const contentMap = {
    best: { message: '최상의 컨디션!', description: '아주 좋은 상태입니다. 지금처럼 유지하세요!' },
    stretch: { message: '스트레칭 어때요?', description: '조금씩 피로가 쌓이고 있어요. 몸을 가볍게 풀어주세요.' },
    tired: { message: '휴식이 필요해요', description: '집중력이 떨어질 수 있습니다. 잠시 차 한 잔 어떠세요?' },
    critical: { message: '긴급 충전 필요!', description: '즉시 휴식을 취하고 심호흡을 통해 안정을 찾으세요.' }
  }
  return contentMap[key]
})

const refreshStressLevel = async () => {
  if (agentStore.fetchStressLevel) {
    await agentStore.fetchStressLevel()
  }
}
</script>

<style scoped>
/**
 * [피드백 반영] 테마별 색상을 CSS 변수로 관리
 */
 .energy-card {
  --energy-theme-color: #3d5abe; /* Default: Best */
  --energy-bg-opacity: rgba(61, 90, 190, 0.15);
  --energy-badge-opacity: rgba(61, 90, 190, 0.2);
}

.energy-card[data-status="best"] {
  --energy-theme-color: #3d5abe;
  --energy-bg-opacity: rgba(61, 90, 190, 0.15);
  --energy-badge-opacity: rgba(61, 90, 190, 0.2);
}

.energy-card[data-status="stretch"] {
  --energy-theme-color: #f59e0b;
  --energy-bg-opacity: rgba(245, 158, 11, 0.15);
  --energy-badge-opacity: rgba(245, 158, 11, 0.2);
}

.energy-card[data-status="tired"] {
  --energy-theme-color: #f97316;
  --energy-bg-opacity: rgba(249, 115, 22, 0.15);
  --energy-badge-opacity: rgba(249, 115, 22, 0.2);
}

.energy-card[data-status="critical"] {
  --energy-theme-color: #ef4444;
  --energy-bg-opacity: rgba(239, 68, 68, 0.15);
  --energy-badge-opacity: rgba(239, 68, 68, 0.2);
}

/**
 * SVG 스타일링 클래스화
 */
.circle-bg {
  stroke: #e5e7eb;
  stroke-width: 20;
  fill: none;
}

.circle-progress {
  stroke: var(--energy-theme-color);
  stroke-width: 20;
  fill: none;
  stroke-linecap: round;
  /* [피드백 반영] SVG 속성들을 CSS 클래스에서 관리 */
  transition: stroke-dashoffset 1s cubic-bezier(0.4, 0, 0.2, 1), stroke 0.8s ease-in-out;
  filter: drop-shadow(0 0 8px var(--energy-theme-color));
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

/* Animations */
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