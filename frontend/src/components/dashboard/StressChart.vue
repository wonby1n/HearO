<template>
  <div class="card h-full flex flex-col">
    <h3 class="text-lg font-semibold mb-4">현재 스트레스 지수</h3>

    <!-- 차트 컨테이너 -->
    <div class="relative flex-1 flex items-center justify-center">
      <!-- Doughnut 차트 -->
      <Doughnut :data="chartData" :options="chartOptions" />

      <!-- 중앙 텍스트 오버레이 -->
      <div class="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
        <p class="text-6xl font-bold text-gray-900">{{ agentStore.stressLevel }}%</p>
      </div>
    </div>

    <!-- 하단 메시지 -->
    <div class="mt-6 text-center">
      <p class="text-3xl font-bold text-primary-700">{{ stressMessage }}</p>
      <p class="text-base text-gray-600 mt-2">{{ stressDescription }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Doughnut } from 'vue-chartjs'
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip
} from 'chart.js'
import { useAgentStore } from '@/stores/agent'

// Chart.js 요소 등록
ChartJS.register(ArcElement, Tooltip)

const agentStore = useAgentStore()

// 차트 데이터
const chartData = computed(() => ({
  datasets: [{
    data: [agentStore.stressLevel, 100 - agentStore.stressLevel],
    backgroundColor: ['#ef4444', '#e5e7eb'], // red-500, gray-200
    borderWidth: 0
  }]
}))

// 차트 옵션
const chartOptions = {
  cutout: '70%',
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    tooltip: { enabled: false },
    legend: { display: false }
  }
}

// 스트레스 메시지
const stressMessage = computed(() => {
  const level = agentStore.stressLevel
  if (level >= 70) return '휴식이 필요해요!'
  if (level >= 40) return '적당한 긴장 상태예요'
  return '좋은 컨디션이에요!'
})

// 스트레스 설명
const stressDescription = computed(() => {
  const level = agentStore.stressLevel
  if (level >= 70) return '잠시 휴식을 취하고 심호흡을 해보세요'
  if (level >= 40) return '컨디션 관리에 신경 써주세요'
  return '이 상태를 유지하세요'
})
</script>

<style scoped>
/* StressChart 전용 스타일 */
</style>
