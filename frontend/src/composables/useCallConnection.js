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
    // 외부에서 LiveKit 이벤트를 더 받고 싶으면 options.livekitHandlers로 넘기기
    ...(options.livekitHandlers || {}),
  });

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
      console.log("[CallConnection] 매칭 완료, LiveKit 토큰 요청 중...");

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

      // 3. 마이크 활성화 (실패해도 계속 진행)
      try {
        await livekit.enableMicrophone();
      } catch (micError) {
        console.warn("[CallConnection] 마이크 자동 활성화 실패 (수동으로 켜야 함):", micError);
        // 에러를 무시하고 계속 진행
      }

      connectionState.value = "connected";
      console.log("[CallConnection] 통화 연결 완료");

      // 4. 콜백 호출 (페이지 이동은 호출자가 결정)
      options.onMatched?.(matchData);
    } catch (err) {
      console.error("[CallConnection] 연결 실패:", err);
      error.value = err;
      connectionState.value = "error";
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
  };
}
