import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import axios from 'axios'
import { useAgentStore } from './agent'

export const useDashboardStore = defineStore('dashboard', () => {
  // 개발 환경에서 콘솔 테스트 활성화
  if (import.meta.env.DEV) {
    // 나중에 window에 노출
  }

  // 사용자 이름
  const userName = ref('')

  // 스트레스 지수
  const stressIndex = ref(0)

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

  // Todo 리스트 - localStorage에서 불러오기
  const loadTodosFromLocalStorage = () => {
    try {
      const savedTodos = localStorage.getItem('todos')
      return savedTodos ? JSON.parse(savedTodos) : []
    } catch (error) {
      console.error('Todo 로드 실패:', error)
      localStorage.removeItem('todos') // 손상된 데이터 제거
      return []
    }
  }

  const todos = ref(loadTodosFromLocalStorage())

  // todos 변경 시 localStorage에 자동 저장
  watch(
    todos,
    (newTodos) => {
      localStorage.setItem('todos', JSON.stringify(newTodos))
    },
    { deep: true }
  )

  // Computed - 포맷된 통화 시간
  const formattedCallTime = computed(() => {
    return `${totalCallTime.value.hours}:${String(totalCallTime.value.minutes).padStart(2, '0')}`
  })

  // Actions - 대시보드 데이터 조회 (통합)
  const fetchDashboardData = async () => {
    try {
      const response = await axios.get('/api/v1/dashboard')

      if (response.data.isSuccess) {
        const data = response.data.data

        // 사용자 이름
        userName.value = data.userName || ''

        // 현재 에너지 레벨 (currentEnergy -> stressLevel 변환)
        // 단, 애니메이션이 활성화되어 있으면 초기값만 설정하고 이후엔 애니메이션이 제어
        if (data.currentEnergy !== undefined) {
          const agentStore = useAgentStore()
          // currentEnergy는 100이 최대, stressLevel은 0이 최대이므로 변환
          const newStressLevel = 100 - data.currentEnergy

          // 처음 로드 시에만 설정 (이후에는 애니메이션이 제어)
          if (agentStore.stressLevel === null) {
            agentStore.stressLevel = newStressLevel
          }
        }

        // 상담원 상태 업데이트
        // heartbeat가 활성화되어 있으면 로컬 상태를 우선시 (백엔드 status 무시)
        if (data.status && !consultationStatus.value.isActive) {
          const agentStore = useAgentStore()
          agentStore.currentStatus = data.status
          console.log('[DashboardStore] 상담원 상태 업데이트:', data.status)
        } else if (consultationStatus.value.isActive) {
          console.log('[DashboardStore] heartbeat 활성화 중 - 로컬 상태(AVAILABLE) 유지')
        }

        // 스트레스 지수
        stressIndex.value = data.stressIndex || 0

        // 주간 실적 데이터 매핑 (백엔드: dayOfWeek/count -> 프론트: day/calls)
        if (data.weeklyChart && Array.isArray(data.weeklyChart)) {
          weeklyPerformance.value = data.weeklyChart.map(item => ({
            day: item.dayOfWeek,
            calls: item.count
          }))
        }

        // 총 상담 시간 파싱 ("135:12" -> {hours: 135, minutes: 12})
        if (data.totalDuration) {
          const [hours, minutes] = data.totalDuration.split(':').map(Number)
          totalCallTime.value = { hours, minutes }
        }

        // 고객 만족도
        customerSatisfaction.value = data.customerSatisfaction || 0

      } else {
        throw new Error(response.data.message || '데이터를 불러오지 못했습니다')
      }
    } catch (error) {
      console.error('대시보드 조회 실패:', error)
      // TODO: 사용자에게 에러 메시지 표시 (Toast/Snackbar 등)
    }
  }

  // 호환성을 위해 기존 함수명 유지 (내부적으로 fetchDashboardData 호출)
  const fetchWeeklyPerformance = fetchDashboardData
  const fetchStats = fetchDashboardData

  /**
   * 스트레스 차트 전용 새로고침 (경량화)
   * status와 currentEnergy만 가져와서 agentStore 업데이트
   * 다른 데이터(weeklyChart, totalDuration 등)는 건드리지 않음
   */
  const refreshStressData = async () => {
    try {
      const response = await axios.get('/api/v1/dashboard')

      if (response.data.isSuccess) {
        const data = response.data.data
        const agentStore = useAgentStore()

        // status 업데이트 (heartbeat 비활성화 시에만)
        if (data.status && !consultationStatus.value.isActive) {
          agentStore.currentStatus = data.status
          console.log('[DashboardStore] 스트레스 차트 새로고침 - status:', data.status)
        }

        // currentEnergy를 stressLevel로 변환하여 강제 동기화
        if (data.currentEnergy !== undefined) {
          const newStressLevel = 100 - data.currentEnergy
          agentStore.stressLevel = newStressLevel
          console.log('[DashboardStore] 스트레스 차트 새로고침 - stressLevel:', newStressLevel)
        }

        console.log('[DashboardStore] 스트레스 차트 새로고침 완료')
      }
    } catch (error) {
      console.error('[DashboardStore] 스트레스 차트 새로고침 실패:', error)
    }
  }

  const fetchWaitingCustomers = async () => {
    try {
      // TODO: REST API로 초기 대기 고객 수 조회
      // const response = await fetch('/api/v1/users/me/dashboard', {
      //   headers: {
      //     'Authorization': `Bearer ${getAccessToken()}`
      //   }
      // })
      // if (!response.ok) throw new Error('대기 고객 수를 불러오지 못했습니다')
      // const data = await response.json()
      // waitingCustomers.value = data.waitingCustomers
      console.log('fetchWaitingCustomers - API 연동 대기 중')
    } catch (error) {
      console.error('대기 고객 수 조회 실패:', error)
      // TODO: 사용자에게 에러 메시지 표시 (Toast/Snackbar 등)
    }
  }

  /**
   * 대기 고객 수 업데이트 (WebSocket 이벤트 핸들러)
   * @param {number} count - 새로운 대기 고객 수
   */
  const updateWaitingCustomers = (count) => {
    waitingCustomers.value = count
  }

  // Todo 목록 조회 (백엔드 연동 시 사용)
  const fetchTodos = async () => {
    try {
      // TODO: 백엔드 API 연동 시 주석 해제
      // const response = await fetch('/api/v1/users/me/todos', {
      //   method: 'GET',
      //   headers: {
      //     'Content-Type': 'application/json',
      //     // 'Authorization': `Bearer ${token}` // 인증 토큰이 필요한 경우
      //   }
      // })
      // if (!response.ok) throw new Error('Todo 목록을 불러오지 못했습니다')
      // const data = await response.json()
      // todos.value = data

      // 현재는 localStorage에서 로드
      console.log('fetchTodos - localStorage 사용 중 (백엔드 연동 대기)')
    } catch (error) {
      console.error('Todo 목록 조회 실패:', error)
    }
  }

  // Todo 완료 상태 토글
  const toggleTodo = async (id) => {
    const todo = todos.value.find(t => t.id === id)
    if (todo) {
      todo.completed = !todo.completed

      // TODO: 백엔드 API 연동 시 주석 해제
      // try {
      //   const response = await fetch(`/api/v1/users/me/todos/${id}`, {
      //     method: 'PUT',
      //     headers: {
      //       'Content-Type': 'application/json',
      //       // 'Authorization': `Bearer ${token}` // 인증 토큰이 필요한 경우
      //     },
      //     body: JSON.stringify({
      //       text: todo.text,
      //       completed: todo.completed
      //     })
      //   })
      //   if (!response.ok) throw new Error('Todo 업데이트 실패')
      // } catch (error) {
      //   console.error('Todo 상태 변경 실패:', error)
      //   // 실패 시 원래 상태로 되돌리기
      //   todo.completed = !todo.completed
      // }
    }
  }

  // Todo 추가
  const addTodo = async (text) => {
    const newTodo = {
      id: crypto.randomUUID(),
      text,
      completed: false
    }

    // 낙관적 업데이트 (UI 먼저 업데이트)
    todos.value.push(newTodo)

    // TODO: 백엔드 API 연동 시 주석 해제
    // try {
    //   const response = await fetch('/api/v1/users/me/todos', {
    //     method: 'POST',
    //     headers: {
    //       'Content-Type': 'application/json',
    //       // 'Authorization': `Bearer ${token}` // 인증 토큰이 필요한 경우
    //     },
    //     body: JSON.stringify({
    //       text,
    //       completed: false
    //     })
    //   })
    //   if (!response.ok) throw new Error('Todo 추가 실패')
    //   const savedTodo = await response.json()
    //   // 서버에서 받은 ID로 업데이트
    //   const index = todos.value.findIndex(t => t.id === newTodo.id)
    //   if (index !== -1) {
    //     todos.value[index] = savedTodo
    //   }
    // } catch (error) {
    //   console.error('Todo 추가 실패:', error)
    //   // 실패 시 추가한 항목 제거
    //   todos.value = todos.value.filter(t => t.id !== newTodo.id)
    // }
  }

  // Todo 삭제
  const deleteTodo = async (id) => {
    // 낙관적 업데이트를 위해 삭제 전 백업
    const originalTodos = [...todos.value]
    todos.value = todos.value.filter(t => t.id !== id)

    // TODO: 백엔드 API 연동 시 주석 해제
    // try {
    //   const response = await fetch(`/api/v1/users/me/todos/${id}`, {
    //     method: 'DELETE',
    //     headers: {
    //       // 'Authorization': `Bearer ${token}` // 인증 토큰이 필요한 경우
    //     }
    //   })
    //   if (!response.ok) throw new Error('Todo 삭제 실패')
    // } catch (error) {
    //   console.error('Todo 삭제 실패:', error)
    //   // 실패 시 원래 상태로 되돌리기
    //   todos.value = originalTodos
    // }
  }

  const store = {
    // State
    userName,
    stressIndex,
    weeklyPerformance,
    totalCallTime,
    customerSatisfaction,
    waitingCustomers,
    consultationStatus,
    todos,
    // Computed
    formattedCallTime,
    // Actions
    fetchDashboardData,
    fetchWeeklyPerformance,
    fetchStats,
    fetchWaitingCustomers,
    updateWaitingCustomers,
    fetchTodos,
    toggleTodo,
    addTodo,
    deleteTodo,
    refreshStressData
  }

  // 개발 환경에서 콘솔 테스트용으로 store 노출
  if (import.meta.env.DEV) {
    window.__DEV_DASHBOARD_STORE__ = store
  }

  return store
})
