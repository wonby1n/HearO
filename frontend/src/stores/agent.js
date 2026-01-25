import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

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
  const stressLevel = ref(0) // 스트레스 지수 (0-100)
  const consecutiveCalls = ref(0)
  const totalCallTime = ref(0)

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

  return {
    // State
    agentInfo,
    stressLevel,
    consecutiveCalls,
    totalCallTime,
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
    incrementCallStats
  }
})
