<template>
  <div class="client-call-view min-h-screen bg-gradient-to-b from-green-50 to-gray-50">
    <!-- 통화 헤더 -->
    <div class="call-header bg-white shadow-sm p-4">
      <div class="container mx-auto flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
            <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
            </svg>
          </div>
          <div>
            <h2 class="text-lg font-semibold text-gray-900">고객 상담</h2>
            <p class="text-sm text-gray-500">
              {{ callStore.isInCall ? '상담원과 연결됨' : callStore.isConnecting ? '연결 중...' : '대기 중' }}
            </p>
          </div>
        </div>
        <div class="flex items-center gap-4">
          <!-- 통화 시간 -->
          <div class="text-right">
            <p class="text-2xl font-mono font-semibold text-gray-900">
              {{ formattedCallDuration }}
            </p>
            <p class="text-xs text-gray-500">통화 시간</p>
          </div>
          <!-- 통화 상태 표시 -->
          <div class="flex items-center gap-2">
            <div
              :class="[
                'w-3 h-3 rounded-full',
                callStore.isInCall ? 'bg-green-500 animate-pulse' :
                callStore.isConnecting ? 'bg-yellow-500 animate-pulse' :
                'bg-gray-400'
              ]"
            ></div>
            <span class="text-sm text-gray-600">
              {{ callStore.isInCall ? '연결됨' : callStore.isConnecting ? '연결 중...' : '대기' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 메인 콘텐츠 영역 -->
    <div class="container mx-auto py-6 px-4">
      <div class="max-w-2xl mx-auto">
        <!-- 대기 화면 -->
        <div v-if="!callStore.isInCall && !callStore.isConnecting" class="card text-center py-12">
          <div class="w-24 h-24 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <svg class="w-12 h-12 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">상담원 연결 대기 중</h3>
          <p class="text-gray-600 mb-6">잠시만 기다려 주세요. 곧 상담원이 연결됩니다.</p>

          <!-- 대기 순번 표시 -->
          <div v-if="queuePosition > 0" class="bg-blue-50 rounded-lg p-4 mb-6">
            <p class="text-sm text-blue-600">현재 대기 순번</p>
            <p class="text-3xl font-bold text-blue-700">{{ queuePosition }}번</p>
          </div>

          <button
            @click="startCall"
            class="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition font-medium"
          >
            상담 시작
          </button>
        </div>

        <!-- 연결 중 화면 -->
        <div v-else-if="callStore.isConnecting" class="card text-center py-12">
          <div class="w-24 h-24 bg-yellow-100 rounded-full flex items-center justify-center mx-auto mb-6 animate-pulse">
            <svg class="w-12 h-12 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">상담원 연결 중...</h3>
          <p class="text-gray-600">잠시만 기다려 주세요.</p>
        </div>

        <!-- 통화 중 화면 -->
        <div v-else class="space-y-6">
          <!-- 상담원 정보 -->
          <div class="card">
            <div class="flex items-center gap-4">
              <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
                <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <div>
                <h3 class="text-lg font-semibold text-gray-900">상담원</h3>
                <p class="text-sm text-green-600">통화 중</p>
              </div>
            </div>
          </div>

          <!-- 안내 메시지 -->
          <div class="card bg-blue-50 border border-blue-200">
            <p class="text-sm text-blue-800">
              상담원과 연결되었습니다. 문의 사항을 말씀해 주세요.
            </p>
          </div>

          <!-- 내 정보 확인 -->
          <div class="card">
            <h4 class="text-sm font-semibold text-gray-700 mb-3">접수 정보</h4>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-600">제품명</span>
                <span class="font-medium">{{ productName }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">증상</span>
                <span class="font-medium">{{ symptom }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 통화 컨트롤 버튼 (통화 중일 때만 표시) -->
    <ClientCallControls
      v-if="callStore.isInCall"
      :is-muted="isMuted"
      @mute-changed="handleMuteChanged"
      @call-ended="handleCallEnded"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCallStore } from '@/stores/call'
import { useCustomerStore } from '@/stores/customer'
import { useLiveKit } from '@/composables/useLiveKit'
import ClientCallControls from '@/components/client/ClientCallControls.vue'

const router = useRouter()
const callStore = useCallStore()
const customerStore = useCustomerStore()

// LiveKit composable
const {
  room,
  isConnected,
  isConnecting,
  isMuted,
  error: livekitError,
  connectionState,
  connect,
  disconnect,
  enableMicrophone,
  toggleMute,
  startAudioPlayback
} = useLiveKit({
  onTrackSubscribed: (track, publication, participant) => {
    console.log('[Client] 원격 오디오 트랙 수신:', participant.identity)
  },
  onDisconnected: (reason) => {
    console.log('[Client] 연결 해제됨:', reason)
    handleDisconnected(reason)
  },
  onError: (err) => {
    console.error('[Client] LiveKit 에러:', err)
    callStore.setConnectionError(err.message)
  }
})

// 통화 시간 타이머
const callDuration = ref(0)
let timerInterval = null

// 대기 순번
const queuePosition = ref(0)

// 제품 정보 computed
const productName = computed(() => customerStore.currentCustomer.productInfo?.productName || '-')
const symptom = computed(() => customerStore.currentCustomer.issue?.symptom || '-')

// 통화 시간 포맷팅
const formattedCallDuration = computed(() => {
  const hours = Math.floor(callDuration.value / 3600)
  const minutes = Math.floor((callDuration.value % 3600) / 60)
  const seconds = callDuration.value % 60

  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

/**
 * 상담 시작 (테스트용)
 */
const startCall = async () => {
  // TODO: 실제로는 백엔드 API를 통해 대기열에 등록하고 토큰을 받아야 함
  callStore.startCall({
    id: `client-call-${Date.now()}`,
    customerId: customerStore.currentCustomer.id,
    roomToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJBUElqZEVWR0NtQkZQSnUiLCJleHAiOjE3NjkyNTA2MDcsInN1YiI6InVzZXIxMjMiLCJ2aWRlbyI6eyJyb29tSm9pbiI6dHJ1ZSwicm9vbSI6InJvb21BIn0sInNpcCI6e319.NKJkwGUX-vnZx9gXPSZB-Pl16g3gKtz41d6qjrQdwQw'
  })
}

/**
 * LiveKit 룸 연결
 */
const connectToRoom = async (serverUrl, token) => {
  try {
    callStore.initiateCall({
      id: `call-${Date.now()}`,
      customerId: customerStore.currentCustomer.id,
      roomToken: token,
      serverUrl: serverUrl
    })

    const connectedRoom = await connect(serverUrl, token)
    callStore.setLivekitRoom(connectedRoom)

    const audioTrack = await enableMicrophone()
    callStore.setAudioTrack(audioTrack)

    callStore.activateCall()

    console.log('[Client] 통화 연결 완료')
  } catch (err) {
    console.error('[Client] 통화 연결 실패:', err)
    callStore.setConnectionError(err.message)
  }
}

/**
 * 연결 해제 핸들러
 */
const handleDisconnected = (reason) => {
  if (callStore.isInCall) {
    console.warn('[Client] 예상치 못한 연결 해제:', reason)
  }
}

// 음소거 변경 핸들러
const handleMuteChanged = async (muted) => {
  console.log('[Client] 음소거 상태:', muted)
  await toggleMute()
}

// 통화 종료 핸들러
const handleCallEnded = async (callData) => {
  console.log('[Client] 통화 종료:', callData)

  if (timerInterval) {
    clearInterval(timerInterval)
  }

  await disconnect()

  // 만족도 조사 페이지로 이동 (나중에 구현)
  alert('통화가 종료되었습니다. 이용해 주셔서 감사합니다.')
}

// 컴포넌트 마운트 시 초기화
onMounted(async () => {
  // 테스트용 고객 정보 설정
  if (!customerStore.hasCustomerInfo) {
    customerStore.setCustomerInfo({
      id: 'customer-001',
      name: '홍길동',
      phone: '010-1234-5678',
      productInfo: {
        modelName: 'ABC-1000',
        productName: '세탁기',
        purchaseDate: '2024-01-15',
        warrantyPeriod: '2026-01-15'
      },
      issue: {
        symptom: '세탁기가 작동하지 않습니다',
        errorCode: 'E01'
      }
    })
  }

  // 통화 시간 타이머
  timerInterval = setInterval(() => {
    if (callStore.isInCall) {
      callDuration.value++
    }
  }, 1000)
})

// 컴포넌트 언마운트 시 정리
onUnmounted(async () => {
  if (timerInterval) {
    clearInterval(timerInterval)
  }

  if (isConnected.value) {
    await disconnect()
  }
})
</script>

<style scoped>
/* 추가 스타일이 필요하면 여기에 */
</style>
