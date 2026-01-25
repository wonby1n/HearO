# VOICE-S01: 실시간 음성 스트림 수신 구조

## 📁 파일 구조

```
src/
├── services/
│   ├── stt/
│   │   ├── sttEngine.js           # STT 엔진 인터페이스 (추상 클래스)
│   │   └── webSpeechEngine.js     # Web Speech API 구현
│   │
│   ├── sttService.js              # STT 엔진 관리자 (Singleton)
│   └── livekitService.js          # LiveKit 연결 관리 (Singleton)
│
└── composables/
    ├── useAudioStream.js          # 오디오 버퍼링/샘플링 처리
    └── useVoiceCall.js            # 통합 통화 관리
```

---

## 🎯 각 모듈 역할

### 1. **STT 엔진 (전략 패턴)**

#### `sttEngine.js` - 인터페이스
모든 STT 엔진이 구현해야 하는 메서드 정의

#### `webSpeechEngine.js` - Web Speech API 구현
- 브라우저 내장 STT API 사용
- 실시간 음성 인식
- 중간 결과 지원

#### `clovaEngine.js` (TODO)
- Naver Clova STT API 사용
- WebSocket 기반 스트리밍
- 높은 정확도

### 2. **서비스 레이어**

#### `sttService.js` - STT 관리자
- 다양한 STT 엔진을 통합 관리
- `.env` 파일로 엔진 선택 가능
- Singleton 패턴

#### `livekitService.js` - LiveKit 관리
- WebRTC Room 연결/해제
- 원격 오디오 트랙 구독
- 이벤트 처리

### 3. **Composable**

#### `useAudioStream.js` - 오디오 처리
- 오디오 버퍼링 (200ms)
- 리샘플링 (16kHz)
- WebAudio API 활용

#### `useVoiceCall.js` - 통합 관리
- LiveKit + STT + Audio 통합
- Vue 컴포넌트에서 쉽게 사용

---

## 🚀 사용 방법

### CounselorCallView.vue에서 사용

```javascript
<script setup>
import { ref } from 'vue'
import { useVoiceCall } from '@/composables/useVoiceCall'

const sttMessages = ref([])

const { isConnected, isSTTActive, initializeCall, startSTT, endCall } = useVoiceCall()

// 통화 초기화
const startCall = async () => {
  await initializeCall({
    livekitToken: 'your-livekit-token',
    livekitUrl: 'wss://your-livekit-server.com',
    sttEngine: 'web-speech',
    onTranscript: ({ text, confidence, isFinal, timestamp }) => {
      if (isFinal) {
        sttMessages.value.push({
          speaker: 'customer',
          text: text,
          timestamp: new Date(timestamp).toLocaleTimeString(),
          confidence: confidence,
          hasProfanity: false
        })
      }
    },
    onError: (error) => {
      console.error('STT Error:', error)
    }
  })

  // STT 시작
  await startSTT()
}

// 통화 종료
const handleEndCall = async () => {
  await endCall()
}
</script>
```

### 환경변수 설정 (.env)

```bash
# STT 엔진 선택
VITE_STT_ENGINE=web-speech

# LiveKit 설정
VITE_LIVEKIT_URL=wss://your-livekit-server.com

# Clova STT (나중에 사용 시)
# VITE_STT_ENGINE=clova
# VITE_CLOVA_API_KEY=your_api_key
# VITE_CLOVA_SECRET_KEY=your_secret_key
```

---

## ⚠️ TODO 리스트

### 우선순위 높음
- [ ] `livekitService.js` - publishLocalAudio() 구현
- [ ] `useAudioStream.js` - processAudioStream() 실제 구현
- [ ] `useAudioStream.js` - 버퍼링 로직 구현 (200ms)
- [ ] `useAudioStream.js` - 리샘플링 로직 구현 (16kHz)

### 우선순위 중간
- [ ] `clovaEngine.js` - Clova STT 엔진 구현
- [ ] TestRTC.vue의 오디오 파이프라인 코드 이식
- [ ] ScriptProcessorNode → AudioWorklet 전환 (성능 개선)

### 우선순위 낮음
- [ ] `whisperEngine.js` - Whisper STT 엔진 구현
- [ ] 오디오 시각화 (waveform, volume meter)
- [ ] 네트워크 품질 모니터링

---

## 📚 참고 코드

### TestRTC.vue에서 가져올 부분
- `startAudioPipelineForRemoteTrack()` - 오디오 파이프라인 구성
- `createLocalAudioTrack()` - 로컬 마이크 발행
- WebAudio API 설정 (AudioContext, Delay, Analyser 등)

### 구현 시 주의사항
1. **AudioContext는 사용자 제스처 후 활성화** (브라우저 정책)
2. **ScriptProcessorNode는 deprecated** → AudioWorklet 권장
3. **리샘플링은 성능 이슈 가능** → Web Worker 고려
4. **버퍼링은 메모리 관리 중요** → Ring Buffer 사용

---

## 🔧 디버깅

```javascript
// STT 엔진 확인
console.log(sttService.getCurrentEngine()) // 'web-speech'

// LiveKit 연결 상태
console.log(livekitService.isConnected()) // true/false

// AudioContext 상태
const { audioContext } = useAudioStream()
console.log(audioContext.value?.state) // 'running' / 'suspended'
```
