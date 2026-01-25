import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useDashboardStore = defineStore('dashboard', () => {
  // 주간 실적 데이터 (월~금)
  const weeklyPerformance = ref([
    { day: 'Mon', calls: 20 },
    { day: 'Tue', calls: 18 },
    { day: 'Wed', calls: 30 },
    { day: 'Thu', calls: 35 },
    { day: 'Fri', calls: 25 }
  ])

  // 총 상담 시간
  const totalCallTime = ref({
    hours: 135,
    minutes: 12
  })

  // 고객 만족도
  const customerSatisfaction = ref(4.8)

  // 대기 고객 수
  const waitingCustomers = ref(10)

  // 상담 상태
  const consultationStatus = ref({
    isActive: false,
    lastCallTime: 59 // 마지막 통화 후 경과 시간 (초)
  })

  // Todo 리스트
  const todos = ref([
    { id: 1, text: '통화 20통 채우기', completed: false },
    { id: 2, text: '점심 먹고 스트레칭', completed: false },
    { id: 3, text: '팀장님께 전화', completed: false },
    { id: 4, text: '담타 ㄱㄱ', completed: false }
  ])

  // Computed - 포맷된 통화 시간
  const formattedCallTime = computed(() => {
    return `${totalCallTime.value.hours}:${String(totalCallTime.value.minutes).padStart(2, '0')}`
  })

  // Actions - API 연동 준비 (현재는 더미 데이터 사용)
  const fetchWeeklyPerformance = async () => {
    try {
      // TODO: API 연동
      // const response = await fetch('/api/dashboard/weekly-performance')
      // if (!response.ok) throw new Error('데이터를 불러오지 못했습니다')
      // const data = await response.json()
      // weeklyPerformance.value = data
      console.log('fetchWeeklyPerformance - API 연동 대기 중')
    } catch (error) {
      console.error('주간 실적 조회 실패:', error)
      // TODO: 사용자에게 에러 메시지 표시 (Toast/Snackbar 등)
    }
  }

  const fetchStats = async () => {
    try {
      // TODO: API 연동
      // const response = await fetch('/api/dashboard/stats')
      // if (!response.ok) throw new Error('통계 데이터를 불러오지 못했습니다')
      // const data = await response.json()
      // totalCallTime.value = data.totalCallTime
      // customerSatisfaction.value = data.satisfaction
      console.log('fetchStats - API 연동 대기 중')
    } catch (error) {
      console.error('통계 조회 실패:', error)
      // TODO: 사용자에게 에러 메시지 표시 (Toast/Snackbar 등)
    }
  }

  const fetchWaitingCustomers = async () => {
    try {
      // TODO: WebSocket 또는 폴링으로 실시간 업데이트
      // const response = await fetch('/api/dashboard/waiting-customers')
      // if (!response.ok) throw new Error('대기 고객 수를 불러오지 못했습니다')
      // const data = await response.json()
      // waitingCustomers.value = data.count
      console.log('fetchWaitingCustomers - API 연동 대기 중')
    } catch (error) {
      console.error('대기 고객 수 조회 실패:', error)
      // TODO: 사용자에게 에러 메시지 표시 (Toast/Snackbar 등)
    }
  }

  const toggleTodo = (id) => {
    const todo = todos.value.find(t => t.id === id)
    if (todo) {
      todo.completed = !todo.completed
    }
  }

  const addTodo = (text) => {
    const newTodo = {
      id: crypto.randomUUID(),
      text,
      completed: false
    }
    todos.value.push(newTodo)
  }

  const deleteTodo = (id) => {
    todos.value = todos.value.filter(t => t.id !== id)
  }

  return {
    // State
    weeklyPerformance,
    totalCallTime,
    customerSatisfaction,
    waitingCustomers,
    consultationStatus,
    todos,
    // Computed
    formattedCallTime,
    // Actions
    fetchWeeklyPerformance,
    fetchStats,
    fetchWaitingCustomers,
    toggleTodo,
    addTodo,
    deleteTodo
  }
})
