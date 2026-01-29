package com.ssafy.hearo.domain.calls.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IssueTokenResponse {

    //고객과 상담자한테 부여될 토큰
    private String token;

    //임시로 사용할 url
    private String url;

    //token으로 Response 생성
    public static IssueTokenResponse of(String token, String url){
        return IssueTokenResponse.builder().token(token).url(url).build();
    }

}
