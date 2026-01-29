/**
 * 제품 정보 관련 API 서비스
 */

import axios from 'axios'

const API_BASE_URL = '/api/v1'

/**
 * 제품 정보 조회
 * API: GET /api/v1/products/{productId}
 *
 * @param {number} productId - 제품 ID
 * @returns {Promise<Object>} 제품 정보 객체
 */
export const fetchProductById = async (productId) => {
  try {
    console.log(`[API 요청] GET ${API_BASE_URL}/products/${productId}`)
    const response = await axios.get(`${API_BASE_URL}/products/${productId}`)
    console.log('[API 응답]', response.data)

    if (response.data.isSuccess) {
      const { data } = response.data
      return {
        id: data.id,
        name: data.name,
        code: data.code,
        imageUrl: data.imageUrl,
        category: data.category
      }
    } else {
      throw new Error(response.data.message || '제품 정보 조회 실패')
    }
  } catch (error) {
    console.error('[제품 정보 조회 실패]', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status,
      url: `${API_BASE_URL}/products/${productId}`
    })
    throw error
  }
}
