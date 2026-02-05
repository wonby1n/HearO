/**
 * Consultation-related API service
 */
import axios from 'axios'

const API_BASE_URL = '/api/v1'

const getAuthToken = () => {
  return localStorage.getItem('accessToken')
}

/**
 * Start consultation (상담 시작)
 * API: POST /api/v1/consultations
 *
 * @param {Object} payload
 * @param {number} payload.customerId - 고객 ID
 * @param {number} payload.registrationId - 접수 ID
 * @returns {Promise<Object>} { consultationId }
 */
export const startConsultation = async (payload) => {
  try {
    const token = getAuthToken()
    const response = await axios.post(
      `${API_BASE_URL}/consultations`,
      {
        customerId: payload.customerId,
        registrationId: payload.registrationId
      },
      token
        ? {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
        : undefined
    )

    if (response.data && response.data.isSuccess) {
      console.log('✅ [consultationService] 상담 시작 성공:', response.data.data)
      return response.data.data
    } else {
      throw new Error(response.data?.message || '상담 시작 실패')
    }
  } catch (error) {
    console.error('❌ 상담 시작 에러:', error.response?.data || error.message)
    throw error
  }
}

/**
 * Submit consultation rating (고객 평점 제출)
 * API: POST /api/v1/consultations/{consultationId}/rating
 *
 * @param {string|number} consultationId
 * @param {Object} ratingData - { processRating, solutionRating, kindnessRating, feedback }
 * @returns {Promise<Object>}
 */
export const submitConsultationRating = async (consultationId, ratingData) => {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/consultations/${consultationId}/rating`,
      ratingData
    )

    if (response.data && response.data.isSuccess) {
      console.log('✅ [consultationService] 평점 제출 성공:', response.data.data)
      return response.data.data
    } else {
      throw new Error(response.data?.message || '평점 제출 실패')
    }
  } catch (error) {
    console.error('❌ 평점 제출 에러:', error.response?.data || error.message)
    throw error
  }
}

/**
 * Get consultation rating
 * API: GET /api/v1/consultations/{consultationId}/rating
 *
 * @param {string|number} consultationId
 * @returns {Promise<Object>} { ratingId, consultationId, processRating, solutionRating, kindnessRating, feedback }
 */
export const fetchConsultationRating = async (consultationId) => {
  try {
    const token = getAuthToken()
    const response = await axios.get(
      `${API_BASE_URL}/consultations/${consultationId}/rating`,
      token
        ? {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
        : undefined
    )

    if (response.data && response.data.isSuccess) {
      return response.data.data
    } else {
      throw new Error(response.data?.message || '평점 조회 실패')
    }
  } catch (error) {
    console.error('❌ 상담 평점 조회 에러:', error.response?.data || error.message)
    throw error
  }
}

/**
 * Calculate average rating from individual ratings
 *
 * @param {Object} rating - { processRating, solutionRating, kindnessRating }
 * @returns {number} Average rating (rounded to 1 decimal)
 */
export const calculateAverageRating = (rating) => {
  if (!rating) return 0

  const { processRating, solutionRating, kindnessRating } = rating

  if (!processRating || !solutionRating || !kindnessRating) {
    return 0
  }

  const average = (processRating + solutionRating + kindnessRating) / 3
  return Math.round(average * 10) / 10 // 소수점 첫째자리까지
}

/**
 * Get latest 3 consultation history by customerId
 * API: GET /api/v1/consultations/latest
 *
 * @param {number} customerId - Customer ID
 * @returns {Promise<Array>} Latest 3 consultations
 */
export const getLatestConsultations = async (customerId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/consultations/latest`, {
      params: { customerId }
    })

    if (response.data && response.data.isSuccess) {
      return response.data.data || []
    } else {
      throw new Error(response.data?.message || '과거 상담 이력 조회 실패')
    }
  } catch (error) {
    console.error('❌ 과거 상담 이력 조회 에러:', error.response?.data || error.message)
    throw error
  }
}

/**
 * Get my consultation history (paginated)
 * API: GET /api/v1/consultations/me
 *
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Page size
 * @returns {Promise<Object>} Paginated consultation list
 */
export const getMyConsultations = async (page = 0, size = 10) => {
  try {
    const token = getAuthToken()
    const response = await axios.get(`${API_BASE_URL}/consultations/me`, {
      params: { page, size },
      headers: token ? {
        Authorization: `Bearer ${token}`
      } : undefined
    })

    if (response.data && response.data.isSuccess) {
      return response.data.data
    } else {
      throw new Error(response.data?.message || '상담 이력 조회 실패')
    }
  } catch (error) {
    console.error('❌ 상담 이력 조회 에러:', error.response?.data || error.message)
    throw error
  }
}

/**
 * Get consultations by customer ID (paginated)
 * API: GET /api/v1/consultations/customer/{customerId}
 *
 * @param {number} customerId - Customer ID
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Page size
 * @returns {Promise<Object>} Paginated consultation list
 */
export const getConsultationsByCustomer = async (customerId, page = 0, size = 10) => {
  try {
    const token = getAuthToken()
    const response = await axios.get(`${API_BASE_URL}/consultations/customer/${customerId}`, {
      params: { page, size },
      headers: token ? {
        Authorization: `Bearer ${token}`
      } : undefined
    })

    if (response.data && response.data.isSuccess) {
      return response.data.data
    } else {
      throw new Error(response.data?.message || '고객 상담 이력 조회 실패')
    }
  } catch (error) {
    console.error('❌ 고객 상담 이력 조회 에러:', error.response?.data || error.message)
    throw error
  }
}
