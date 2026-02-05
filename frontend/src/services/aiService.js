import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

/**
 * Get authentication token from localStorage
 */
const getAuthToken = () => {
    return localStorage.getItem('accessToken')
}

/**
 * Generate AI summary for a consultation
 * API: POST /api/v1/ai/summary
 * 
 * @param {number} consultationId - Consultation ID
 * @param {string} fullTranscript - Full STT transcript
 * @returns {Promise<Object>} AI summary with title, subtitle, aiSummary
 */
export const generateAISummary = async (consultationId, fullTranscript) => {
    try {
        const token = getAuthToken()
        const response = await axios.post(
            `${API_BASE_URL}/api/v1/ai/summary`,
            {
                consultationId,
                fullTranscript
            },
            {
                headers: token ? {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json'
                } : {
                    'Content-Type': 'application/json'
                }
            }
        )

        console.log('[aiService] 응답 데이터:', response.data)

        // 응답 형식 처리: isSuccess 래퍼가 있는 경우와 직접 응답인 경우 모두 처리
        if (response.data) {
            // isSuccess 래퍼가 있는 경우
            if (response.data.isSuccess && response.data.data) {
                return response.data.data
            }
            // 직접 응답인 경우 (title, subtitle, aiSummary가 직접 있음)
            else if (response.data.title || response.data.subtitle || response.data.aiSummary) {
                return response.data
            }
        }

        throw new Error('AI 요약 응답 형식이 올바르지 않습니다')
    } catch (error) {
        console.error('❌ AI 요약 생성 에러:', error.response?.data || error.message)
        throw error
    }
}
