package com.ssafy.hearo.calls;


import org.springframework.stereotype.Service;

public interface CallsService {
    String createToken(String identity, String roomName);
}
