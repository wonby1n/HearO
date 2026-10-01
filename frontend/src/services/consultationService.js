/**
 * Consultation-related API service
 * 인증 헤더는 axios 인터셉터(stores/auth.js)가 붙인다.
 */
import axios from 'axios'

const API_BASE_URL = '/api/v1'

// BaseResponse { isSuccess, data, message } 래퍼 해제
const unwrap = (response, fallbackMessage) => {
  if (response.data?.isSuccess) return response.data.data
  throw new Error(response.data?.message || fallbackMessage)
}

const request = async (label, fn) => {
  try {
    return await fn()
  } catch (error) {
    console.error(`❌ ${label} 에러:`, error.response?.data || error.message)
    throw error
  }
}

/**
 * 상담 시작
 * API: POST /api/v1/consultations
 *
 * @param {{ customerId: number, registrationId: number }} payload
 * @returns {Promise<{ consultationId: number }>}
 */
export const startConsultation = (payload) => request('상담 시작', async () => {
  const response = await axios.post(`${API_BASE_URL}/consultations`, {
    customerId: payload.customerId,
    registrationId: payload.registrationId
  })
  return unwrap(response, '상담 시작 실패')
})

/**
 * 고객 평점 제출
 * API: POST /api/v1/consultations/{consultationId}/rating
 *
 * @param {string|number} consultationId
 * @param {Object} ratingData - { processRating, solutionRating, kindnessRating, feedback }
 */
export const submitConsultationRating = (consultationId, ratingData) => request('평점 제출', async () => {
  const response = await axios.post(`${API_BASE_URL}/consultations/${consultationId}/rating`, ratingData)
  return unwrap(response, '평점 제출 실패')
})

/**
 * 고객의 최근 상담 이력 3건
 * API: GET /api/v1/consultations/latest
 *
 * @param {number} customerId
 * @returns {Promise<Array>}
 */
export const getLatestConsultations = (customerId) => request('과거 상담 이력 조회', async () => {
  const response = await axios.get(`${API_BASE_URL}/consultations/latest`, {
    params: { customerId }
  })
  return unwrap(response, '과거 상담 이력 조회 실패') || []
})

/**
 * 내 상담 이력 (페이지네이션)
 * API: GET /api/v1/consultations/me
 *
 * @param {number} page - 0부터 시작
 * @param {number} size
 */
export const getMyConsultations = (page = 0, size = 10) => request('상담 이력 조회', async () => {
  const response = await axios.get(`${API_BASE_URL}/consultations/me`, {
    params: { page, size }
  })
  return unwrap(response, '상담 이력 조회 실패')
})

/**
 * 특정 고객의 상담 이력 (페이지네이션)
 * API: GET /api/v1/consultations/customer/{customerId}
 *
 * @param {number} customerId
 * @param {number} page - 0부터 시작
 * @param {number} size
 */
export const getConsultationsByCustomer = (customerId, page = 0, size = 10) => request('고객 상담 이력 조회', async () => {
  const response = await axios.get(`${API_BASE_URL}/consultations/customer/${customerId}`, {
    params: { page, size }
  })
  return unwrap(response, '고객 상담 이력 조회 실패')
})
