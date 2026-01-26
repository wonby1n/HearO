import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  // 알림 목록 (화면에 표시될 알림들)
  const notifications = ref([])

  // 알림 ID 카운터
  let NOTIFICATION_ID_COUNTER = 0

  // 타이머 관리 맵 (메모리 누수 방지)
  const timers = new Map()

  /**
   * 알림 추가
   * @param {Object} options - 알림 옵션
   * @param {string} options.type - 알림 타입 ('profanity', 'abuse', 'warning', 'info', 'success')
   * @param {string} options.message - 알림 메시지
   * @param {number} options.duration - 표시 시간 (ms, 기본 3000ms)
   * @param {number} options.count - 카운트 (폭언/비속어 횟수 등)
   */
  const addNotification = (options) => {
    const {
      type = 'info',
      message,
      duration = 3000,
      count = null
    } = options

    // 중복 알림 방지: 같은 타입과 메시지의 알림이 이미 있으면 업데이트
    const existingIndex = notifications.value.findIndex(
      n => n.type === type && n.message === message
    )

    if (existingIndex !== -1) {
      const existingNotification = notifications.value[existingIndex]
      // 카운트 업데이트
      existingNotification.count = count
      // 기존 타이머 취소
      clearTimeout(timers.get(existingNotification.id))

      // 새 타이머 설정
      if (duration > 0) {
        const timerId = setTimeout(() => {
          removeNotification(existingNotification.id)
        }, duration)
        timers.set(existingNotification.id, timerId)
      }

      return existingNotification.id
    }

    // 새로운 알림 추가
    const id = ++NOTIFICATION_ID_COUNTER
    const notification = {
      id,
      type,
      message,
      count,
      createdAt: Date.now()
    }

    notifications.value.push(notification)

    // 자동 제거 타이머 설정
    if (duration > 0) {
      const timerId = setTimeout(() => {
        removeNotification(id)
      }, duration)
      timers.set(id, timerId)
    }

    return id
  }

  /**
   * 알림 제거
   * @param {number} id - 알림 ID
   */
  const removeNotification = (id) => {
    // 타이머 정리 (메모리 누수 방지)
    clearTimeout(timers.get(id))
    timers.delete(id)

    const index = notifications.value.findIndex(n => n.id === id)
    if (index !== -1) {
      notifications.value.splice(index, 1)
    }
  }

  /**
   * 모든 알림 제거
   */
  const clearAllNotifications = () => {
    // 모든 타이머 정리
    timers.forEach(timerId => clearTimeout(timerId))
    timers.clear()

    notifications.value = []
  }

  /**
   * 폭언 감지 알림
   * @param {number} count - 폭언 횟수
   */
  const notifyProfanity = (count) => {
    return addNotification({
      type: 'profanity',
      message: '폭언 감지',
      count: count,
      duration: 3000
    })
  }

  /**
   * 비속어 감지 알림
   */
  const notifyAbuse = () => {
    return addNotification({
      type: 'abuse',
      message: '비속어 감지',
      duration: 3000
    })
  }

  /**
   * 일반 경고 알림
   * @param {string} message - 경고 메시지
   */
  const notifyWarning = (message) => {
    return addNotification({
      type: 'warning',
      message: message,
      duration: 3000
    })
  }

  /**
   * 정보 알림
   * @param {string} message - 정보 메시지
   */
  const notifyInfo = (message) => {
    return addNotification({
      type: 'info',
      message: message,
      duration: 3000
    })
  }

  /**
   * 성공 알림
   * @param {string} message - 성공 메시지
   */
  const notifySuccess = (message) => {
    return addNotification({
      type: 'success',
      message: message,
      duration: 3000
    })
  }

  return {
    // State
    notifications,

    // Actions
    addNotification,
    removeNotification,
    clearAllNotifications,
    notifyProfanity,
    notifyAbuse,
    notifyWarning,
    notifyInfo,
    notifySuccess
  }
})
