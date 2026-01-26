/**
 * STT 엔진 인터페이스
 * 모든 STT 엔진은 이 클래스를 상속하여 구현해야 함
 */
export class STTEngine {
  /**
   * STT 엔진 초기화
   * @param {Object} config - 엔진별 설정
   * @returns {Promise<void>}
   */
  async initialize(config) {
    throw new Error('initialize() must be implemented')
  }

  /**
   * STT 인식 시작
   * @returns {Promise<void>}
   */
  async start() {
    throw new Error('start() must be implemented')
  }

  /**
   * STT 인식 중지
   * @returns {Promise<void>}
   */
  async stop() {
    throw new Error('stop() must be implemented')
  }

  /**
   * 트랜스크립트 콜백 등록
   * @param {Function} callback - (transcript: string, confidence: number) => void
   */
  onTranscript(callback) {
    throw new Error('onTranscript() must be implemented')
  }

  /**
   * 에러 콜백 등록
   * @param {Function} callback - (error: Error) => void
   */
  onError(callback) {
    throw new Error('onError() must be implemented')
  }

  /**
   * 종료 처리
   * @returns {Promise<void>}
   */
  async destroy() {
    throw new Error('destroy() must be implemented')
  }
}
