/**
 * 고객 정보 관련 API 서비스
 * API 명세: 공통_api명세서.xlsx (업데이트 반영)
 */

import axios from 'axios'

/**
 * JWT 토큰 가져오기 (세션 또는 로컬 스토리지)
 */
const getAuthToken = () => {
  return sessionStorage.getItem('customerAccessToken') || localStorage.getItem('customerAccessToken')
}

/**
 * 대기열 등록 (고객 접수)
 * API: POST /api/v1/registrations
 * * @param {Object} payload - 접수 정보 (symptom, productId, errorCode, manufacturedAt, warrantyEndsAt)
 * @returns {Promise<Object>} 등록된 접수 상세 정보 (data 객체 전체)
 */
export const registerQueue = async (payload) => {
  try {
    const token = getAuthToken()
    
    const response = await axios.post(
      `/api/v1/registrations`, 
      {
        symptom: payload.symptom,
        productId: payload.productId,
        errorCode: payload.errorCode,
        manufacturedAt: payload.manufacturedAt,
        warrantyEndsAt: payload.warrantyEndsAt
      },
      {
        headers: { 
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    )

    const { isSuccess, data, message } = response.data

    if (isSuccess) {
      console.log('✅ 대기열 등록 성공:', message)
      // 서버 응답의 data 객체(id, customerName, productName 등 포함)를 그대로 반환합니다.
      return data 
    } else {
      throw new Error(message || '대기열 등록에 실패했습니다.')
    }
  } catch (error) {
    console.error('❌ 대기열 등록 에러:', error.response?.data || error.message)
    throw error
  }
}

/**
 * 접수 정보 조회 (단건)
 * API: GET /api/v1/registrations/{registrationId}
 *
 * @param {number} registrationId - 접수 ID
 * @returns {Promise<Object>} 접수 상세 데이터
 */
export const fetchCustomerInfo = async (registrationId) => {
  try {
    const token = getAuthToken()
    const response = await axios.get(`/api/v1/registrations/${registrationId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })

    const { isSuccess, data, message } = response.data

    if (isSuccess) {
      return data // 상세 정보 데이터 객체 반환
    } else {
      throw new Error(message || '접수 정보 조회 실패')
    }
  } catch (error) {
    console.error('❌ 접수 정보 조회 에러:', error.response?.data || error.message)
    throw error
  }
}

/**
 * 고객 상담 이력 조회
 * API: GET /api/v1/customers/{customerId}/consultations
 * (현재는 빈 배열을 반환하며, 추후 API 구현 시 연결)
 */
export const fetchCustomerHistory = async (customerId) => {
  try {
    // TODO: 실제 API 엔드포인트 구현 시 아래 주석 해제 및 적용
    /*
    const token = getAuthToken()
    const response = await axios.get(
      `/api/v1/customers/${customerId}/consultations`,
      {
        headers: { Authorization: `Bearer ${token}` }
      }
    )

    const { isSuccess, data, message } = response.data

    if (isSuccess) {
      return data || []
    } else {
      throw new Error(message || '상담 이력 조회 실패')
    }
    */

    // 현재는 빈 배열 반환
    return []
  } catch (error) {
    console.error('❌ 상담 이력 조회 에러:', error)
    return []
  }
}

/**
 * 고객 통합 데이터 조회 (기본 정보 + 상담 이력)
 * * @param {number} registrationId - 접수 ID
 */
export const fetchCustomerData = async (registrationId) => {
  try {
    // 1. 먼저 접수 정보를 가져옵니다.
    const customerInfo = await fetchCustomerInfo(registrationId)
    
    // 2. 접수 정보에 포함된 customerId를 사용하여 이력을 조회합니다.
    const history = await fetchCustomerHistory(customerInfo.customerId)

    return {
      ...customerInfo,
      consultationHistory: history
    }
  } catch (error) {
    console.error('❌ 고객 데이터 통합 조회 실패:', error)
    throw error
  }
}