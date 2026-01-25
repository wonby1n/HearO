<template>
  <div class="wrap">
    <h2>LiveKit 음성 통화 테스트</h2>

    <section class="card">
      <h3>1) 토큰 발급</h3>

      <div class="row">
        <label>Backend Base URL</label>
        <input v-model="backendBaseUrl" placeholder="http://localhost:8080" />
      </div>

      <div class="row">
        <label>Room Name</label>
        <input v-model="roomName" placeholder="roomA" />
      </div>

      <div class="row">
        <label>Identity</label>
        <input v-model="identity" placeholder="user123" />
      </div>

      <div class="actions">
        <button :disabled="loadingIssue" @click="issueToken">
          {{ loadingIssue ? "요청 중..." : "토큰 발급 요청" }}
        </button>
      </div>

      <div class="result" v-if="issued">
        <p><b>LiveKit URL:</b> {{ livekitUrl }}</p>
        <p><b>Token:</b></p>
        <textarea readonly rows="3">{{ token }}</textarea>
      </div>

      <div class="result error" v-if="issueError">
        <p><b>에러:</b> {{ issueError }}</p>
      </div>
    </section>

    <section class="card">
      <h3>2) LiveKit 연결</h3>

      <div class="actions">
        <button :disabled="!issued || connecting || connected" @click="connectLiveKit">
          {{ connecting ? "연결 중..." : connected ? "연결됨" : "연결" }}
        </button>
        <button class="ghost" :disabled="!connected" @click="disconnectLiveKit">
          연결 해제
        </button>
      </div>

      <div class="result" v-if="connected">
        <p><b>Room:</b> {{ info.room }}</p>
        <p><b>Local Identity:</b> {{ info.identity }}</p>
        <p><b>Remote Participants:</b> {{ info.remoteCount }}</p>
      </div>

      <div class="result error" v-if="connectError">
        <p><b>연결 에러:</b> {{ connectError }}</p>
      </div>
    </section>

    <section class="card">
      <h3>3) 마이크 Publish / 음성 수신</h3>

      <div class="actions">
        <button :disabled="!connected || micStarting || micOn" @click="startMic">
          {{ micStarting ? "마이크 켜는 중..." : micOn ? "마이크 ON" : "마이크 켜기(발송)" }}
        </button>
        <button class="ghost" :disabled="!micOn" @click="stopMic">마이크 끄기</button>

        <!-- ✅ 연결 전에도 켤 수 있게 변경 -->
        <button class="ghost" :disabled="audioEnabled" @click="enableAudio">
          {{ audioEnabled ? "오디오 엔진 ON" : "오디오 엔진 켜기(필수)" }}
        </button>
      </div>

      <div class="result">
        <p><b>원격 오디오(딜레이 스트리밍):</b></p>
        <small class="hint">
          원격 오디오는 {{ DELAY_SEC.toFixed(1) }}초 딜레이로 재생되며, 그 딜레이 동안 STT+분석으로
          유해 구간만 자동 차단됩니다. (브라우저 정책 때문에 ‘오디오 엔진 켜기’ 버튼을 먼저 눌러주세요)
        </small>
      </div>

      <div class="result" v-if="micOn">
        <p>✅ 로컬 마이크 트랙이 publish 되었습니다.</p>
        <small class="hint">다른 탭/기기에서도 같은 roomName으로 접속 후 마이크 켜면 서로 들립니다.</small>
      </div>

      <div class="result error" v-if="micError">
        <p><b>마이크 에러:</b> {{ micError }}</p>
      </div>
    </section>

    <section class="card">
      <h3>로그</h3>
      <div class="log">
        <div v-for="(line, idx) in logs" :key="idx" class="log-line">{{ line }}</div>
      </div>
    </section>

    <section class="card">
      <h3>4) STT(자막) 전송</h3>

      <div class="actions">
        <button :disabled="!connected || sttStarting || sttOn" @click="startStt">
          {{ sttStarting ? "STT 켜는 중..." : sttOn ? "STT ON" : "STT 켜기(인식/전송)" }}
        </button>
        <button class="ghost" :disabled="!sttOn" @click="stopStt">STT 끄기</button>
      </div>

      <div class="result" v-if="sttOn">
        <p><b>내 STT:</b> {{ sttLocalPreview }}</p>
        <small class="hint">인식된 문장이 상대에게 전송됩니다.</small>
      </div>

      <div class="result">
        <p><b>상대 자막:</b></p>

        <div class="subtitles">
          <div
            v-for="(m, i) in sttMessages"
            :key="i"
            class="subtitle-line clickable"
            :class="{ toxic: m.is_toxic, revealed: m.revealed }"
            @click="onClickSttMessage(m)"
            title="클릭: 모자이크 해제 + (차단중이면) 차단 해제"
          >
            <b>{{ m.from }}:</b>

            <span v-if="m.loading" class="masked placeholder">분석중…</span>

            <span v-else-if="m.is_toxic && !m.revealed" class="masked">
              {{ m.text }}
            </span>

            <span v-else>
              {{ m.text }}
            </span>

            <span v-if="!m.loading && m.toxicity != null" class="score">
              — 점수: {{ m.toxicity.toFixed(2) }}
              <b v-if="m.is_toxic" style="color:red;"> 🚨</b>
            </span>

            <div v-if="m.is_toxic && !m.loading" class="toxic-actions">
              <small v-if="m.blockPlanned && !m.revealed">
                자동 차단됨(다음 STT 올 때까지). 클릭하면 차단 해제
              </small>
              <small v-else-if="!m.blockPlanned && !m.revealed">
                차단됨. 클릭하면 차단 해제
              </small>
              <small v-else>확인됨</small>
            </div>
          </div>
        </div>
      </div>

      <div class="result error" v-if="sttError">
        <p><b>STT 에러:</b> {{ sttError }}</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeUnmount } from "vue";
