package baristation.security.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) // 파라미터에만 사용 가능하도록 설정
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 어노테이션 정보 유지
/*
 * 핵심 로직:
 * 1. 인증되지 않은 익명 사용자일 경우 'anonymousUser' 문자열이 들어오므로 null을 반환합니다.
 * 2. 인증된 사용자일 경우 UserDetails의 getUsername() 값을 추출합니다.
 * 3. 현재 TokenProvider에서 getUsername() 자리에 유저 ID를 매핑해 두었으므로 유저 ID가 추출됩니다.
 */
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : T(Long).valueOf(username)")
public @interface CurrentUserId {
}