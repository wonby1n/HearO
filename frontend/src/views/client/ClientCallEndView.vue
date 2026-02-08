<template>
  <div class="client-call-end-view">
    <div class="main-content">
      <!-- 폭언 종료: 경고 아이콘 -->
      <div v-if="isAutoTerminated" class="warning-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
          />
        </svg>
      </div>
      <!-- 정상 종료: 전화기X 아이콘 -->
      <div v-else class="call-ended-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <!-- 전화기 아이콘 -->
          <path
            d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72 12.84 12.84 0 00.7 2.81 2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45 12.84 12.84 0 002.81.7A2 2 0 0122 16.92z" />
          <!-- X 표시 -->
          <line x1="15" y1="9" x2="9" y2="15" stroke-linecap="round" />
          <line x1="9" y1="9" x2="15" y2="15" stroke-linecap="round" />
        </svg>
      </div>

      <!-- 상담 종료 메시지 -->
      <h1 class="end-message">
        {{ isAutoTerminated ? '부적절한 언어 사용으로 상담이 종료되었습니다' : '상담이 종료되었습니다' }}
      </h1>

      <!-- 폭언 종료 시 부가 메시지 -->
      <p v-if="isAutoTerminated" class="sub-message">
        원활한 상담 진행을 위해 정중한 대화를 부탁드립니다.
      </p>

      <!-- 통화 시간 -->
      <p class="call-duration">{{ formattedDuration }}</p>
    </div>
    <div class="call-reconnection">
      <p @click="handleReconnect">
        {{ isAutoTerminated ? '재연결하기' : '같은 내용으로 재연결하기' }}
      </p>
    </div>
    <!-- 하단 버튼 영역 -->
    <div class="button-section">
      <button type="button" class="next-button" @click="handleNext">
        {{ isAutoTerminated ? '종료하기' : '다음으로' }}
      </button>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCallStore } from '@/stores/call'
import { registerQueue } from '@/services/customerService'
import { CALL_END_REDIRECT_DELAY_MS } from '@/constants/call'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const callStore = useCallStore()

const isReconnecting = ref(false)

// 폭언 종료 여부 판별
const isAutoTerminated = computed(() => route.query.autoTerminated === 'true')

// 라우트 쿼리에서 통화 시간(초) 가져오기
const callDuration = computed(() => {
  return parseInt(route.query.duration) || 0
})

// 통화 시간 포맷팅 (mm:ss)
const formattedDuration = computed(() => {
  const minutes = Math.floor(callDuration.value / 60)
  const seconds = callDuration.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

// 설정된 시간 후 만족도 조사 페이지로 이동 (정상 종료 시에만)
let redirectTimer = null

onMounted(() => {
  if (!isAutoTerminated.value) {
    redirectTimer = setTimeout(() => {
      router.push({
        name: 'client-review',
        query: { consultationId: route.query.consultationId }
      })
    }, CALL_END_REDIRECT_DELAY_MS)
  }
})

onUnmounted(() => {
  if (redirectTimer) {
    clearTimeout(redirectTimer)
  }
})

// 다음/종료 버튼 클릭 핸들러
const handleNext = () => {
  if (isAutoTerminated.value) {
    // 폭언 종료: 초기 상담 신청 화면으로 이동
    router.push({ name: 'client-landing' })
  } else {
    // 정상 종료: 만족도 조사로 이동
    router.push({
      name: 'client-review',
      query: { consultationId: route.query.consultationId }
    })
  }
}

// 재연결 버튼 클릭 핸들러
const handleReconnect = async () => {
  // 폭언 종료 시: 즉시 재연결
  if (isAutoTerminated.value) {
    if (isReconnecting.value) return

    try {
      isReconnecting.value = true
      console.log('[ClientCallEnd] 폭언 종료 - 즉시 재연결 시작')

      // 1. sessionStorage에서 고객 정보 읽기
      const productId = sessionStorage.getItem('clientProductId')
      const clientName = sessionStorage.getItem('clientName')
      const clientPhone = sessionStorage.getItem('clientPhone')

      if (!productId || !clientName || !clientPhone) {
        throw new Error('고객 정보를 찾을 수 없습니다. 처음부터 다시 시작해주세요.')
      }

      // 2. 자동 로그인으로 새 토큰 발급
      console.log('[ClientCallEnd] 자동 로그인 시작')
      const loginResponse = await axios.post('/api/v1/auth/customer/login', {
        name: clientName,
        phone: clientPhone
      })

      const { accessToken, customerId } = loginResponse.data

      // 새 토큰을 sessionStorage에 저장
      sessionStorage.setItem('customerAccessToken', accessToken)
      sessionStorage.setItem('clientCustomerId', String(customerId))
      console.log('[ClientCallEnd] 자동 로그인 성공, 새 토큰 발급됨')

      // 3. 같은 제품으로 새 상담 요청 생성
      console.log('[ClientCallEnd] 새 상담 요청 생성 (productId:', productId, ')')
      const result = await registerQueue({
        symptom: '이전 상담 내용 재연결',
        productId: parseInt(productId)
      })

      console.log('[ClientCallEnd] 재연결 성공:', result)

      // 4. callStore에 저장
      callStore.initiateCall({
        registrationId: result.registrationId,
        customerId: result.customerId,
        roomToken: null,
        serverUrl: null
      })

      // 5. 대기 화면으로 이동
      router.push({
        name: 'client-waiting',
        params: {
          registrationId: result.registrationId
        },
        query: {
          waitingRank: result.waitingRank,
          estimatedWaitMinutes: result.estimatedWaitMinutes,
          websocketTopic: result.websocketTopic,
          reconnected: 'true' // 재연결 표시
        }
      })
    } catch (error) {
      console.error('[ClientCallEnd] 재연결 실패:', error)
      alert('재연결에 실패했습니다. 처음부터 다시 시도해주세요.')
      // 실패 시 처음 화면으로
      router.push({ name: 'client-landing' })
    } finally {
      isReconnecting.value = false
    }
  } else {
    // 정상 종료 시: 재연결 페이지로 이동 (기존 로직 유지)
    router.push({
      name: 'client-reconnect',
      query: { consultationId: route.query.consultationId }
    })
  }
}
</script>

<style scoped>
.client-call-end-view {
  min-height: 100vh;
  max-width: 430px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  display: flex;
  flex-direction: column;
  position: relative;
}


.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  gap: 16px;
}

/* 종료된 통화 아이콘 */
.call-ended-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.call-ended-icon svg {
  width: 48px;
  height: 48px;
}

/* 폭언 종료 경고 아이콘 */
.warning-icon {
  width: 72px;
  height: 72px;
  background: #fef2f2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #dc2626;
}

.warning-icon svg {
  width: 40px;
  height: 40px;
}

/* 상담 종료 메시지 */
.end-message {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  margin-top: 8px;
  text-align: center;
  line-height: 1.4;
}

/* 폭언 종료 부가 메시지 */
.sub-message {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  text-align: center;
}

/* 통화 시간 */
.call-duration {
  font-size: 16px;
  color: #9ca3af;
  margin: 0;
  font-family: 'SF Mono', Monaco, monospace;
  letter-spacing: 1px;
}


.next-button {
  width: 100%;
  padding: 16px;
  border: none;
  border-radius: 12px;
  background: #3b82f6;
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  cursor: pointer;
  transition: background-color 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.next-button:hover {
  background: #2563eb;
}

.button-section {
  padding: 16px 24px 32px;
}


.call-reconnection {
  width: 100%;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #646464;
  cursor: pointer;
  transition: background-color 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

</style>
