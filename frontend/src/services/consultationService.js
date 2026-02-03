/**
 * Consultation-related API service
 */
import axios from 'axios'

const API_BASE_URL = '/api/v1'

const getAuthToken = () => {
  return localStorage.getItem('accessToken')
}

/**
 * Save consultation memo
 * API: PUT /api/v1/consultations/{consultationId}/memo
 *
 * @param {string|number} consultationId
 * @param {string} memo
 * @returns {Promise<Object>}
 */
export const saveConsultationMemo = async (consultationId, memo) => {
  const token = getAuthToken()
  const response = await axios.put(
    `${API_BASE_URL}/consultations/${consultationId}/memo`,
    { memo },
    token
      ? {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      : undefined
  )

  return response.data
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
 * Get my consultation history (paginated)
 * API: GET /api/v1/consultations/me
 *
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Page size
 * @returns {Promise<Object>} Paginated consultation list
 */
export const getMyConsultations = async (page = 0, size = 10) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/consultations/me`, {
      params: { page, size }
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
