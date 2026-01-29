package com.ssafy.hearo.domain.calls.service;


import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CallsServiceImpl implements CallsService {

    @Value("${livekit.api-key}")
    private String livekitApiKey;

    @Value("${livekit.api-secret}")
    private String livekitApiSecret;

    @Override
    public String createToken(String identity, String roomName){

        AccessToken token = new AccessToken(livekitApiKey, livekitApiSecret);
        token.setIdentity(identity);
        token.setTtl(Duration.ofMinutes(30).toMillis());
        token.addGrants(
                new RoomJoin(true),
                new RoomName(roomName)
        );

        return token.toJwt();
    }
}