import { Room, RoomEvent, createLocalAudioTrack } from "livekit-client";

/* =========================
 * 1) UI/상태 (Vue state)
 * ========================= */
const backendBaseUrl = ref("http://localhost:8080");
const roomName = ref("roomA");
const identity = ref("user123");

const loadingIssue = ref(false);
const issued = ref(false);
const token = ref("");
const livekitUrl = ref("");
const issueError = ref("");

const connecting = ref(false);
const connected = ref(false);
const connectError = ref("");

const micStarting = ref(false);
const micOn = ref(false);
const micError = ref("");

const sttStarting = ref(false);
const sttOn = ref(false);
const sttError = ref("");
const sttLocalPreview = ref("");
const sttMessages = ref([]); // { from, text, ... }

const logs = ref([]);

/** LiveKit */
const room = ref(null);
const localAudioTrack = ref(null);

/** room 정보 표시용 */
const info = reactive({
  room: "",
  identity: "",
  remoteCount: 0,
});

/* =========================
 * 2) 상수/공통 유틸
 * ========================= */
const DELAY_SEC = 4.0;
const RMS_THRESHOLD = 0.003; // WebAudio 입력 살아있다고 판단할 RMS 기준
const POSTPAD_MS = 150; // 다음 STT 도착 후, 딜레이+여유시간 뒤 unmute

/** identity -> sid 캐시 */
const identityToSid = new Map();

/** STT(WebSpeech) */
let recognition = null;

function log(msg) {
  const ts = new Date().toLocaleTimeString();
  logs.value.unshift(`[${ts}] ${msg}`);
}

function safeMsg(e) {
  return e?.message ?? String(e);
}

function getRemoteParticipants(r) {
  const ps = r?.participants;
  if (ps && typeof ps.values === "function") return Array.from(ps.values());
  if (Array.isArray(ps)) return ps;
  if (ps && typeof ps === "object") return Object.values(ps);
  return [];
}

function updateRemoteCount() {
  info.remoteCount = getRemoteParticipants(room.value).length;
}

/* =========================
 * 3) WebAudio: Delay + Gain pipeline
 * ========================= */
const audioEnabled = ref(false);
let audioCtx = null;

/**
 * sid -> pipeline
 * {
 *   sid, participant,
 *   stream, source, delay, analyser, gain,
 *   meterTimer,
 *   pendingBlock: { unmuteTimer? } | null,
 *   isMuted,
 *   fallbackEl
 * }
 */
