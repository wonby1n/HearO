import { ref } from 'vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

export function useMatchingNotification(options = {}) {
  const { customerId, counselorId, onMatched, onRankUpdate } = options

  const client = ref(null)
  const isConnected = ref(false)
  const matchData = ref(null)
  const isMatched = ref(false) // 중복 매칭 방지 플래그

  // STOMP 클라이언트 연결
  const connect = () => {
    // 새 연결 시작 시 매칭 플래그 리셋
    isMatched.value = false

    client.value = new Client({
      webSocketFactory: () => new SockJS('/ws/queue'),
      reconnectDelay: 5000,

      onConnect: () => {
        isConnected.value = true
        console.log('[STOMP] 연결 성공')

        // 고객용 매칭 알림 구독
        if (customerId) {
          const topic = `/topic/queue-rank/${customerId}`
          console.log('[STOMP] 고객 매칭 알림 구독 시작:', topic)

          client.value.subscribe(topic, (message) => {
            console.log('[STOMP] 메시지 수신:', message.body)
            const data = JSON.parse(message.body)

            if (data.status === 'MATCHED') {
              // 중복 매칭 방지
              if (isMatched.value) {
                console.warn('[STOMP] 이미 매칭되었으므로 중복 메시지 무시:', data)
                return
              }

              // 자신의 customerId와 일치하는지 검증
              const myCustomerId = String(customerId)
              const matchedCustomerId = String(data.customerId)
              if (myCustomerId !== matchedCustomerId) {
                console.warn('[STOMP] 다른 고객의 매칭 메시지 무시:', {
                  myCustomerId,
                  matchedCustomerId
                })
                return
              }

              console.log('[STOMP] 매칭 완료:', data)
              isMatched.value = true
              matchData.value = data
              onMatched?.(data)
            } else if (data.status === 'WAITING' && data.rank !== undefined) {
              // 대기 순위 업데이트
              console.log('[STOMP] 순위 업데이트:', data.rank)
              onRankUpdate?.(data.rank)
            } else {
              console.log('[STOMP] 기타 상태:', data.status, data)
            }
          })

          console.log('[STOMP] 고객 매칭 알림 구독 완료')
        }

        // 상담원용 매칭 알림 구독
        if (counselorId) {
          console.log('[STOMP] 상담원 매칭 알림 구독 시작, counselorId:', counselorId)
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
          console.log('[STOMP] 상담원 매칭 알림 구독 완료')
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

  // Note: onUnmounted 제거
  // 이 composable은 setup() 밖에서 동적으로 생성되므로 lifecycle hooks 사용 불가
  // useCallConnection에서 명시적으로 disconnect() 호출하여 정리

  return {
    isConnected,
    matchData,
    connect,
    disconnect
  }
}