/**
 * useDelayedCustomerAudio — 상담원이 듣는 고객 음성을 지연 재생하고, 폭언 구간을 차단하는 composable
 *
 * 고객 음성을 delaySec만큼 늦춰 재생하므로, 그 사이 도착한 STT 결과로 폭언을 판정해
 * 상담원이 듣기 전에 해당 구간을 삐- 소리로 대체할 수 있다.
 *
 * - attach(track, participantId, container) : 고객 오디오 트랙에 지연 파이프라인 연결
 * - blockUntilNextStt(participantId)         : 폭언 감지 시 즉시 음소거 + 삐- 소리
 * - scheduleUnblock(participantId)           : 다음 STT 수신 시 지연 시간 이후 음소거 해제
 * - cleanup()                                : 모든 리소스 정리 (unmount용)
 */
export function useDelayedCustomerAudio({ delaySec = 3, mutePostpadMs = 600 } = {}) {
  let audioCtx = null
  const pipelines = new Map() // participantId -> { gain, blocked, fallbackEl }

  const ensureAudioContext = async () => {
    if (!audioCtx) {
      audioCtx = new (window.AudioContext || window.webkitAudioContext)()
    }
    if (audioCtx.state === 'suspended') {
      try { await audioCtx.resume() } catch { }
    }
    return audioCtx
  }

  const attach = async (track, participantId, container) => {
    if (!track?.mediaStreamTrack) return

    const ctx = await ensureAudioContext()
    if (pipelines.has(participantId)) return

    // 1. Web Audio가 신호를 내기 전까지 끊김이 없도록 원본을 즉시 재생
    const fallbackEl = track.attach()
    container?.appendChild(fallbackEl)

    // 2. Source -> Delay -> Gain -> Destination & Analyser
    const source = ctx.createMediaStreamSource(new MediaStream([track.mediaStreamTrack]))

    const delayNode = ctx.createDelay(10)
    delayNode.delayTime.value = delaySec

    const gainNode = ctx.createGain()
    gainNode.gain.value = 1

    const analyser = ctx.createAnalyser()
    analyser.fftSize = 256
    const pcmData = new Float32Array(analyser.fftSize)

    source.connect(delayNode)
    delayNode.connect(gainNode)
    gainNode.connect(ctx.destination)
    gainNode.connect(analyser)

    pipelines.set(participantId, { gain: gainNode, blocked: false, fallbackEl })

    // 3. 지연된 소리가 나오기 시작하면 원본 재생을 끔
    const checkSignal = () => {
      const pipe = pipelines.get(participantId)
      if (!pipe) return

      analyser.getFloatTimeDomainData(pcmData)
      let sumSquares = 0
      for (const amplitude of pcmData) {
        sumSquares += amplitude * amplitude
      }
      const rms = Math.sqrt(sumSquares / pcmData.length)

      if (rms > 0.01) {
        pipe.fallbackEl.muted = true
      } else {
        requestAnimationFrame(checkSignal)
      }
    }

    checkSignal()
  }

  const setMuted = (participantId, muted) => {
    const p = pipelines.get(participantId)
    if (!p) return
    const target = muted ? 0 : 1
    try {
      p.gain.gain.setTargetAtTime(target, audioCtx.currentTime, 0.02)
    } catch {
      p.gain.gain.value = target
    }
  }

  // 폭언 구간 대체용 삐- 소리 (페이드 인/아웃으로 클릭음 방지)
  const playBeep = (duration = 0.25, frequency = 1000) => {
    if (!audioCtx) return

    const oscillator = audioCtx.createOscillator()
    const gainNode = audioCtx.createGain()
    oscillator.connect(gainNode)
    gainNode.connect(audioCtx.destination)

    oscillator.frequency.value = frequency
    oscillator.type = 'sine'

    const now = audioCtx.currentTime
    gainNode.gain.setValueAtTime(0, now)
    gainNode.gain.linearRampToValueAtTime(0.3, now + 0.02)
    gainNode.gain.setValueAtTime(0.3, now + duration - 0.02)
    gainNode.gain.linearRampToValueAtTime(0, now + duration)

    oscillator.start(now)
    oscillator.stop(now + duration)
  }

  const blockUntilNextStt = (participantId) => {
    const p = pipelines.get(participantId)
    if (!p) return
    p.blocked = true
    setMuted(participantId, true)
    playBeep()
  }

  // 다음 STT가 오면, 지연 시간만큼 기다렸다가 해제 ("그 다음 구간"부터 다시 들리게 하는 보수적 방식)
  const scheduleUnblock = (participantId) => {
    const p = pipelines.get(participantId)
    if (!p?.blocked) return

    setTimeout(() => {
      const latest = pipelines.get(participantId)
      if (!latest) return
      latest.blocked = false
      setMuted(participantId, false)
    }, delaySec * 1000 + mutePostpadMs)
  }

  const cleanup = () => {
    try {
      for (const pipe of pipelines.values()) {
        pipe.fallbackEl?.pause()
        pipe.fallbackEl?.remove()
      }
      pipelines.clear()
      audioCtx?.close?.()
    } catch {
      // ignore
    } finally {
      audioCtx = null
    }
  }

  return { ensureAudioContext, attach, blockUntilNextStt, scheduleUnblock, cleanup }
}
