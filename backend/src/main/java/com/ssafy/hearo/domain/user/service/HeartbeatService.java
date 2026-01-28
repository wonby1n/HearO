package com.ssafy.hearo.domain.user.service;

import java.util.Set;

public interface HeartbeatService {

    /**
     * Set heartbeat status for a counselor
     *
     * @param userId the counselor's user ID
     * @param isActive true to activate heartbeat, false to deactivate
     */
    void setHeartbeat(Long userId, boolean isActive);

    /**
     * Check if a counselor has an active heartbeat
     *
     * @param userId the counselor's user ID
     * @return true if heartbeat is active
     */
    boolean isHeartbeatActive(Long userId);

    /**
     * Get all counselor IDs with active heartbeat
     *
     * @return set of counselor IDs
     */
    Set<Long> getActiveHeartbeatCounselorIds();
}
