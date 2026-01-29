<template>
  <div class="performance-card card h-full flex flex-col p-5 bg-white rounded-2xl shadow-sm">
    <div class="mb-4">
      <div class="flex items-center justify-between">
        <div class="flex flex-col">
          <div class="flex items-center gap-2">
            <h3 class="text-base font-semibold text-gray-800">
              {{ viewType === 'weekly' ? currentWeekTitle : '2026년 월별 실적' }}
            </h3>
            <!-- 주간 보기일 때만 주차 이동 네비게이션 표시 -->
            <div v-if="viewType === 'weekly'" class="flex items-center bg-gray-50 rounded-lg px-1 py-0.5 ml-1">
              <button @click="changeWeek(-1)" class="p-1 hover:bg-gray-200 rounded transition-colors text-gray-500">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M15 19l-7-7 7-7" />
                </svg>
              </button>
              <button @click="resetToToday" class="px-1.5 text-[10px] font-bold text-gray-400 hover:text-blue-600">TODAY</button>
              <button @click="changeWeek(1)" class="p-1 hover:bg-gray-200 rounded transition-colors text-gray-500">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M9 5l7 7-7 7" />
                </svg>
              </button>
            </div>
          </div>
          <p class="text-xs text-gray-400 mt-0.5 font-medium">
            {{ viewType === 'weekly' ? currentWeekRange : '연간 통화 통계' }}
          </p>
        </div>
        
        <!-- 주간/월간 전환 탭 -->
        <div class="flex bg-gray-100 p-1 rounded-lg">
          <button 
            @click="viewType = 'weekly'"
            :class="['tab-btn', { 'active': viewType === 'weekly' }]"
          >
            주간
          </button>
          <button 
            @click="viewType = 'monthly'"
            :class="['tab-btn', { 'active': viewType === 'monthly' }]"
          >
            월간
          </button>
        </div>
      </div>
    </div>

    <!-- 차트 컨테이너 -->
    <div class="flex-1 min-h-0">
      <Bar :data="chartData" :options="chartOptions" />
    </div>

    <!-- 하단 요약 정보 -->
    <div class="summary-section mt-3 pt-3 border-t border-gray-50 flex justify-around">
      <div class="text-center">
        <p class="summary-label">총 통화량</p>
        <p class="summary-value text-gray-800">{{ summaryData.total }}건</p>
      </div>
      <div class="divider border-l border-gray-100"></div>
      <div class="text-center">
        <p class="summary-label">{{ viewType === 'weekly' ? '일평균' : '월평균' }}</p>
        <p class="summary-value text-primary-main">{{ summaryData.average }}건</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'
import { useDashboardStore } from '@/stores/dashboard'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const dashboardStore = useDashboardStore()
const viewType = ref('weekly')
const selectedWeekOffset = ref(0) // 0은 이번 주, -1은 저번 주, 1은 다음 주

/**
 * 테마 설정
 */
const theme = {
  colors: {
    primary: '#1F3A8C',
    inactive: '#E0E7FF',
    future: '#F3F4F6',
    grid: '#f9fafb',
    text: '#9ca3af'
  }
}

// 오늘 기준 정보
const realToday = new Date()
const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
const realTodayName = dayNames[realToday.getDay()]

// 선택된 주차의 기준 날짜 계산
const targetDate = computed(() => {
  const date = new Date(realToday)
  date.setDate(realToday.getDate() + (selectedWeekOffset.value * 7))
  return date
})

const changeWeek = (offset) => {
  selectedWeekOffset.value += offset
}

const resetToToday = () => {
  selectedWeekOffset.value = 0
}

