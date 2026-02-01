import { ref, onUnmounted } from 'vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

export function useMatchingNotification(options = {}) {
  const { customerId, counselorId, onMatched } = options

  const client = ref(null)
  const isConnected = ref(false)
  const matchData = ref(null)
  const isMatched = ref(false) // 중복 매칭 방지 플래그

  // STOMP 클라이언트 연결
  const connect = () => {
    client.value = new Client({
      webSocketFactory: () => new SockJS('/ws/queue'),
      reconnectDelay: 5000,

      onConnect: () => {
        isConnected.value = true
        console.log('[STOMP] 연결 성공')

        // 고객용 매칭 알림 구독
        if (customerId) {
          client.value.subscribe(`/topic/queue-rank/${customerId}`, (message) => {
            const data = JSON.parse(message.body)

            if (data.status === 'MATCHED') {
              // 중복 매칭 방지
              if (isMatched.value) {
                console.warn('[STOMP] 이미 매칭되었으므로 중복 메시지 무시:', data)
                return
              }

              console.log('[STOMP] 매칭 완료:', data)
              isMatched.value = true
              matchData.value = data
              onMatched?.(data)
            }
          })
        }

        // 상담원용 매칭 알림 구독
        if (counselorId) {
          client.value.subscribe(`/topic/counselor/${counselorId}`, (message) => {
            const data = JSON.parse(message.body)

            if (data.type === 'MATCH_ASSIGNED') {
              // 중복 매칭 방지
              if (isMatched.value) {
                console.warn('[STOMP] 이미 매칭되었으므로 중복 메시지 무시:', data)
                return
              }

              console.log('[STOMP] 매칭 배정:', data)
              isMatched.value = true
              matchData.value = data
              onMatched?.(data)
            }
          })
        }
      },

      onDisconnect: () => {
        isConnected.value = false
        console.log('[STOMP] 연결 해제')
      },

      onStompError: (error) => {
        console.error('[STOMP] 에러:', error)
      }
    })

    client.value.activate()
  }

  const disconnect = () => {
    if (client.value) {
      client.value.deactivate()
      client.value = null
    }
    isMatched.value = false // 플래그 리셋
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    isConnected,
    matchData,
    connect,
    disconnect
  }
}