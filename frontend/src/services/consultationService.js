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
