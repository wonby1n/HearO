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
  return localStorage.getItem('customerAccessToken')
}

/**
 * 대기열 등록 (고객 접수)
 * API: POST /api/v1/queue/register
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
 * 고객 상담 이력 조회 (최근 3건)
 * API: GET /api/v1/registerations/{registrationId}/customer-history
 *
 * @param {number} registrationId - 접수 ID
 * @returns {Promise<Array>} 상담 이력 배열
 */
export const fetchCustomerHistory = async (registrationId) => {
  try {
    // TODO: 백엔드 API 준비되면 주석 해제
    // const token = getAuthToken()
    // const response = await axios.get(
    //   `${API_BASE_URL}/registerations/${registrationId}/customer-history`,
    //   {
    //     headers: {
    //       Authorization: `Bearer ${token}`
    //     }
    //   }
    // )
    // const { pastConsultations } = response.data
    //
    // // 명세에 맞춰 데이터 매핑 (최근 3건만)
    // return pastConsultations.slice(0, 3).map(consultation => ({
    //   date: consultation.createdAt,
    //   agent: `담당자: ${consultation.userName}`,
    //   summary: consultation.title
    // }))

    // 개발용: Mock 데이터 반환
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve(mockCustomerInfo.consultationHistory || [])
      }, 500)
    })
  } catch (error) {
    console.error('상담 이력 조회 실패:', error)
    throw error
  }
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
