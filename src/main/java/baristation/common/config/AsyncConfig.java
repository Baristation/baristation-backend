package baristation.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 비동기 처리 활성화 설정
 * @Async 어노테이션을 통한 비동기 메서드 실행을 위해 필수적으로 필요합니다.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}

