<template>
  <div class="client-reconnect-view">
    <div class="main-content">
      <!-- 느낌표 아이콘 -->
      <div class="icon-container">
        <div class="icon-circle">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <circle cx="12" cy="16" r="0.5" fill="currentColor" />
          </svg>
        </div>
      </div>

      <!-- 메시지 -->
      <h1 class="title">상담이 일찍 종료되었나요?</h1>
      <p class="description">
        연결이 끊어졌거나 추가 상담이 필요하시면<br />
        아래 버튼을 눌러 다시 연결할 수 있습니다.
      </p>
    </div>

    <!-- 하단 버튼 -->
    <div class="button-section">
      <button
        type="button"
        class="reconnect-button"
        @click="handleReconnect"
        :disabled="isReconnecting"
        :class="{ 'reconnecting': isReconnecting }"
      >
        <svg
          v-if="!isReconnecting"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          class="refresh-icon"
        >
          <path d="M1 4v6h6" />
          <path d="M23 20v-6h-6" />
          <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10M23 14l-4.64 4.36A9 9 0 0 1 3.51 15" />
        </svg>
        <svg
          v-else
          class="spinner"
          viewBox="0 0 24 24"
          fill="none"
        >
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" opacity="0.25"/>
          <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        {{ isReconnecting ? '재연결 중...' : '상담 재연결하기' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCallStore } from '@/stores/call'
import { registerQueue } from '@/services/customerService'

const router = useRouter()
const route = useRoute()
const callStore = useCallStore()

const isReconnecting = ref(false)

// 상담 재연결
const handleReconnect = async () => {
  if (isReconnecting.value) return

  try {
    isReconnecting.value = true
    console.log('[ClientReconnect] 재연결 시작')

    // 1. sessionStorage에서 제품 정보 읽기
    const productId = sessionStorage.getItem('clientProductId')
    if (!productId) {
      throw new Error('제품 정보를 찾을 수 없습니다. 처음부터 다시 시작해주세요.')
    }

    // 2. 같은 제품으로 새 상담 요청 생성
    console.log('[ClientReconnect] 새 상담 요청 생성 (productId:', productId, ')')
    const result = await registerQueue({
      symptom: '이전 상담 내용 재연결',
      productId: parseInt(productId)
    })

    console.log('[ClientReconnect] 재연결 성공:', result)

    // 3. callStore에 저장
    callStore.initiateCall({
      registrationId: result.registrationId,
      customerId: result.customerId,
      roomToken: null,
      serverUrl: null
    })

    // 4. 대기 화면으로 이동
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
    console.error('[ClientReconnect] 재연결 실패:', error)
    alert('재연결에 실패했습니다. 처음부터 다시 시도해주세요.')
    // 실패 시 처음 화면으로
    router.push({ name: 'client-landing' })
  } finally {
    isReconnecting.value = false
  }
}
</script>

<style scoped>
.client-reconnect-view {
  min-height: 100vh;
  max-width: 430px;
  margin: 0 auto;
  background: #ffffff;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 24px;
  text-align: center;
}

/* 아이콘 컨테이너 */
.icon-container {
  margin-bottom: 32px;
}

.icon-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-circle svg {
  width: 40px;
  height: 40px;
  color: #9ca3af;
}

/* 제목 */
.title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 16px 0;
}

/* 설명 */
.description {
  font-size: 14px;
  color: #9ca3af;
  margin: 0;
  line-height: 1.6;
}

/* 버튼 영역 */
.button-section {
  padding: 16px 24px 32px;
}

.reconnect-button {
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

.reconnect-button:hover:not(:disabled) {
  background: #2563eb;
}

.reconnect-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.reconnect-button.reconnecting {
  background: #60a5fa;
}

.refresh-icon {
  width: 20px;
  height: 20px;
}

.spinner {
  width: 20px;
  height: 20px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
