/**
 * 로컬 AI 서버(/unsmile) 폭언 감지 API
 * Electron 앱이 함께 실행하는 AI 서버를 직접 호출하므로 axios 기본 설정(baseURL, 인증)을 쓰지 않는다.
 */
const TOXIC_API_URL = import.meta.env.VITE_TOXIC_API_URL || 'http://127.0.0.1:8000/unsmile'

/**
 * @param {string} text
 * @returns {Promise<{ toxic: boolean, score: number }>} 실패 시 폭언 아님으로 처리
 */
export const analyzeToxicity = async (text) => {
  if (!text?.trim()) return { toxic: false, score: 0 }
  try {
    const res = await fetch(TOXIC_API_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ text })
    })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const data = await res.json()
    return {
      toxic: !!data.toxic,
      score: typeof data.risk_max === 'number' ? data.risk_max : 0
    }
  } catch (e) {
    console.warn('[toxicityService] 폭력성 검사 실패(우회):', e)
    return { toxic: false, score: 0 }
  }
}
