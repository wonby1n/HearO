package com.ssafy.hearo.infra.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 스프링 부트 애플리케이션과 Redis 서버를 연결해주는 다리 역할
 */

@Configuration // 설정 파일임을 알리는 어노테이션 , 스프링이 구동될 때 이 클래스를 읽어 필요한 설정을 등록함
public class RedisConfig {

    @Bean //RedisTemplate이 반환하는 객체를 스프링 컨테이너기 관리하는 객체로 등록함
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) { // Redis에 데이터를 저장하고 조회할 때 사용할 핵심 도구
        RedisTemplate<String, String> template = new RedisTemplate<>(); //
        template.setConnectionFactory(connectionFactory); // Redis 서버와의 실제 연결 정보를 담고 있는 팩토리를 설정( 보통 application.yaml에 적힌 정보를 바탕으로 자동 주입 됨)

        StringRedisSerializer serializer = new StringRedisSerializer(); // Redis 는 보통 기본적으로 데이터를 바이너리 형태로 저장한다. 이를 사람이 읽을 수 있는 평범한 문자열로 변환해주는 도구를 준비
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);
        // Redis의 key.value,hash 구조의 key,value를 모두 위에서 만든 문자열 변환기(serializer)를 사용하도록 지정
        // 이 설정을 안하면 Redis 관리 툴에서 데이터를 볼 때 글자가 깨져 보일 수 있는데, 이 코드가 그걸 방지 해줌

        template.afterPropertiesSet(); // 템플릿의 모든 설정이 완료 되었음을 알리고 초기화하는 표준 절차
        return template; // 설정이 완료된 템플릿을 반환하여 스프링 빈으로 등록을 마침
    }

    /**
     * Redis Key Expiration 이벤트를 수신하기 위한 리스너 컨테이너
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 키 만료 이벤트 패턴 구독 (__keyevent@*__:expired)
        // 주의: Redis 설정에서 notify-keyspace-events Ex 가 활성화되어 있어야 함
        container.addMessageListener((message, pattern) -> {}, new PatternTopic("__keyevent@*__:expired"));
        return container;
    }
}
