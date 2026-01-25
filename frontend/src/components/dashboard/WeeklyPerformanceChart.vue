<template>
  <div class="card h-full flex flex-col">
    <div class="mb-4">
      <h3 class="text-lg font-semibold">1월 첫째주 실적</h3>
      <p class="text-sm text-gray-500">2026.01.01 - 2026.01.05</p>
    </div>

    <!-- 차트 컨테이너 -->
    <div class="flex-1 min-h-0">
      <Bar :data="chartData" :options="chartOptions" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip
} from 'chart.js'
import { useDashboardStore } from '@/stores/dashboard'

// Chart.js 요소 등록
ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip)

const dashboardStore = useDashboardStore()

// 차트 데이터
const chartData = computed(() => ({
  labels: dashboardStore.weeklyPerformance.map(d => d.day),
  datasets: [{
    data: dashboardStore.weeklyPerformance.map(d => d.calls),
    backgroundColor: [
      '#bae6fd', // Mon - primary-200
      '#bae6fd', // Tue
      '#bae6fd', // Wed
      '#bae6fd', // Thu
      '#0369a1'  // Fri - primary-700
    ],
    borderRadius: 8,
    barThickness: 40
  }]
}))

// 차트 옵션
const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        stepSize: 10
      },
      grid: {
        color: '#f3f4f6'
      }
    },
    x: {
      grid: {
        display: false
      }
    }
  },
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: '#1f2937',
      padding: 12,
      titleFont: {
        size: 14
      },
      bodyFont: {
        size: 13
      },
      callbacks: {
        label: function(context) {
          return `통화: ${context.parsed.y}건`
        }
      }
    }
  }
}
</script>

<style scoped>
/* WeeklyPerformanceChart 전용 스타일 */
</style>
