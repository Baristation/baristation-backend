package dripnote.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA가 엔티티의 생성/수정 이벤트를 감시(Auditing)하고,
 * 적절한 시점에 시간을 주입하도록 엔진을 가동함.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}