const audioPipelines = new Map();

function ensureAudioCtx() {
  if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)();
  // gesture 없이 resume 실패할 수 있지만, 일단 시도
  if (audioCtx.state !== "running") audioCtx.resume?.().catch(() => {});
  return audioCtx;
}

async function enableAudio() {
  try {
    const ctx = ensureAudioCtx();
    log(`AudioContext state(before)=${ctx.state}`);

    try {
      await ctx.resume();
    } catch (e) {
      log(`⚠️ audioCtx.resume() 실패: ${safeMsg(e)}`);
    }

    log(`AudioContext state(after)=${ctx.state}`);
    audioEnabled.value = (ctx.state === "running");

    if (!audioEnabled.value) {
      log("⚠️ 오디오가 아직 running이 아닙니다. 브라우저 자동재생/사이트 소리 설정을 확인하세요.");
    }
  } catch (e) {
    log(`오디오 엔진 활성화 실패: ${safeMsg(e)}`);
  }
}

function ensureFallbackEl(sid) {
  const id = `lk-fallback-${sid}`;
  let el = document.getElementById(id);
  if (!el) {
    el = document.createElement("audio");
    el.id = id;
    el.autoplay = true;
    el.playsInline = true;
    el.muted = false;
    el.style.display = "none";
    document.body.appendChild(el);
  }
  return el;
}

function stopAudioPipeline(sid) {
  const p = audioPipelines.get(sid);
  if (!p) return;

  try {
    if (p.pendingBlock?.unmuteTimer) clearTimeout(p.pendingBlock.unmuteTimer);
    if (p.meterTimer) clearInterval(p.meterTimer);

    try { p.source?.disconnect(); } catch {}
    try { p.delay?.disconnect(); } catch {}
    try { p.analyser?.disconnect(); } catch {}
    try { p.gain?.disconnect(); } catch {}

    try { p.stream?.getTracks?.().forEach((t) => t.stop()); } catch {}

    // fallback 정리
    try {
      if (p.fallbackEl) {
        p.fallbackEl.srcObject = null;
        p.fallbackEl.remove();
      } else {
        document.getElementById(`lk-fallback-${sid}`)?.remove();
      }
    } catch {}

    audioPipelines.delete(sid);
    log(`🧹 오디오 파이프라인 제거: ${sid}`);
  } catch (e) {
    log(`⚠️ 파이프라인 정리 오류: ${safeMsg(e)}`);
  }
}

function cleanupAllPipelines() {
  for (const [sid] of audioPipelines) stopAudioPipeline(sid);
  audioPipelines.clear();
}

function startAudioPipelineForRemoteTrack(lkTrack, participant) {
  const sid = participant?.sid ?? participant?.identity ?? "unknownParticipant";
  const mediaTrack = lkTrack?.mediaStreamTrack;
  if (!mediaTrack) return;

  // 기존 있으면 교체
  if (audioPipelines.has(sid)) stopAudioPipeline(sid);

  // (1) fallback <audio> 로 “일단 무조건 들리게”
  const fallbackEl = ensureFallbackEl(sid);
  try {
    lkTrack.attach(fallbackEl);
    fallbackEl.play?.().catch((e) => log(`⚠️ fallback <audio>.play 실패: ${safeMsg(e)}`));
    log(`🔊 fallback <audio> attach: ${participant.identity} (sid=${sid})`);
  } catch (e) {
    log(`⚠️ fallback attach 실패: ${safeMsg(e)}`);
  }

  // (2) WebAudio 파이프라인 구성
  const ctx = ensureAudioCtx();
  if (ctx.state !== "running") {
    log(`⚠️ AudioContext가 실행중이 아닙니다(state=${ctx.state}). "오디오 엔진 켜기" 버튼을 눌러주세요.`);
  }

  const stream = new MediaStream([mediaTrack]);
  const source = ctx.createMediaStreamSource(stream);

  const delay = ctx.createDelay(10.0);
  delay.delayTime.value = DELAY_SEC;

  const analyser = ctx.createAnalyser();
  analyser.fftSize = 2048;

  const gain = ctx.createGain();
  gain.gain.value = 1.0;

  // source -> delay -> analyser -> gain -> destination
  source.connect(delay);
  delay.connect(analyser);
  analyser.connect(gain);
  gain.connect(ctx.destination);

  // (3) RMS 미터: WebAudio 입력이 실제로 살아있는지 확인
  const buf = new Float32Array(analyser.fftSize);
  const meterTimer = setInterval(() => {
    analyser.getFloatTimeDomainData(buf);
    let sum = 0;
    for (let i = 0; i < buf.length; i++) sum += buf[i] * buf[i];
    const rms = Math.sqrt(sum / buf.length);

    log(`📶 RMS(${participant.identity})=${rms.toFixed(4)} gain=${gain.gain.value}`);

    // WebAudio 입력이 정상이라면, fallback 이중재생을 막기 위해 mute
    if (rms > RMS_THRESHOLD && fallbackEl && !fallbackEl.muted) {
      fallbackEl.muted = true;
      log(`🔇 fallback <audio> muted (WebAudio 정상 입력 감지) sid=${sid}`);
    }
  }, 1000);

  audioPipelines.set(sid, {
    sid,
    participant,
    stream,
    source,
    delay,
    analyser,
    gain,
    meterTimer,
    pendingBlock: null,
    isMuted: false,
    fallbackEl,
  });

  log(`🎵 원격 오디오 파이프라인 생성: ${participant.identity} (delay=${DELAY_SEC}s, sid=${sid})`);
}

