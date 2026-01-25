/**
 * 고객 정보 관련 API 서비스
 * API 명세: 공통_api명세서.xlsx
 */

import axios from 'axios'
import { mockCustomerInfo } from '@/mocks/counselor'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1'

/**
 * JWT 토큰 가져오기
 * @returns {string|null} JWT 토큰
 */
const getAuthToken = () => {
  // TODO: 실제 토큰 저장소에서 가져오기 (localStorage, Pinia store 등)
  return localStorage.getItem('accessToken')
}

/**
 * 고객 접수 정보 조회
 * API: GET /api/v1/registerations/{registrationId}/product-info
 *
 * @param {number} registrationId - 접수 ID
 * @returns {Promise<Object>} 고객 정보 객체
 */
export const fetchCustomerInfo = async (registrationId) => {
  try {
    // TODO: 백엔드 API 준비되면 주석 해제
    // const token = getAuthToken()
    // const response = await axios.get(
    //   `${API_BASE_URL}/registerations/${registrationId}/product-info`,
    //   {
    //     headers: {
    //       Authorization: `Bearer ${token}`
    //     }
    //   }
    // )
    // const { registration } = response.data
    //
    // // 명세에 맞춰 데이터 매핑
    // return {
    //   phone: registration.phone || '010-****-****',
    //   productName: registration.productCategory || '',
    //   modelNumber: registration.modelCode || '',
    //   purchaseDate: registration.boughtAt || '',
    //   warrantyStatus: registration.warrantyStatus === 'ACTIVE' ? '이내' : '만료',
    //   productImage: null,
    //   symptoms: [registration.symptom, registration.errorCode].filter(Boolean)
    // }

    // 개발용: Mock 데이터 반환
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          phone: mockCustomerInfo.phone,
          productName: mockCustomerInfo.productName,
          modelNumber: mockCustomerInfo.modelNumber,
          purchaseDate: mockCustomerInfo.purchaseDate,
          warrantyStatus: mockCustomerInfo.warrantyStatus,
          productImage: mockCustomerInfo.productImage,
          symptoms: mockCustomerInfo.symptoms
        })
      }, 500) // 네트워크 지연 시뮬레이션
    })
  } catch (error) {
    console.error('고객 정보 조회 실패:', error)
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
