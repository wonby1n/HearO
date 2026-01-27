package com.ssafy.hearo.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 스프링에서 비동기 처리 기능을 활성화함
 * 간단해보이지만 아주 강력한 설정 파일
 * 대기열 시스템에서 꼭 필요한 설정
 */

@Configuration // 스프링 서버가 뜰 때 이 클래스를 설정 정보로 읽어들이겠다는 선언
@EnableAsync // 이 파일의 핵심 ! 스프링의 비동기 실행 기능을 켬 .
// 이 애노테이션이 있어야 나중에 service method 위에 @Async를 붙였을 때 그 로직이 별도의 스레드에서 돌아감
public class AsyncConfig {
}
