package com.ssafy.hearo.domain.calls.controller;


import com.ssafy.hearo.domain.calls.service.CallsService;
import com.ssafy.hearo.domain.calls.dto.IssueTokenRequest;
import com.ssafy.hearo.domain.calls.dto.IssueTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${livekit.url}")
    private String livekitUrl;

    @PostMapping("/token")
    public ResponseEntity<IssueTokenResponse> createToken(@Valid @RequestBody IssueTokenRequest request){
        String token = callsService.createToken(request.getIdentity(),request.getRoomName());
        return ResponseEntity.ok(IssueTokenResponse.of(token,livekitUrl));
    }

}
