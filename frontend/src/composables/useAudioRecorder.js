/**
 * useAudioRecorder — 고객 + 상담원 오디오를 믹스하여 녹음하고 로컬 파일로 다운로드하는 composable
 *
 * - startRecording()  : AudioContext + MediaStreamDestination + MediaRecorder 생성 및 시작
 * - addTrack(track)   : 녹음 믹스에 MediaStreamTrack 추가
 * - stopRecording()   : 녹음 종료 → { blob, durationMs } 반환 (Promise)
 * - downloadRecording(blob, baseName) : 오디오 파일 다운로드 트리거 (확장자 자동 결정)
 * - cleanup()         : 모든 리소스 강제 정리 (unmount용)
 */
export function useAudioRecorder() {
  let audioCtx = null
  let destination = null
  let mediaRecorder = null
  let mimeType = ''
  let chunks = []
  let startTime = null
  const sources = [] // MediaStreamSource 참조 — disconnect용

  // 현재 브라우저에서 지원하는 최우선 mime 타입 감지
  const detectMimeType = () => {
    const candidates = [
      'audio/webm;codecs=opus',
      'audio/webm',
      'audio/ogg;codecs=opus',
      'audio/ogg',
      'audio/mp4'
    ]
    return candidates.find((t) => MediaRecorder.isTypeSupported(t)) || ''
  }

  const startRecording = () => {
    try {
      if (mediaRecorder) return // 이미 녹음 중
      audioCtx = new (window.AudioContext || window.webkitAudioContext)()
      destination = audioCtx.createMediaStreamDestination()

      mimeType = detectMimeType()
      mediaRecorder = new MediaRecorder(destination.stream, mimeType ? { mimeType } : {})
      chunks = []
      startTime = Date.now()

      mediaRecorder.ondataavailable = (e) => {
        if (e.data.size > 0) chunks.push(e.data)
      }

      mediaRecorder.start(1000) // 1초 단위로 청크 수집
      console.log('[useAudioRecorder] 녹음 시작 (mime:', mimeType || 'default', ')')
    } catch (e) {
      console.error('[useAudioRecorder] 녹음 시작 실패:', e)
    }
  }

  const addTrack = (mediaStreamTrack) => {
    if (!audioCtx || !destination || !mediaStreamTrack) {
      console.warn('[useAudioRecorder] 녹음 미활성화 또는 트랙 없음')
      return
    }
    try {
      if (audioCtx.state === 'suspended') audioCtx.resume()
      const stream = new MediaStream([mediaStreamTrack])
      const source = audioCtx.createMediaStreamSource(stream)
      source.connect(destination)
      sources.push(source)
      console.log('[useAudioRecorder] 트랙 추가:', mediaStreamTrack.label || mediaStreamTrack.id)
    } catch (e) {
      console.error('[useAudioRecorder] 트랙 추가 실패:', e)
    }
  }

  // 녹음 종료 후 Blob 반환 (await 가능)
  const stopRecording = () => {
    return new Promise((resolve) => {
      if (!mediaRecorder || mediaRecorder.state === 'inactive') {
        resolve(null)
        return
      }

      mediaRecorder.onstop = () => {
        const blob = new Blob(chunks, { type: mimeType || 'audio/webm' })
        const durationMs = startTime ? Date.now() - startTime : 0
        chunks = []
        mediaRecorder = null
        console.log('[useAudioRecorder] 녹음 종료 — 크기:', blob.size, 'B, 시간:', durationMs, 'ms')
        resolve({ blob, durationMs })
      }

      mediaRecorder.stop()
    })
  }

  // baseName만 전달하면 blob.type에 따라 확장자 자동 결정
  const downloadRecording = (blob, baseName) => {
    if (!blob || blob.size === 0) {
      console.warn('[useAudioRecorder] 다운로드할 오디오 없음')
      return
    }
    let ext = 'webm'
    if (blob.type?.includes('ogg')) ext = 'ogg'
    else if (blob.type?.includes('mp4')) ext = 'mp4'

    const filename = `${baseName}.${ext}`
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    console.log('[useAudioRecorder] 파일 다운로드:', filename)
  }

  // 강제 정리 (unmount / 예외 종료용)
  const cleanup = () => {
    if (mediaRecorder && mediaRecorder.state !== 'inactive') {
      try { mediaRecorder.stop() } catch {}
    }
    sources.forEach((s) => { try { s.disconnect() } catch {} })
    sources.length = 0
    try { audioCtx?.close() } catch {}
    audioCtx = null
    destination = null
    mediaRecorder = null
    chunks = []
    startTime = null
  }

  return { startRecording, addTrack, stopRecording, downloadRecording, cleanup }
}