/* =========================
 * 4) 차단 로직(B안)
 * - 유해 감지 즉시 mute
 * - 다음 STT 도착 시점 기준으로 (딜레이+여유) 후 unmute
 * ========================= */
function markToxicAndBlockUntilNextStt(sid) {
  const p = audioPipelines.get(sid);
  if (!p) return;

  if (p.pendingBlock?.unmuteTimer) clearTimeout(p.pendingBlock.unmuteTimer);

  p.gain.gain.value = 0.0;
  p.isMuted = true;
  p.pendingBlock = {}; // "차단중" 표시용

  log(`🔇 즉시 차단(감지 즉시) sid=${sid}`);
}

function onNextSttArrived(sid) {
  const p = audioPipelines.get(sid);
  if (!p || !p.pendingBlock) return;

  if (p.pendingBlock.unmuteTimer) clearTimeout(p.pendingBlock.unmuteTimer);

  const now = performance.now();
  const unmuteDelay = Math.max(0, (DELAY_SEC * 1000) + POSTPAD_MS);

  p.pendingBlock.unmuteTimer = setTimeout(() => {
    p.gain.gain.value = 1.0;
    p.isMuted = false;
    p.pendingBlock = null;

    // fallback은 RMS가 올라오면 자동 mute 되므로 여기서는 건드리지 않음
    log(`🔈 차단 해제(sid=${sid})`);
  }, unmuteDelay);
}

/* =========================
 * 5) API: 토큰 발급
 * ========================= */
async function issueToken() {
  issueError.value = "";
  issued.value = false;

  loadingIssue.value = true;
  try {
    const url = `${backendBaseUrl.value.replace(/\/$/, "")}/api/v1/calls/token`;
    const payload = { identity: identity.value, roomName: roomName.value };

    log(`POST ${url}`);
    const res = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    const data = await res.json().catch(() => null);
    if (!res.ok) throw new Error(data?.message || data?.error || `HTTP ${res.status} ${res.statusText}`);
    if (!data?.token || !data?.url) throw new Error("응답에 token/url이 없습니다. 백엔드 응답 형태를 확인하세요.");

    token.value = data.token;
    livekitUrl.value = data.url;
    issued.value = true;

    log("토큰 발급 성공");
  } catch (e) {
    issueError.value = safeMsg(e);
    log(`토큰 발급 실패: ${issueError.value}`);
  } finally {
    loadingIssue.value = false;
  }
}

/* =========================
 * 6) LiveKit: 연결/해제 + 이벤트 바인딩
 * ========================= */
