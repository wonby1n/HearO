import { ref } from "vue";
import { useRouter } from "vue-router";
import { useLiveKit } from "@/composables/useLiveKit";
import { useMatchingNotification } from "@/composables/useMatchingNotification";
import { useCallStore } from "@/stores/call";
import axios from "axios";

export function useCallConnection(role = "customer", options = {}) {
  const router = useRouter();
  const callStore = useCallStore();
  const connectionState = ref("idle"); // idle, waiting, matched, connecting, connected
  const error = ref(null);
  const matchedData = ref(null); // 매칭 데이터 저장

  // LiveKit composable
  const livekit = useLiveKit({
    // 상담원은 원격(고객) 오디오를 그대로 재생하면 딜레이/차단을 못 거므로 자동 attach를 끔
    autoAttachRemoteAudio: role !== 'counselor',
    onDisconnected: (reason) => {
      console.log("[CallConnection] 통화 종료:", reason);
      connectionState.value = "idle";
    },
    onError: (err) => {
      error.value = err;
      connectionState.value = "error";
    },
    // 고객의 경우 상담사 입장 감지
    onParticipantConnected: (participant) => {
      console.log("[CallConnection] 참가자 입장:", participant.identity);

      // 고객이고, 상담사가 입장한 경우
      if (role === "customer" && participant.identity.startsWith("counselor_")) {
        console.log("[CallConnection] 상담사 입장 감지!");
        options.onCounselorJoined?.();
      }
    },
    // 외부에서 LiveKit 이벤트를 더 받고 싶으면 options.livekitHandlers로 넘기기
    ...(options.livekitHandlers || {}),
  });

  // LiveKit 연결 수행 (내부 함수)
  const performConnection = async (matchData) => {
    const { roomName, identity } = matchData;

    // 1. LiveKit 토큰 요청
    const response = await axios.post("/api/v1/calls/token", {
      identity,
      roomName,
    });

    const { token, url } = response.data;

    connectionState.value = "connecting";
    console.log("[CallConnection] LiveKit 연결 중...");

    // 2. LiveKit 룸 연결
    const room = await livekit.connect(url, token);

    // LiveKit room을 call store에 저장 (페이지 이동 시에도 유지)
    callStore.setLivekitRoom(room);
    console.log("[CallConnection] LiveKit room을 store에 저장");

    // 마이크는 각 통화 화면(ClientCallView, CounselorCallView)에서 활성화
    console.log("[CallConnection] 마이크는 통화 화면 진입 시 활성화 예정");

    connectionState.value = "connected";
    console.log("[CallConnection] 통화 연결 완료");

    // 고객의 경우: 상담사 입장 대기
    if (role === "customer") {
      console.log("[CallConnection] 상담사 입장 대기 중...");
    }
  };

  // 매칭 완료 시 호출될 핸들러
  const handleMatched = async (matchData) => {
    // 중복 매칭 방지: 이미 연결 중이거나 연결된 상태면 무시
    if (connectionState.value === "connecting" || connectionState.value === "connected" || connectionState.value === "matched") {
      console.warn("[CallConnection] 이미 연결 중이므로 중복 매칭 무시:", matchData);
      return;
    }

    try {
      connectionState.value = "matched";
      matchedData.value = matchData;
      console.log("[CallConnection] 매칭 완료");

      // 상담사의 경우: 자동 연결하지 않고 매칭 데이터만 저장
      if (role === "counselor") {
        console.log("[CallConnection] 상담사 모드 - 모달 확인 대기 중 (LiveKit 연결 지연)");
        options.onMatched?.(matchData);
        return;
      }

      // 고객의 경우: 즉시 연결
      console.log("[CallConnection] 고객 모드 - LiveKit 토큰 요청 중...");
      await performConnection(matchData);
      options.onMatched?.(matchData);
    } catch (err) {
      console.error("[CallConnection] 연결 실패:", err);
      error.value = err;
      connectionState.value = "error";
    }
  };

  // 수동으로 LiveKit 연결 시작 (상담사용)
  const connectToCall = async () => {
    if (!matchedData.value) {
      console.error("[CallConnection] 매칭 데이터가 없습니다");
      return;
    }

    if (connectionState.value === "connecting" || connectionState.value === "connected") {
      console.warn("[CallConnection] 이미 연결 중이거나 연결되어 있습니다");
      return;
    }

    try {
      console.log("[CallConnection] 상담사 확인 완료 - LiveKit 토큰 요청 중...");
      await performConnection(matchedData.value);
    } catch (err) {
      console.error("[CallConnection] 연결 실패:", err);
      error.value = err;
      connectionState.value = "error";
      throw err;
    }
  };

  // 통화 화면으로 이동하는 헬퍼 함수
  const navigateToCall = () => {
    if (role === "customer") {
      router.push("/client/call");
    } else {
      router.push("/counselor/call");
    }
  };

  // 매칭 알림 composable (나중에 초기화)
  let matching = null;

  // 대기 시작 (고객용)
  const startWaiting = (customerId) => {
    if (!customerId) {
      console.error("[CallConnection] customerId가 필요합니다");
      return;
    }

    // 기존 STOMP 연결이 있으면 먼저 정리 (중복 구독 방지)
    if (matching) {
      console.log("[CallConnection] 기존 STOMP 연결 정리 중...");
      matching.disconnect();
      matching = null;
    }

    connectionState.value = "waiting";

    // 매칭 알림 composable 초기화 (customerId와 함께)
    matching = useMatchingNotification({
      customerId,
      onMatched: handleMatched,
    });

    matching.connect();
  };

  // 대기 시작 (상담원용)
  const startListening = (counselorId) => {
    if (!counselorId) {
      console.error("[CallConnection] counselorId가 필요합니다");
      return;
    }

    // 기존 STOMP 연결이 있으면 먼저 정리 (중복 구독 방지)
    if (matching) {
      console.log("[CallConnection] 기존 STOMP 연결 정리 중...");
      matching.disconnect();
      matching = null;
    }

    connectionState.value = "waiting";

    // 매칭 알림 composable 초기화 (counselorId와 함께)
    matching = useMatchingNotification({
      counselorId,
      onMatched: handleMatched,
    });

    matching.connect();
  };

  // 연결 종료
  const disconnect = async (disconnectLiveKit = true) => {
    if (matching) {
      matching.disconnect();
    }

    // LiveKit 연결은 선택적으로 끊기 (페이지 이동 시에는 유지)
    if (disconnectLiveKit) {
      await livekit.disconnect();
      callStore.setLivekitRoom(null);
      console.log("[CallConnection] LiveKit 연결 종료 및 store 정리");
    } else {
      console.log("[CallConnection] STOMP만 종료, LiveKit 연결은 유지");
    }

    connectionState.value = "idle";
  };

  return {
    connectionState,
    matchedData,
    error,
    livekit,
    startWaiting,
    startListening,
    disconnect,
    handleMatched,
    navigateToCall,
    connectToCall, // 상담사가 모달 확인 시 수동 연결용
  };
}
