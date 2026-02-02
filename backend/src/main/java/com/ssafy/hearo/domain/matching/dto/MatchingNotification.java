package com.ssafy.hearo.domain.matching.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 매칭 완료 시 WebSocket으로 전송되는 알림 DTO
 */
public class MatchingNotification {

    /**
     * 고객에게 전송되는 매칭 알림
     */
    @Getter
    @Builder
    public static class CustomerNotification {
        private String customerId;
        private String status;           // "MATCHED"
        private String identity;         // LiveKit 접속용 identity
        private String roomName;         // LiveKit 방 이름
        private Long timestamp;
    }

    /**
     * 상담원에게 전송되는 매칭 알림
     */
    @Getter
    @Builder
    public static class CounselorNotification {
        private String type;             // "MATCH_ASSIGNED"
        private Long registrationId;     // 접수 ID
        private Integer customerId;      // 고객 ID (DB PK)
        private String identity;         // LiveKit 접속용 identity
        private String roomName;         // LiveKit 방 이름
        private Long timestamp;
    }
}