function bindRoomEvents(r) {
  // DataReceived: STT 수신
  r.on(RoomEvent.DataReceived, async (payload, participant) => {
    try {
      const text = new TextDecoder().decode(payload);
      const msg = JSON.parse(text);

      if (msg?.type !== "stt") {
        log(`📩 dataReceived(unknown): ${text}`);
        return;
      }

      const from = msg.from ?? participant?.identity ?? "unknown";
      const receivedText = (msg.text ?? "").trim();
      if (receivedText.length < 2) return;

      // sid 결정 (가능하면 participant.sid 우선)
      let sid = participant?.sid ?? identityToSid.get(from) ?? "unknownSid";
      if (sid !== "unknownSid") onNextSttArrived(sid);

      const item = {
        from,
        text: receivedText,
        ts: msg.ts ?? Date.now(),
        participantSid: sid,
        toxicity: null,
        is_toxic: null,
        loading: true,
        revealed: false,
        blockPlanned: false,
      };
      sttMessages.value.unshift(item);

      log(`📝 STT 수신: ${from}: ${receivedText}`);

      const analysis = await analyzeViolence(receivedText);

      item.toxicity = analysis.toxicity;
      item.is_toxic = analysis.is_toxic;
      item.loading = false;

      log(`🧪 analysis: toxic=${analysis.is_toxic} score=${Number(analysis.toxicity).toFixed(2)}`);

      if (item.is_toxic) {
        item.revealed = false;
        item.blockPlanned = (sid !== "unknownSid");

        if (sid !== "unknownSid") markToxicAndBlockUntilNextStt(sid);
        log(`🚨 유해성 감지: ${from} (${Number(analysis.toxicity).toFixed(2)})`);
      } else {
        item.revealed = true;
      }
    } catch (e) {
      log(`📩 dataReceived 처리 실패: ${safeMsg(e)}`);
    }
  });

  // TrackSubscribed: 원격 오디오 처리
  r.on(RoomEvent.TrackSubscribed, (track, publication, participant) => {
    if (track.kind !== "audio") return;

    identityToSid.set(participant.identity, participant.sid);
    log(`원격 오디오 구독: ${participant.identity} sid=${participant.sid}`);
    log(`🔎 pub muted=${publication?.isMuted} subscribed=${publication?.isSubscribed} source=${publication?.source}`);

    startAudioPipelineForRemoteTrack(track, participant);
  });

  r.on(RoomEvent.TrackUnsubscribed, (track, _publication, participant) => {
    if (track.kind !== "audio") return;
    const sid = participant?.sid ?? participant?.identity ?? "unknownParticipant";
    log(`원격 오디오 구독 해제: ${participant.identity}`);
    stopAudioPipeline(sid);
  });

  r.on(RoomEvent.ParticipantConnected, (p) => {
    identityToSid.set(p.identity, p.sid);
    updateRemoteCount();
    log(`participantConnected: ${p.identity} sid=${p.sid}`);
  });

  r.on(RoomEvent.ParticipantDisconnected, (p) => {
    updateRemoteCount();
    log(`participantDisconnected: ${p.identity}`);
    const sid = p?.sid ?? p?.identity ?? "unknownParticipant";
    stopAudioPipeline(sid);
  });

  r.on(RoomEvent.Disconnected, () => {
    connected.value = false;
    micOn.value = false;
    log("disconnected");
    cleanupAllPipelines();
  });
}

async function connectLiveKit() {
  connectError.value = "";
  connecting.value = true;

  try {
    // 사용자 클릭 직후 resume 시도 (제스처 컨텍스트 기대)
    enableAudio(); // await 안 걸어도 됨

    await disconnectLiveKit(); // 기존 연결 정리
    stopStt();
    await stopMic();

    const r = new Room();
    room.value = r;

    bindRoomEvents(r);

    log(`LiveKit connect: ${livekitUrl.value}`);
    await r.connect(livekitUrl.value, token.value);

    connected.value = true;
    info.room = r.name ?? "";
    info.identity = r.localParticipant?.identity ?? "";
    updateRemoteCount();

    log("LiveKit 연결 성공");
  } catch (e) {
    connectError.value = safeMsg(e);
    connected.value = false;
    log(`LiveKit 연결 실패: ${connectError.value}`);
  } finally {
    connecting.value = false;
  }
}

