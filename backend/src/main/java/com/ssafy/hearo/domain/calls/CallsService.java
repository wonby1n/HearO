package com.ssafy.hearo.domain.calls;


import org.springframework.stereotype.Service;

public interface CallsService {
    String createToken(String identity, String roomName);
}
