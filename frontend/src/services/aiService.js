import axios from 'axios'

/**
 * 상담 AI 요약 생성
 * API: POST /api/v1/ai/summary
 * 인증 헤더는 axios 인터셉터(stores/auth.js)가 붙인다.
 *
 * @param {number} consultationId
 * @param {string} fullTranscript - 전체 STT 대화록
 * @returns {Promise<{ title: string, subtitle: string, aiSummary: string }>}
 */
export const generateAISummary = async (consultationId, fullTranscript) => {
    try {
        const response = await axios.post('/api/v1/ai/summary', { consultationId, fullTranscript })
        const body = response.data

        // BaseResponse 래퍼가 있는 경우와 직접 응답인 경우 모두 처리
        if (body?.isSuccess && body.data) {
            return body.data
        }
        if (body?.title || body?.subtitle || body?.aiSummary) {
            return body
        }

        throw new Error('AI 요약 응답 형식이 올바르지 않습니다')
    } catch (error) {
        console.error('❌ AI 요약 생성 에러:', error.response?.data || error.message)
        throw error
    }
}
