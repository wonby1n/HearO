import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAgentStore = defineStore('agent', () => {
  // 상담원 정보
  const agentInfo = ref({
    id: null,
    name: '',
    email: '',
    status: 'offline', // offline, available, busy, break
    isAuthenticated: false
  })

  // 상담원 상태
  const stressLevel = ref(0) // 0: 양호, 1: 주의, 2: 위험
  const consecutiveCalls = ref(0)
  const totalCallTime = ref(0)

  // 통화 가능 여부
  const isAvailable = computed(() => {
    return agentInfo.value.status === 'available' && agentInfo.value.isAuthenticated
  })

  // 상담원 로그인
  const login = (userData) => {
    agentInfo.value = {
      ...agentInfo.value,
      ...userData,
      isAuthenticated: true,
      status: 'available'
    }
  }

  // 상담원 로그아웃
  const logout = () => {
    agentInfo.value = {
      id: null,
      name: '',
      email: '',
      status: 'offline',
      isAuthenticated: false
    }
    resetStats()
  }

  // 상태 변경
  const updateStatus = (newStatus) => {
    agentInfo.value.status = newStatus
  }

  // 스트레스 지수 업데이트
  const updateStressLevel = (level) => {
    stressLevel.value = level
  }

  // 통계 초기화
  const resetStats = () => {
    stressLevel.value = 0
    consecutiveCalls.value = 0
    totalCallTime.value = 0
  }

  // 통화 완료 후 통계 업데이트
  const incrementCallStats = (duration) => {
    consecutiveCalls.value++
    totalCallTime.value += duration
  }

  return {
    // State
    agentInfo,
    stressLevel,
    consecutiveCalls,
    totalCallTime,
    // Getters
    isAvailable,
    // Actions
    login,
    logout,
    updateStatus,
    updateStressLevel,
    resetStats,
    incrementCallStats
  }
})
