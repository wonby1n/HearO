package com.ssafy.hearo.calls;


import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CallsServiceImpl implements CallsService {

    private static final String LIVEKIT_API_KEY = "";
    private static final String LIVEKIT_API_SECRET = "";

    @Override
    public String createToken(String identity, String roomName){

        AccessToken token = new AccessToken(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
        token.setIdentity(identity);
        token.setTtl(Duration.ofMinutes(30).toMillis());
        token.addGrants(
                new RoomJoin(true),
                new RoomName(roomName)
        );

        return token.toJwt();
    }
}
