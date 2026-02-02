import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import axios from 'axios'
import { useAgentStore } from './agent'

export const useDashboardStore = defineStore('dashboard', () => {
  // --- 상태 (State) ---
  const userName = ref('')
  const energyIndex = ref(0)
  const waitingCustomers = ref(0)
  const customerSatisfaction = ref(0)
  const todos = ref([])
  
  const weeklyPerformance = ref([
    { day: 'Mon', calls: 0 },
    { day: 'Tue', calls: 0 },
    { day: 'Wed', calls: 0 },
    { day: 'Thu', calls: 0 },
    { day: 'Fri', calls: 0 }
  ])

  const totalCallTime = ref({ hours: 0, minutes: 0 })

  // localStorage에서 상담 상태 복원 (새로고침 시 유지)
  const savedConsultationStatus = localStorage.getItem('consultationStatus')
  const consultationStatus = ref({
    isActive: savedConsultationStatus ? JSON.parse(savedConsultationStatus).isActive : false,
    lastCallTime: 0
  })

  // 매칭 데이터 (모달 표시용)
  const matchedData = ref(null)

  // --- 연산 (Computed) ---
  const formattedCallTime = computed(() => {
    return `${totalCallTime.value.hours}:${String(totalCallTime.value.minutes).padStart(2, '0')}`
  })

  // --- 기능 (Actions) ---

  /**
   * 대시보드 종합 데이터 조회
   */
  const fetchDashboardData = async () => {
    try {
      const response = await axios.get('/api/v1/dashboard')
      if (response.data.isSuccess) {
        const data = response.data.data
        userName.value = data.userName || ''
        energyIndex.value = data.currentEnergy || 0
        customerSatisfaction.value = data.customerSatisfaction || 0

        if (data.currentEnergy !== undefined) {
          const agentStore = useAgentStore()
          // 백엔드의 currentEnergy는 이미 0-100 값이므로 그대로 사용
          // 항상 백엔드 값으로 덮어쓰기 (동기화)
          agentStore.energyLevel = data.currentEnergy
        }

        if (data.weeklyChart) {
          weeklyPerformance.value = data.weeklyChart.map(item => ({
            day: item.dayOfWeek,
            calls: item.count
          }))
        }

        if (data.totalDuration) {
          const [hours, minutes] = data.totalDuration.split(':').map(Number)
          totalCallTime.value = { hours, minutes }
        }
      }
    } catch (error) {
      console.error('[DashboardStore] 대시보드 데이터 조회 실패:', error)
    }
  }

  /**
   * Todo 목록 조회
   */
  const fetchTodos = async () => {
    try {
      const response = await axios.get('/api/v1/todos')
      if (response.data.isSuccess) {
        todos.value = response.data.data
      }
    } catch (error) {
      console.error('[DashboardStore] Todo 목록 조회 실패:', error)
    }
  }

  /**
   * Todo 추가
   */
  const addTodo = async (content) => {
    if (!content.trim()) return
    try {
      const response = await axios.post('/api/v1/todos', { content })
      if (response.data.isSuccess) {
        // 성공 시 리스트에 추가하거나 다시 불러오기
        if (response.data.data) {
          todos.value.push(response.data.data)
        } else {
          await fetchTodos()
        }
      }
    } catch (error) {
      console.error('[DashboardStore] Todo 추가 실패:', error)
    }
  }

  /**
   * Todo 토글 (체크 상태 변경)
   * PATCH /api/v1/todos/{todoId}/check
   */
  const toggleTodo = async (id) => {
    const todo = todos.value.find(t => t.id === id)
    if (!todo) return

    const originalState = todo.completed
    todo.completed = !todo.completed // 낙관적 업데이트

    try {
      const response = await axios.patch(`/api/v1/todos/${id}/check`)
      if (!response.data.isSuccess) {
        throw new Error('Toggle failed')
      }
    } catch (error) {
      console.error('[DashboardStore] Todo 토글 실패:', error)
      todo.completed = originalState // 롤백
    }
  }

  /**
   * Todo 내용 수정
   * PATCH /api/v1/todos/{todoId}
   */
  const updateTodo = async (id, newContent) => {
    const todo = todos.value.find(t => t.id === id)
    if (!todo || !newContent.trim()) return

    const originalContent = todo.content
    todo.content = newContent // 낙관적 업데이트

    try {
      const response = await axios.patch(`/api/v1/todos/${id}`, {
        content: newContent
      })
      if (!response.data.isSuccess) {
        throw new Error('Update failed')
      }
    } catch (error) {
      console.error('[DashboardStore] Todo 수정 실패:', error)
      todo.content = originalContent // 롤백
    }
  }

  /**
   * Todo 삭제
   */
  const deleteTodo = async (id) => {
    const originalTodos = [...todos.value]
    todos.value = todos.value.filter(t => t.id !== id)

    try {
      const response = await axios.delete(`/api/v1/todos/${id}`)
      if (!response.data.isSuccess) {
        throw new Error('Delete failed')
      }
    } catch (error) {
      console.error('[DashboardStore] Todo 삭제 실패:', error)
      todos.value = originalTodos
    }
  }

  /**
   * 에너지 데이터 부분 새로고침
   */
  const refreshEnergyData = async () => {
    try {
      const response = await axios.get('/api/v1/dashboard')
      if (response.data.isSuccess) {
        const data = response.data.data
        const agentStore = useAgentStore()
        if (data.currentEnergy !== undefined) {
          // 백엔드의 currentEnergy는 이미 0-100 값이므로 그대로 사용
          agentStore.energyLevel = data.currentEnergy
        }
      }
    } catch (error) {
      console.error('[DashboardStore] 에너지 새로고침 실패:', error)
    }
  }

  // consultationStatus 변경 시 localStorage에 저장 (새로고침 시 유지)
  watch(
    () => consultationStatus.value.isActive,
    (newValue) => {
      localStorage.setItem('consultationStatus', JSON.stringify({ isActive: newValue }))
      console.log('[DashboardStore] consultationStatus 저장:', newValue)
    }
  )

  /**
   * 매칭 데이터 설정 (모달 표시용)
   */
  const setMatchedData = (data) => {
    matchedData.value = data
    console.log('[DashboardStore] 매칭 데이터 저장:', data)
  }

  /**
   * 매칭 데이터 초기화
   */
  const clearMatchedData = () => {
    matchedData.value = null
  }

  return {
    userName,
    energyIndex,
    weeklyPerformance,
    totalCallTime,
    customerSatisfaction,
    waitingCustomers,
    consultationStatus,
    todos,
    matchedData,
    formattedCallTime,
    fetchDashboardData,
    fetchTodos,
    addTodo,
    toggleTodo,
    updateTodo,
    deleteTodo,
    refreshEnergyData,
    setMatchedData,
    clearMatchedData
  }
})