/**
 * Web Speech API 기반 STT 엔진
 */
import { STTEngine } from './sttEngine'

export class WebSpeechEngine extends STTEngine {
  constructor() {
    super()
    this.recognition = null
    this.transcriptCallback = null
    this.errorCallback = null
    this.isRunning = false
  }

  /**
   * Web Speech API 초기화
   * @param {Object} config
   * @param {string} config.lang - 언어 코드 (기본: 'ko-KR')
   * @param {boolean} config.continuous - 연속 인식 (기본: true)
   * @param {boolean} config.interimResults - 중간 결과 (기본: true)
   */
  async initialize(config = {}) {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition

    if (!SpeechRecognition) {
      throw new Error('Web Speech API를 지원하지 않는 브라우저입니다.')
    }

    this.recognition = new SpeechRecognition()
    this.recognition.lang = config.lang || 'ko-KR'
    this.recognition.continuous = config.continuous ?? true
    this.recognition.interimResults = config.interimResults ?? true
    this.recognition.maxAlternatives = 1

    this._setupEventHandlers()
  }

  /**
   * 이벤트 핸들러 설정
   * @private
   */
  _setupEventHandlers() {
    this.recognition.onresult = (event) => {
      const result = event.results[event.resultIndex]
      const transcript = result[0].transcript
      const confidence = result[0].confidence
      const isFinal = result.isFinal

      if (this.transcriptCallback) {
        this.transcriptCallback({
          text: transcript,
          confidence: confidence,
          isFinal: isFinal,
          timestamp: new Date().toISOString()
        })
      }
    }

    this.recognition.onerror = (event) => {
      console.error('Speech recognition error:', event.error)
      if (this.errorCallback) {
        this.errorCallback(new Error(event.error))
      }
    }

    this.recognition.onend = () => {
      // continuous 모드에서 자동 재시작
      if (this.isRunning && this.recognition.continuous) {
        try {
          this.recognition.start()
        } catch (error) {
          console.error('Recognition restart failed:', error)
        }
      }
    }
  }

  /**
   * STT 시작
   */
  async start() {
    if (!this.recognition) {
      throw new Error('STT engine not initialized')
    }

    if (this.isRunning) {
      console.warn('STT is already running')
      return
    }

    try {
      this.recognition.start()
      this.isRunning = true
    } catch (error) {
      console.error('Failed to start STT:', error)
      throw error
    }
  }

  /**
   * STT 중지
   */
  async stop() {
    if (!this.recognition) return

    this.isRunning = false
    try {
      this.recognition.stop()
    } catch (error) {
      console.error('Failed to stop STT:', error)
    }
  }

  /**
   * 트랜스크립트 콜백 등록
   * @param {Function} callback - ({ text, confidence, isFinal, timestamp }) => void
   */
  onTranscript(callback) {
    this.transcriptCallback = callback
  }

  /**
   * 에러 콜백 등록
   * @param {Function} callback - (error: Error) => void
   */
  onError(callback) {
    this.errorCallback = callback
  }

  /**
   * 리소스 정리
   */
  async destroy() {
    await this.stop()
    this.recognition = null
    this.transcriptCallback = null
    this.errorCallback = null
  }
}