async function disconnectLiveKit() {
  await stopMic();

  if (room.value) {
    try { await room.value.disconnect(); } catch {}
    room.value = null;
  }

  cleanupAllPipelines();
  connected.value = false;
}

/* =========================
 * 7) 마이크 publish/unpublish
 * ========================= */
async function startMic() {
  micError.value = "";
  micStarting.value = true;

  try {
    if (!room.value) throw new Error("먼저 LiveKit에 연결하세요.");

    const track = await createLocalAudioTrack();
    localAudioTrack.value = track;

    await room.value.localParticipant.publishTrack(track);
    micOn.value = true;

    log("로컬 마이크 publish 성공");
  } catch (e) {
    micError.value = safeMsg(e);
    log(`마이크 publish 실패: ${micError.value}`);
  } finally {
    micStarting.value = false;
  }
}

async function stopMic() {
  try {
    if (!room.value || !localAudioTrack.value) return;

    room.value.localParticipant.unpublishTrack(localAudioTrack.value);
    localAudioTrack.value.stop();
    localAudioTrack.value = null;

    micOn.value = false;
    log("로컬 마이크 unpublish/stop");
  } catch (e) {
    log(`마이크 끄기 실패: ${safeMsg(e)}`);
  }
}

/* =========================
 * 8) STT (Web Speech)
 * ========================= */
function getSpeechRecognition() {
  return window.SpeechRecognition || window.webkitSpeechRecognition || null;
}

async function startStt() {
  sttError.value = "";
  sttStarting.value = true;

  try {
    if (!room.value) throw new Error("먼저 LiveKit에 연결하세요.");

    const SR = getSpeechRecognition();
    if (!SR) throw new Error("이 브라우저는 Web Speech STT를 지원하지 않습니다. (Chrome/Edge 권장)");

    recognition = new SR();
    recognition.lang = "ko-KR";
    recognition.interimResults = true;
    recognition.continuous = true;

    recognition.onstart = () => {
      sttOn.value = true;
      log("🎙️ STT 시작");
    };

    recognition.onerror = (ev) => {
      const msg = ev?.error ? `SpeechRecognition error: ${ev.error}` : "STT 에러";
      sttError.value = msg;
      log(`STT 오류: ${msg}`);
    };

    recognition.onend = () => {
      if (sttOn.value) log("STT 종료(onend)");
      sttOn.value = false;
    };

    recognition.onresult = async (event) => {
      let interim = "";
      let finalText = "";

      for (let i = event.resultIndex; i < event.results.length; i++) {
        const res = event.results[i];
        const t = res[0]?.transcript ?? "";
        if (res.isFinal) finalText += t;
        else interim += t;
      }

      sttLocalPreview.value = (finalText || interim).trim();
      if (finalText.trim()) await sendStt(finalText.trim());
    };

    recognition.start();
  } catch (e) {
    sttError.value = safeMsg(e);
    log(`STT 시작 실패: ${sttError.value}`);
    sttOn.value = false;
  } finally {
    sttStarting.value = false;
  }
}

function stopStt() {
  try {
    sttOn.value = false;
    sttLocalPreview.value = "";

    if (recognition) {
      recognition.onresult = null;
      recognition.onerror = null;
      recognition.onend = null;
      recognition.stop();
      recognition = null;
    }

    log("🛑 STT 중지");
  } catch (e) {
    log(`STT 중지 실패: ${safeMsg(e)}`);
  }
}

async function sendStt(text) {
  if (!room.value) return;

  const payload = {
    type: "stt",
    from: room.value.localParticipant?.identity ?? identity.value,
    text,
    ts: Date.now(),
  };

  const bytes = new TextEncoder().encode(JSON.stringify(payload));
  room.value.localParticipant.publishData(bytes, { reliable: true });

  sttMessages.value.unshift({
    from: "me",
    text,
    ts: payload.ts,
    participantSid: "me",
    toxicity: null,
    is_toxic: false,
    loading: false,
    revealed: true,
    blockPlanned: false,
  });

  log(`📤 STT 전송: ${text}`);
}

/* =========================
 * 9) 자막 클릭: 모자이크 해제 + 차단 해제
 * ========================= */
