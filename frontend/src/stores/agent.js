import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

// 스트레스 임계값 상수
const STRESS_THRESHOLDS = {
  HIGH: 67,
  MEDIUM: 34
}

// 스트레스 색상 상수
const STRESS_COLORS = {
  high: '#ef4444',    // red-500
  medium: '#eab308',  // yellow-500
  low: '#22c55e'      // green-500
}

// 에너지 변화 속도 (stressLevel 증감/초)
// stressLevel 0 → 100까지 소요 시간 또는 100 → 0까지 회복 시간
const ENERGY_CHANGE_RATES = {
  AVAILABLE: 0.028,  // 상담 대기 중: +0.028/초 → 1시간(3600초)에 완전 소진
  IN_CALL: 0.014,    // 통화 중: +0.014/초 → 2시간(7200초)에 완전 소진
  REST: -0.5         // 휴식 중: -0.5/초 → 약 3분 20초(200초)에 완전 회복
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
  const stressLevel = ref(null) // 스트레스 지수 (0-100), null이면 초기화 전
  const consecutiveCalls = ref(0)
  const totalCallTime = ref(0)
  const currentStatus = ref('REST') // 현재 상태: AVAILABLE, IN_CALL, REST

  // 에너지 애니메이션 타이머
  let energyAnimationInterval = null
  let isInitialized = false // 초기 데이터 로드 완료 여부

  // 통화 가능 여부
  const isAvailable = computed(() => {
    return agentInfo.value.status === 'available' && agentInfo.value.isAuthenticated
  })

  // 스트레스 상태 계산 (3단계: 녹색/노란색/빨간색)
  const stressStatus = computed(() => {
    const level = stressLevel.value
    if (level >= STRESS_THRESHOLDS.HIGH) return 'high' // 빨간색
    if (level >= STRESS_THRESHOLDS.MEDIUM) return 'medium' // 노란색
    return 'low' // 녹색
  })

  // 스트레스 색상
  const stressColor = computed(() => STRESS_COLORS[stressStatus.value])

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

  // 스트레스 지수 조회 (백엔드 연동)
  const fetchStressLevel = async () => {
    try {
      // TODO: 백엔드 API 연동 시 주석 해제
      // NOTE: 백엔드 팀과 API 응답 필드명 협의 필요
      // 예상 응답 형식 1: { "stressLevel": 0-100 }
      // 예상 응답 형식 2: { "stress": 0-100 }
      // 현재는 data.stressLevel || data.stress로 둘 다 처리하도록 구현되어 있으나,
      // 백엔드 API 스펙이 확정되면 필드명을 하나로 통일하는 것을 권장합니다.
      //
      // const response = await fetch('/api/v1/users/me/stress', {
      //   method: 'GET',
      //   headers: {
      //     'Content-Type': 'application/json',
      //     // 'Authorization': `Bearer ${token}` // 인증 토큰이 필요한 경우
      //   }
      // })
      // if (!response.ok) throw new Error('스트레스 지수를 불러오지 못했습니다')
      // const data = await response.json()
      // stressLevel.value = data.stressLevel || data.stress || 0

      // 현재는 더미 데이터 사용 (localStorage)
      console.log('fetchStressLevel - API 연동 대기 중')
    } catch (error) {
      console.error('스트레스 지수 조회 실패:', error)
    }
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

  // 에너지 자동 증감 애니메이션 시작
  const startEnergyAnimation = () => {
    // 기존 타이머가 있으면 정리
    stopEnergyAnimation()

    // 에너지 증감 애니메이션 (1초마다)
    energyAnimationInterval = setInterval(() => {
      // stressLevel이 초기화되지 않았으면 스킵
      if (stressLevel.value === null) {
        console.log('[AgentStore] 에너지 레벨 초기화 대기 중...')
        return
      }

      const rate = ENERGY_CHANGE_RATES[currentStatus.value] || 0

      // stressLevel 업데이트 (0-100 범위 유지)
      stressLevel.value = Math.max(0, Math.min(100, stressLevel.value + rate))

      console.log(`[AgentStore] 에너지 변화 - Status: ${currentStatus.value}, StressLevel: ${stressLevel.value.toFixed(1)}`)
    }, 1000)

    console.log('[AgentStore] 에너지 애니메이션 시작')
  }

  // 에너지 애니메이션 중지
  const stopEnergyAnimation = () => {
    if (energyAnimationInterval) {
      clearInterval(energyAnimationInterval)
      energyAnimationInterval = null
    }
    console.log('[AgentStore] 에너지 애니메이션 중지')
  }

  return {
    // State
    agentInfo,
    stressLevel,
    consecutiveCalls,
    totalCallTime,
    currentStatus,
    // Getters
    isAvailable,
    stressStatus,
    stressColor,
    // Actions
    login,
    logout,
    updateStatus,
    fetchStressLevel,
    updateStressLevel,
    resetStats,
    incrementCallStats,
    startEnergyAnimation,
    stopEnergyAnimation
  }
})
