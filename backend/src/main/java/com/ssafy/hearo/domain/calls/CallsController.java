package com.ssafy.hearo.domain.calls;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/calls")
@RequiredArgsConstructor
public class CallsController {

    final private CallsService callsService;
    private static final String LIVEKIT_URL = "";

    @PostMapping("/token")
    public ResponseEntity<IssueTokenResponse> createToken(@Valid @RequestBody IssueTokenRequest request){
        String token = callsService.createToken(request.getIdentity(),request.getRoomName());
        return ResponseEntity.ok(new IssueTokenResponse(token,LIVEKIT_URL));
    }

}
