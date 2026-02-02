<template>
  <div class="rtc-card">
    <h2 class="title">📞 고객 통화 화면</h2>

    <!-- 상태 배지 -->
    <div class="status-row">
      <span class="badge" :class="statusClass">{{ statusLabel }}</span>
      <span class="identity">ID: {{ identity }}</span>
      <span class="room">Room: {{ roomName }}</span>
    </div>

    <!-- 연결 버튼 -->
    <div class="btn-row">
      <button class="btn primary" @click="connectAsCustomer" :disabled="connected">
        연결
      </button>
      <button class="btn danger" @click="disconnect" :disabled="!connected">
        종료
      </button>
    </div>

    <!-- STT -->
    <div class="stt-box">
      <div class="stt-header">
        <span>🎙 STT</span>
        <span class="stt-state" :class="{ on: sttOn }">
          {{ sttOn ? "ON" : "OFF" }}
        </span>
      </div>

      <div class="btn-row">
        <button class="btn secondary" @click="startStt" :disabled="!connected || sttOn">
          STT 시작
        </button>
        <button class="btn secondary" @click="stopStt" :disabled="!sttOn">
          STT 중지
        </button>
      </div>

      <div class="stt-result">
        {{ lastText || "음성을 인식하면 여기에 표시됩니다." }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { Room, RoomEvent } from "livekit-client";

/**
 * 고객(Client) 요구사항
 * - 토큰 발급: POST /api/v1/calls/token (identity, roomName)
 * - connect(url, token)
 * - 마이크 오디오 publish
 * - STT는 텍스트만 DataChannel로 전송 (딜레이/저장 X)
 */

const status = ref("idle");
const connected = ref(false);

const identity = ref(`cust-${crypto.randomUUID()}`); // 예: cust-...
const roomName = ref("roomA"); // 필요하면 라우트/파라미터로 주입

const lastText = ref("");
const sttOn = ref(false);

const room = new Room();
let recognition = null;

/** 1) 문서 기준 토큰 발급 API 호출
 * POST /api/v1/calls/token
 * body: { identity, roomName }
 * resp: { token, url }
 */
async function issueToken(identityVal, roomNameVal) {
  const res = await fetch("http://127.0.0.1:8080/api/v1/calls/token", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ identity: identityVal, roomName: roomNameVal }),
  });
  if (!res.ok) throw new Error(`Token API failed: ${res.status}`);
  return await res.json(); // { token, url }
}

/** 2) LiveKit 연결 + 마이크 publish */
async function connectAsCustomer() {
  try {
    status.value = "token issuing...";
    const { token, url } = await issueToken(identity.value, roomName.value);
    console.log(url);
    status.value = "connecting...";
    await room.connect(url, token);

    // 고객은 "음성"만 보내면 됨: 마이크 publish
    status.value = "enabling mic...";
    await room.localParticipant.setMicrophoneEnabled(true);

    connected.value = true;
    status.value = "connected";

    // (선택) 상담원에서 내려주는 data가 있으면 받기
    room.on(RoomEvent.DataReceived, (payload, participant, kind, topic) => {
      const msg = safeDecodeJson(payload);
      console.log("data received", { from: participant?.identity, topic, msg });
    });
  } catch (e) {
    console.error(e);
    status.value = "error";
    connected.value = false;
  }
}

/** 3) STT: 텍스트만 publishData로 전송 */
function startStt() {
  const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SR) {
    alert("이 브라우저는 Web Speech API(STT)를 지원하지 않습니다.");
    return;
  }
  if (!connected.value) {
    alert("먼저 LiveKit에 연결하세요.");
    return;
  }

  recognition = new SR();
  recognition.lang = "ko-KR";
  recognition.interimResults = true; // 중간결과도 보내려면 true
  recognition.continuous = true;

  recognition.onresult = (event) => {
    const r = event.results[event.results.length - 1];
    const text = r?.[0]?.transcript?.trim() ?? "";
    if (!text) return;

    lastText.value = text;
    if (!r.isFinal) return;
    // 정책 선택:
    // 1) final만 보낼 경우: if (!r.isFinal) return;
    // 2) interim도 보내기(지금): 그대로 전송
    sendStt(text, { isFinal: !!r.isFinal });
  };

  recognition.onerror = (e) => console.warn("stt error:", e);
  recognition.onend = () => {
    // 자동 재시작(원하면)
    if (sttOn.value) recognition.start();
  };

  sttOn.value = true;
  recognition.start();
}

function stopStt() {
  sttOn.value = false;
  if (recognition) recognition.stop();
  recognition = null;
}

/** STT 텍스트만 전송 (상담원은 DataReceived로 받음) */
function sendStt(text, extra = {}) {
  const msg = {
    type: "stt",
    text,
    ts: Date.now(),
    ...extra,
  };
  const bytes = new TextEncoder().encode(JSON.stringify(msg));

  // STT는 순서/전달 보장이 유리한 편이라 reliable 추천
  room.localParticipant.publishData(bytes, { reliable: true, topic: "stt" });
}

/** 4) 종료 */
async function disconnect() {
  stopStt();
  try {
    await room.disconnect();
  } finally {
    connected.value = false;
    status.value = "disconnected";
  }
}

function safeDecodeJson(payload) {
  try {
    const str = new TextDecoder().decode(payload);
    return JSON.parse(str);
  } catch {
    return null;
  }
}
</script>
<style scoped>
/* 전체 카드 */
.rtc-card {
  width: 420px;
  margin: 40px auto;
  padding: 24px;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  font-family: system-ui, -apple-system, BlinkMacSystemFont;
}

/* 제목 */
.title {
  margin-bottom: 16px;
  text-align: center;
  font-weight: 700;
}

/* 상태 영역 */
.status-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #555;
}

/* 상태 배지 */
.badge {
  align-self: flex-start;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

/* 상태 색상 */
.badge.idle {
  background: #eee;
  color: #666;
}

.badge.connected {
  background: #e8f7ef;
  color: #1e8e5a;
}

.badge.error {
  background: #fdecea;
  color: #d93025;
}

/* 버튼 공통 */
.btn-row {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.btn {
  flex: 1;
  padding: 10px;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: 0.15s;
}

.btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

/* 버튼 타입 */
.btn.primary {
  background: #2563eb;
  color: white;
}

.btn.primary:hover:not(:disabled) {
  background: #1d4ed8;
}

.btn.secondary {
  background: #f3f4f6;
  color: #111;
}

.btn.danger {
  background: #ef4444;
  color: white;
}

/* STT 박스 */
.stt-box {
  margin-top: 12px;
  padding: 14px;
  border-radius: 10px;
  background: #f9fafb;
}

.stt-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: 600;
}

/* STT 상태 */
.stt-state {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #eee;
}

.stt-state.on {
  background: #dcfce7;
  color: #15803d;
}

/* STT 결과 */
.stt-result {
  min-height: 48px;
  padding: 10px;
  border-radius: 6px;
  background: white;
  border: 1px solid #e5e7eb;
  font-size: 13px;
  color: #333;
}
</style>