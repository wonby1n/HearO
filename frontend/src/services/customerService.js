/**
 * 고객 정보 관련 API 서비스
 */

import axios from 'axios'
import { mockCustomerInfo } from '@/mocks/counselor'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

/**
 * 고객 접수 정보 조회
 * @param {string} customerId - 고객 ID
 * @returns {Promise<Object>} 고객 정보 객체
 */
export const fetchCustomerInfo = async (customerId) => {
  try {
    // TODO: 백엔드 API 준비되면 주석 해제
    // const response = await axios.get(`${API_BASE_URL}/customer/info/${customerId}`)
    // return response.data

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
 * @param {string} customerId - 고객 ID
 * @returns {Promise<Array>} 상담 이력 배열
 */
export const fetchCustomerHistory = async (customerId) => {
  try {
    // TODO: 백엔드 API 준비되면 주석 해제
    // const response = await axios.get(`${API_BASE_URL}/customer/history/${customerId}`, {
    //   params: { limit: 3 }
    // })
    // return response.data

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
 * @param {string} customerId - 고객 ID
 * @returns {Promise<Object>} { info, history }
 */
export const fetchCustomerData = async (customerId) => {
  try {
    const [info, history] = await Promise.all([
      fetchCustomerInfo(customerId),
      fetchCustomerHistory(customerId)
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
