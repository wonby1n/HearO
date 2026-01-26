import { ref, onUnmounted } from 'vue'

/**
 * WebSocket 연결 관리 Composable
 * @param {string} url - WebSocket URL
 * @param {Object} options - 설정 옵션
 * @param {Function} options.onMessage - 메시지 수신 콜백
 * @param {Function} options.onOpen - 연결 성공 콜백
 * @param {Function} options.onClose - 연결 종료 콜백
 * @param {Function} options.onError - 에러 콜백
 * @param {number} options.reconnectDelay - 재연결 지연 시간 (ms, 기본 3000)
 * @param {number} options.maxReconnectAttempts - 최대 재연결 시도 횟수 (기본 5)
 */
export function useWebSocket(url, options = {}) {
  const {
    onMessage,
    onOpen,
    onClose,
    onError,
    reconnectDelay = 3000,
    maxReconnectAttempts = 5
  } = options

  const ws = ref(null)
  const isConnected = ref(false)
  const reconnectAttempts = ref(0)
  let reconnectTimer = null

  /**
   * WebSocket 연결
   */
  const connect = () => {
    try {
      ws.value = new WebSocket(url)

      ws.value.onopen = (event) => {
        isConnected.value = true
        reconnectAttempts.value = 0
        console.log('[WebSocket] 연결 성공:', url)
        onOpen?.(event)
      }

      ws.value.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          onMessage?.(data)
        } catch (error) {
          console.error('[WebSocket] 메시지 파싱 실패:', error)
          onMessage?.(event.data)
        }
      }

      ws.value.onclose = (event) => {
        isConnected.value = false
        console.log('[WebSocket] 연결 종료:', event.code, event.reason)
        onClose?.(event)

        // 자동 재연결 (정상 종료가 아닌 경우)
        if (event.code !== 1000 && reconnectAttempts.value < maxReconnectAttempts) {
          scheduleReconnect()
        }
      }

      ws.value.onerror = (error) => {
        console.error('[WebSocket] 에러:', error)
        onError?.(error)
      }
    } catch (error) {
      console.error('[WebSocket] 연결 실패:', error)
      onError?.(error)
    }
  }

  /**
   * 재연결 스케줄링
   */
  const scheduleReconnect = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
    }

    reconnectAttempts.value++
    console.log(
      `[WebSocket] 재연결 시도 ${reconnectAttempts.value}/${maxReconnectAttempts}...`
    )

    reconnectTimer = setTimeout(() => {
      connect()
    }, reconnectDelay)
  }

  /**
   * 메시지 전송
   */
  const send = (data) => {
    if (!ws.value || ws.value.readyState !== WebSocket.OPEN) {
      console.error('[WebSocket] 연결되지 않음')
      return false
    }

    try {
      const message = typeof data === 'string' ? data : JSON.stringify(data)
      ws.value.send(message)
      return true
    } catch (error) {
      console.error('[WebSocket] 메시지 전송 실패:', error)
      return false
    }
  }

  /**
   * 연결 종료
   */
  const disconnect = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }

    if (ws.value) {
      ws.value.close(1000, 'Client disconnect')
      ws.value = null
    }

    isConnected.value = false
  }

  // 컴포넌트 언마운트 시 자동 연결 해제
  onUnmounted(() => {
    disconnect()
  })

  return {
    ws,
    isConnected,
    connect,
    disconnect,
    send
  }
}
