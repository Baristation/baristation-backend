package baristation.security.config;

import baristation.security.handler.OAuth2SuccessHandler;
import baristation.security.jwt.JwtAuthenticationFilter;
import baristation.security.jwt.JwtTokenProvider;
import baristation.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /* [추가된 부분]
       - requestMatchers 내부에 "/api/**" 추가하였습니다.
    */
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtTokenProvider jwtTokenProvider;

// filterChain 메서드 내부 (logout 설정 아래 쯤에 추가)

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                // CSRF 비활성화 - 개발 단계 및 OAuth2 연동 테스트 임시 설정
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용

                // 세션 설정 - JWT 사용하므로 세션은 Stateless로 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 경로별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(
                                "/",
                                "/signin",
                                "/beans/**",
                                "/classes/**",
                                "/oauth2/**",
                                "/login/**",   // 구글 리디렉션 도착 경로 허용
                                "/api/beans/**",
                                "/api/lessons/**",
                                "/api/auth/refresh",
                                // swagger 경로
                                "/swagger-custom-ui.html",
                                "/swagger-ui/**",
                                "/api-docs",
                                "/api-docs/**",
                                "/actuator/health",
                                "/actuator/health/**"

                        ).permitAll()
                        .requestMatchers(
                                "/api/auth/**"
                        ).authenticated()  // 인증 필수
                        .requestMatchers(
                                "/mypage/**"
                        ).authenticated()
                        .anyRequest().authenticated() // 나머지는 로그인한 유저만
                )
                // OAuth2 로그인 설정
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        // 리액트로 리다이렉트 시키기 위해 SuccessHandler를 연결
                        .successHandler(oAuth2SuccessHandler)
//                        .defaultSuccessUrl("http://localhost:3000/oauth2/redirect", true)
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutSuccessUrl("https://dripnote-frontend-web.vercel.app/") // 로그아웃 후 리액트 메인으로
                        .deleteCookies("JSESSIONID")
                );

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // CORS 설정 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://dripnote-frontend-web.vercel.app/")); // 리액트 주소 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // 쿠키/인증 정보 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}