const getWeekRange = (baseDate) => {
  const current = new Date(baseDate)
  const day = current.getDay()
  const diffToMon = current.getDate() - day + (day === 0 ? -6 : 1)
  const mon = new Date(current.setDate(diffToMon))
  const fri = new Date(current.setDate(mon.getDate() + 4))
  const formatDate = (d) => `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
  return `${formatDate(mon)} - ${formatDate(fri)}`
}

const currentWeekRange = computed(() => getWeekRange(targetDate.value))
const currentWeekTitle = computed(() => {
  const d = targetDate.value
  return `${d.getMonth() + 1}월 ${Math.ceil(d.getDate() / 7)}째주 실적`
})

// 주간 데이터 가공 (선택된 주차와 오늘 날짜를 비교하여 미래 여부 판단)
const processedWeeklyData = computed(() => {
  const weekDays = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri']
  
  return dashboardStore.weeklyPerformance.map((d, index) => {
    // 선택된 주차가 과거면 모든 데이터 표시, 현재면 오늘 이후 0, 미래면 모두 0
    let status = 'past'
    let displayCalls = d.calls

    if (selectedWeekOffset.value === 0) {
      // 이번 주일 때
      const todayIdx = weekDays.indexOf(realTodayName)
      if (index === todayIdx) status = 'today'
      else if (index > todayIdx && todayIdx !== -1) {
        status = 'future'
        displayCalls = 0
      }
    } else if (selectedWeekOffset.value > 0) {
      // 미래 주차일 때
      status = 'future'
      displayCalls = 0
    }

    return { ...d, displayCalls, status }
  })
})

const monthlyPerformance = [
  { month: '1월', calls: 450 }, { month: '2월', calls: 380 }, { month: '3월', calls: 520 },
  { month: '4월', calls: 0 }, { month: '5월', calls: 0 }, { month: '6월', calls: 0 },
  { month: '7월', calls: 0 }, { month: '8월', calls: 0 }, { month: '9월', calls: 0 },
  { month: '10월', calls: 0 }, { month: '11월', calls: 0 }, { month: '12월', calls: 0 }
]

const chartData = computed(() => {
  if (viewType.value === 'weekly') {
    return {
      labels: processedWeeklyData.value.map(d => d.day),
      datasets: [{
        data: processedWeeklyData.value.map(d => d.displayCalls),
        backgroundColor: processedWeeklyData.value.map(d => 
          d.status === 'today' ? theme.colors.primary : (d.status === 'future' ? theme.colors.future : theme.colors.inactive)
        ),
        borderRadius: 6,
        barThickness: 36, // 뚱뚱하게 조정 (기존 24)
      }]
    }
  } else {
    const currentMonthIdx = realToday.getMonth()
    return {
      labels: monthlyPerformance.map(d => d.month),
      datasets: [{
        data: monthlyPerformance.map(d => d.calls),
        backgroundColor: monthlyPerformance.map((d, i) => 
          i === currentMonthIdx ? theme.colors.primary : (i > currentMonthIdx ? theme.colors.future : theme.colors.inactive)
        ),
        borderRadius: 4,
        barThickness: 12, // 뚱뚱하게 조정 (기존 12)
      }]
    }
  }
})

const summaryData = computed(() => {
  const data = viewType.value === 'weekly' ? processedWeeklyData.value.map(d => d.displayCalls) : monthlyPerformance.map(d => d.calls)
  const total = data.reduce((acc, curr) => acc + curr, 0)
  const count = data.filter(v => v > 0).length || 1
  return {
    total: total.toLocaleString(),
    average: (total / count).toFixed(1)
  }
})

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  layout: {
    padding: { top: 15, bottom: 5, left: 10, right: 10 }
  },
  scales: {
    y: {
      beginAtZero: true,
      border: { display: false },
      ticks: {
        color: theme.colors.text,
        font: { size: 9 }
      },
      grid: { color: theme.colors.grid, drawTicks: false }
    },
    x: {
      border: { display: false },
      ticks: {
        color: (context) => {
          const label = context.tick.label
          // 이번 주를 보고 있을 때만 오늘 요일 강조
          const isHighlight = viewType.value === 'weekly' 
            ? (selectedWeekOffset.value === 0 && label === realTodayName)
            : label === `${realToday.getMonth() + 1}월`
          return isHighlight ? theme.colors.primary : theme.colors.text
        },
        font: { size: 10, weight: '700' }
      },
      grid: { display: false }
    }
  },
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: '#1f2937',
      padding: 12,
      cornerRadius: 8,
      callbacks: {
        label: (context) => ` 실적: ${context.parsed.y}건`
      }
    }
  }
}))
</script>

<style scoped>
.performance-card {
  --primary-main: #1F3A8C;
  --primary-light: #E0E7FF;
  --gray-background: #F3F4F6;
  --text-muted: #9ca3af;
  
  transition: all 0.3s ease;
}

.tab-btn {
  padding: 0.25rem 0.75rem;
  font-size: 0.7rem;
  font-weight: 700;
  border-radius: 0.375rem;
  transition: all 0.2s;
  color: var(--text-muted);
}

.tab-btn.active {
  background-color: #ffffff;
  color: var(--primary-main);
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.summary-label {
  font-size: 0.65rem;
  color: var(--text-muted);
  margin-bottom: 0.125rem;
}

.summary-value {
  font-size: 1.15rem;
  font-weight: 800;
}

.text-primary-main {
  color: var(--primary-main);
}

button {
  -webkit-tap-highlight-color: transparent;
}
</style>