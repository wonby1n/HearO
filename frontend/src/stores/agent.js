import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

// 에너지 임계값 상수 (낮을수록 위험)
const ENERGY_THRESHOLDS = {
  LOW: 33,      // 33 이하 = 위험 (빨간색)
  MEDIUM: 66    // 33-66 = 보통 (노란색), 67 이상 = 좋음 (녹색)
}

// 에너지 색상 상수
const ENERGY_COLORS = {
  high: '#22c55e',    // green-500 (에너지 높음 = 좋음)
  medium: '#eab308',  // yellow-500 (에너지 보통)
  low: '#ef4444'      // red-500 (에너지 낮음 = 위험)
}

// 에너지 변화 속도 (energyLevel 증감/초)
// 백엔드와 동기화: AVAILABLE/IN_CALL 분당 -0.5, REST 분당 +1.5
const ENERGY_CHANGE_RATES = {
  AVAILABLE: -0.5 / 60,  // 분당 -0.5 소진 (백엔드 동기화)
  IN_CALL: -0.5 / 60,    // 분당 -0.5 소진 (백엔드 동기화)
  REST: 1.5 / 60         // 분당 +1.5 회복 (백엔드 동기화)
}

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
  const energyLevel = ref(null) // 에너지 지수 (0-100), null이면 초기화 전
  const consecutiveCalls = ref(0)
  const totalCallTime = ref(0)
  const currentStatus = ref('REST') // 현재 상태: AVAILABLE, IN_CALL, REST

  // 에너지 애니메이션 타이머
  let energyAnimationInterval = null
  let isInitialized = false // 초기 데이터 로드 완료 여부

  // 통화 가능 여부
  const isAvailable = computed(() => {
    return agentInfo.value.status === 'AVAILABLE' && agentInfo.value.isAuthenticated
  })

  // 에너지 상태 계산 (3단계: 녹색/노란색/빨간색)
  const energyStatus = computed(() => {
    const level = energyLevel.value
    if (level <= ENERGY_THRESHOLDS.LOW) return 'low' // 빨간색 (에너지 낮음)
    if (level <= ENERGY_THRESHOLDS.MEDIUM) return 'medium' // 노란색 (에너지 보통)
    return 'high' // 녹색 (에너지 높음)
  })

  // 에너지 색상
  const energyColor = computed(() => ENERGY_COLORS[energyStatus.value])

  // 상담원 로그인
  const login = (userData) => {
    agentInfo.value = {
      ...agentInfo.value,
      ...userData,
      isAuthenticated: true,
      status: 'AVAILABLE'
    }
  }

  // 상담원 로그아웃
  const logout = () => {
    agentInfo.value = {
      id: null,
      name: '',
      email: '',
      status: 'REST',
      isAuthenticated: false
    }
    resetStats()
  }

  // 상태 변경
  const updateStatus = (newStatus) => {
    agentInfo.value.status = newStatus
  }

  // 에너지 지수 업데이트
  const updateEnergyLevel = (level) => {
    energyLevel.value = level
  }

  // 통계 초기화
  const resetStats = () => {
    energyLevel.value = null
    consecutiveCalls.value = 0
    totalCallTime.value = 0
  }

  // 통화 완료 후 통계 업데이트
  const incrementCallStats = (duration) => {
    consecutiveCalls.value++
    totalCallTime.value += duration
  }

  // 에너지 자동 증감 애니메이션 시작
  const startEnergyAnimation = () => {
    // 기존 타이머가 있으면 정리
    stopEnergyAnimation()

    // 에너지 증감 애니메이션 (1초마다)
    energyAnimationInterval = setInterval(() => {
      // energyLevel이 초기화되지 않았으면 스킵
      if (energyLevel.value === null) {
        return
      }

      const rate = ENERGY_CHANGE_RATES[currentStatus.value] || 0

      // energyLevel 업데이트 (0-100 범위 유지)
      energyLevel.value = Math.max(0, Math.min(100, energyLevel.value + rate))
    }, 1000)

  }

  // 에너지 애니메이션 중지
  const stopEnergyAnimation = () => {
    if (energyAnimationInterval) {
      clearInterval(energyAnimationInterval)
      energyAnimationInterval = null
    }
  }

  return {
    // State
    agentInfo,
    energyLevel,
    consecutiveCalls,
    totalCallTime,
    currentStatus,
    // Getters
    isAvailable,
    energyStatus,
    energyColor,
    // Actions
    login,
    logout,
    updateStatus,
    updateEnergyLevel,
    resetStats,
    incrementCallStats,
    startEnergyAnimation,
    stopEnergyAnimation
  }
})
