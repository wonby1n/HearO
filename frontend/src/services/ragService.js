import axios from 'axios'

/**
 * RAG API를 사용한 AI 가이드 서비스
 */

/**
 * 에러코드에 대한 AI 답변 요청
 * @param {string} question - 질문 (예: "에러코드 E101의 원인")
 * @returns {Promise<Object>} AI 답변 객체 { answer, evidence, need_more_info }
 */
export const askRAG = async (question) => {
  try {
    console.log('[RAG Service] 요청:', question)
    const response = await axios.post('/api/v1/rag/ask', {
      question
    })

    console.log('[RAG Service] 원본 응답:', response.data)

    // AskResponse는 BaseResponse로 감싸져 있지 않고 직접 반환됨
    // { answer: "JSON 문자열", contexts: [...] }
    if (response.data && response.data.answer) {
      // answer 필드가 JSON 문자열로 되어 있으므로 파싱
      const answerData = JSON.parse(response.data.answer)
      console.log('[RAG Service] 파싱된 답변:', answerData)

      return {
        answer: answerData.answer,
        evidence: answerData.evidence || [],
        needMoreInfo: answerData.need_more_info || [],
        contexts: response.data.contexts || []
      }
    } else {
      console.error('[RAG Service] 응답에 answer 필드가 없음:', response.data)
      throw new Error('응답 형식이 올바르지 않습니다')
    }
  } catch (error) {
    console.error('[RAG Service] AI 답변 요청 실패:', error)
    if (error.response) {
      console.error('[RAG Service] 에러 응답:', error.response.data)
    }
    throw error
  }
}

/**
 * 에러코드로 AI 답변 요청 (편의 함수)
 * @param {string} errorCode - 에러코드 (예: "E101")
 * @returns {Promise<Object>} AI 답변 객체
 */
export const askErrorCode = async (errorCode) => {
  const question = `에러코드 ${errorCode}의 원인`
  return askRAG(question)
}

/**
 * 증상으로 AI 답변 요청
 * @param {string} symptom - 증상 설명
 * @returns {Promise<Object>} AI 답변 객체
 */
export const askSymptom = async (symptom) => {
  return askRAG(symptom)
}