async function onClickSttMessage(m) {
  if (m.loading) return;
  if (!m.is_toxic) return;

  m.revealed = true;

  const sid = m.participantSid;
  const p = audioPipelines.get(sid);
  if (!p) {
    log(`ℹ️ 해당 참가자 오디오 파이프라인이 없어 차단 해제는 불가(sid=${sid})`);
    return;
  }

  if (p.pendingBlock?.unmuteTimer) clearTimeout(p.pendingBlock.unmuteTimer);
  p.pendingBlock = null;

  p.gain.gain.value = 1.0;
  p.isMuted = false;

  // 사용자가 수동 해제하면 fallback도 unmute
  if (p.fallbackEl) p.fallbackEl.muted = false;

  log(`✅ 클릭 → 차단 즉시 해제: ${m.from} (sid=${sid})`);
}

/* =========================
 * 10) 분석 API
 * ========================= */
async function analyzeViolence(text) {
  const res = await fetch("http://localhost:8001/analyze", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ text }),
  });
  if (!res.ok) throw new Error(`AI HTTP ${res.status}`);
  return res.json();
}

/* =========================
 * 11) 종료 정리
 * ========================= */
onBeforeUnmount(() => {
  disconnectLiveKit();
  try { audioCtx?.close?.(); } catch {}
  audioCtx = null;
});
</script>


<style scoped>
.wrap {
  max-width: 860px;
  margin: 24px auto;
  padding: 0 16px;
  font-family: system-ui, -apple-system, Segoe UI, Roboto, "Noto Sans KR", sans-serif;
}

.card {
  background: #fff;
  border: 1px solid #e7e7e7;
  border-radius: 12px;
  padding: 16px;
  margin: 14px 0;
}

.row {
  display: grid;
  grid-template-columns: 160px 1fr;
  gap: 10px;
  align-items: center;
  margin: 10px 0;
}

label {
  font-weight: 700;
  color: #222;
}

input {
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 10px;
  outline: none;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  flex-wrap: wrap;
}

button {
  padding: 10px 14px;
  border: 1px solid #222;
  background: #222;
  color: #fff;
  border-radius: 10px;
  cursor: pointer;
}

button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

button.ghost {
  background: transparent;
  color: #222;
}

.result {
  margin-top: 12px;
  padding: 12px;
  border-radius: 10px;
  background: #f6f7ff;
  border: 1px solid #e5e7ff;
}

.result.error {
  background: #fff3f3;
  border: 1px solid #ffd6d6;
}

textarea {
  width: 100%;
  border-radius: 10px;
  border: 1px solid #d9d9d9;
  padding: 10px;
  resize: vertical;
}

.hint {
  color: #666;
  font-size: 12px;
}

.log {
  max-height: 220px;
  overflow: auto;
  background: #111;
  color: #e7e7e7;
  border-radius: 10px;
  padding: 10px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace;
  font-size: 12px;
}

.log-line {
  padding: 2px 0;
}

.subtitles {
  max-height: 220px;
  overflow: auto;
  background: #fff;
  border: 1px solid #e7e7e7;
  border-radius: 10px;
  padding: 10px;
}

.subtitle-line {
  padding: 8px 6px;
  border-bottom: 1px dashed #eee;
}

.subtitle-line.clickable {
  cursor: pointer;
}

.subtitle-line.toxic {
  background: #fff7f7;
  border-left: 3px solid #ff6b6b;
  padding-left: 10px;
}

.masked {
  display: inline-block;
  filter: blur(6px);
  user-select: none;
  padding: 2px 6px;
  border-radius: 6px;
  background: repeating-linear-gradient(
    45deg,
    rgba(0, 0, 0, 0.06),
    rgba(0, 0, 0, 0.06) 6px,
    rgba(0, 0, 0, 0.12) 6px,
    rgba(0, 0, 0, 0.12) 12px
  );
}

.masked.placeholder {
  filter: none;
  background: rgba(0, 0, 0, 0.06);
  color: #666;
}

.score {
  margin-left: 6px;
  color: #666;
  font-size: 12px;
}

.toxic-actions {
  margin-top: 6px;
  color: #a33;
}
</style>
