/**
 * STT 서비스 관리자
 * 다양한 STT 엔진을 통합 관리
 */
import { WebSpeechEngine } from './stt/webSpeechEngine'

const STT_ENGINES = {
  'web-speech': WebSpeechEngine,
  // 추가 STT 엔진은 여기에 등록
}

class STTService {
  constructor() {
    this.engine = null
    this.currentEngine = null
  }

  /**
   * STT 엔진 초기화
   * @param {string} engineType - 'web-speech' | 'clova' | 'whisper'
   * @param {Object} config - 엔진별 설정
   * @returns {Promise<void>}
   */
  async initialize(engineType, config = {}) {
    const EngineClass = STT_ENGINES[engineType]

    if (!EngineClass) {
      throw new Error(`Unknown STT engine: ${engineType}. Available: ${Object.keys(STT_ENGINES).join(', ')}`)
    }

    // 기존 엔진 정리
    if (this.engine) {
      await this.engine.destroy()
    }

    this.engine = new EngineClass()
    await this.engine.initialize(config)
    this.currentEngine = engineType

    console.log(`STT Engine initialized: ${engineType}`)
  }

  /**
   * STT 시작
   * @returns {Promise<void>}
   */
  async start() {
    if (!this.engine) {
      throw new Error('STT engine not initialized. Call initialize() first.')
    }
    return this.engine.start()
  }

  /**
   * STT 중지
   * @returns {Promise<void>}
   */
  async stop() {
    if (!this.engine) return
    return this.engine.stop()
  }

  /**
   * 트랜스크립트 콜백 등록
   * @param {Function} callback - 트랜스크립트 수신 콜백
   */
  onTranscript(callback) {
    if (!this.engine) {
      throw new Error('STT engine not initialized. Call initialize() first.')
    }
    this.engine.onTranscript(callback)
  }

  /**
   * 에러 콜백 등록
   * @param {Function} callback - 에러 발생 콜백
   */
  onError(callback) {
    if (!this.engine) {
      throw new Error('STT engine not initialized. Call initialize() first.')
    }
    this.engine.onError(callback)
  }

  /**
   * 현재 사용 중인 엔진 타입 반환
   * @returns {string|null}
   */
  getCurrentEngine() {
    return this.currentEngine
  }

  /**
   * 리소스 정리
   * @returns {Promise<void>}
   */
  async destroy() {
    if (this.engine) {
      await this.engine.destroy()
      this.engine = null
      this.currentEngine = null
    }
  }
}

// Singleton 인스턴스 export
export const sttService = new STTService()
