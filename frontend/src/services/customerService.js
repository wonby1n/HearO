/**
 * 고객 정보 관련 API 서비스
 * API 명세: 공통_api명세서.xlsx
 */

import axios from 'axios'
import { mockCustomerInfo } from '@/mocks/counselor'

/**
 * JWT 토큰 가져오기
 */
const getAuthToken = () => {
  return sessionStorage.getItem('customerAccessToken') || localStorage.getItem('customerAccessToken')
}

/**
 * 대기열 등록 (고객 접수)
 * API: POST /api/v1/registrations
 */
export const registerQueue = async (payload) => {
  try {
    const token = getAuthToken()
    
    const response = await axios.post(
      `api/v1/registrations`, 
      {
        symptom: payload.symptom,
        productId: payload.productId,
        errorCode: payload.errorCode,
        manufacturedAt: payload.manufacturedAt,
        warrantyEndsAt: payload.warrantyEndsAt
      },
      {
        headers: { Authorization: `Bearer ${token}` }
      }
    )

    const { isSuccess, data, message } = response.data

    if (isSuccess) {
      console.log('✅ 대기열 등록 성공:', message)
      return data // registrationId, waitingRank, websocketTopic 반환
    } else {
      throw new Error(message || '대기열 등록에 실패했습니다.')
    }
  } catch (error) {
    console.error('❌ 대기열 등록 에러:', error.response?.data || error.message)
    throw error
  }
}

/**
 * 고객 접수 정보 조회
 * API: GET /api/v1/registrations/{registrationId}
 */
const fetchCustomerInfo = async (registrationId) => {
  try {
    const response = await axios.get(`/api/v1/registrations/${registrationId}`)
    const { isSuccess, data, message } = response.data

    if (isSuccess) {
      return data
    } else {
      throw new Error(message || '고객 정보 조회 실패')
    }
  } catch (error) {
    console.error('고객 정보 조회 에러:', error)
    // Fallback to mock data
    return mockCustomerInfo
  }
}

/**
 * 고객 상담 이력 조회
 * TODO: 실제 API 연동 필요
 */
const fetchCustomerHistory = async (registrationId) => {
  // 현재는 빈 배열 반환 (추후 구현)
  return []
}

/**
 * 고객 전체 정보 조회 (정보 + 이력)
 *
 * @param {number} registrationId - 접수 ID
 * @returns {Promise<Object>} { info, history }
 */
export const fetchCustomerData = async (registrationId) => {
  try {
    const [info, history] = await Promise.all([
      fetchCustomerInfo(registrationId),
      fetchCustomerHistory(registrationId)
    ])

    return {
      ...info,
      consultationHistory: history
    }
  } catch (error) {
    console.error('고객 데이터 조회 실패:', error)
    throw error
  }
}
