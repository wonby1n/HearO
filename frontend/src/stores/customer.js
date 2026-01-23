import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCustomerStore = defineStore('customer', () => {
  // 현재 고객 정보
  const currentCustomer = ref({
    id: null,
    name: '',
    phone: '',
    productInfo: {
      modelName: '',
      productName: '',
      purchaseDate: '',
      warrantyPeriod: ''
    },
    issue: {
      symptom: '',
      errorCode: ''
    },
    qrId: null
  })

  // 고객 상담 이력
  const callHistory = ref([])

  // 대기열 정보
  const queueInfo = ref({
    position: 0,
    waitingCount: 0,
    isWaiting: false,
    isBlacklisted: false
  })

  // 고객 정보 존재 여부
  const hasCustomerInfo = computed(() => {
    return currentCustomer.value.id !== null
  })

  // 고객 정보 설정
  const setCustomerInfo = (customerData) => {
    currentCustomer.value = {
      ...currentCustomer.value,
      ...customerData
    }
  }

  // QR 정보 설정 (자동 입력)
  const setQRInfo = (qrData) => {
    currentCustomer.value.qrId = qrData.id
    currentCustomer.value.productInfo = {
      modelName: qrData.modelName || '',
      productName: qrData.productName || '',
      purchaseDate: qrData.purchaseDate || '',
      warrantyPeriod: qrData.warrantyPeriod || ''
    }
  }

  // 고객 기본 정보 설정
  const setBasicInfo = (name, phone) => {
    currentCustomer.value.name = name
    currentCustomer.value.phone = phone
  }

  // 제품 정보 설정
  const setProductInfo = (productInfo) => {
    currentCustomer.value.productInfo = {
      ...currentCustomer.value.productInfo,
      ...productInfo
    }
  }

  // 문제 정보 설정
  const setIssueInfo = (issue) => {
    currentCustomer.value.issue = {
      ...currentCustomer.value.issue,
      ...issue
    }
  }

  // 상담 이력 설정
  const setCallHistory = (history) => {
    callHistory.value = history
  }

  // 대기열 정보 업데이트
  const updateQueueInfo = (info) => {
    queueInfo.value = {
      ...queueInfo.value,
      ...info
    }
  }

  // 대기 시작
  const startWaiting = () => {
    queueInfo.value.isWaiting = true
  }

  // 대기 종료
  const stopWaiting = () => {
    queueInfo.value.isWaiting = false
    queueInfo.value.position = 0
  }

  // 고객 정보 초기화
  const resetCustomer = () => {
    currentCustomer.value = {
      id: null,
      name: '',
      phone: '',
      productInfo: {
        modelName: '',
        productName: '',
        purchaseDate: '',
        warrantyPeriod: ''
      },
      issue: {
        symptom: '',
        errorCode: ''
      },
      qrId: null
    }
    callHistory.value = []
    queueInfo.value = {
      position: 0,
      waitingCount: 0,
      isWaiting: false,
      isBlacklisted: false
    }
  }

  return {
    // State
    currentCustomer,
    callHistory,
    queueInfo,
    // Getters
    hasCustomerInfo,
    // Actions
    setCustomerInfo,
    setQRInfo,
    setBasicInfo,
    setProductInfo,
    setIssueInfo,
    setCallHistory,
    updateQueueInfo,
    startWaiting,
    stopWaiting,
    resetCustomer
  }
})
