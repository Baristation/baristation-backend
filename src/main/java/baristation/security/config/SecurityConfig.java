package baristation.security.config;

import baristation.security.exception.CustomAuthenticationEntryPoint;
import baristation.security.handler.OAuth2SuccessHandler;
import baristation.security.jwt.JwtAuthenticationFilter;
import baristation.security.jwt.JwtTokenProvider;
import baristation.user.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                          OAuth2SuccessHandler oAuth2SuccessHandler,
                          JwtTokenProvider jwtTokenProvider,
                          @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver,
                          CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.jwtTokenProvider = jwtTokenProvider;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // JWT 사용으로 CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용

                // 세션 설정 - JWT 사용하므로 세션은 Stateless로 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 예외 처리 - 인증 실패 시 JSON 응답 (로그인 페이지로 리다이렉트 아님)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )

                // 경로별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // CORS preflight 요청 항상 허용 - 명시적 선언
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/assets/**",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/api/auth/refresh").permitAll()
                        .requestMatchers("/api/auth/**").authenticated()  // refresh 제외한 auth는 인증 필수
                        .requestMatchers("/api/bookmarks/**").authenticated()  // 북마크는 인증 필수
                        .requestMatchers("/mypage/**").authenticated()
                        .anyRequest().permitAll() // 나머지는 모두 공개
                )
                // OAuth2 로그인 설정
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        // 리액트로 리다이렉트 시키기 위해 SuccessHandler를 연결
                        .successHandler(oAuth2SuccessHandler)
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutSuccessUrl(frontendBaseUrl) // 로그아웃 후 리액트 메인으로
                );

            http.addFilterBefore(
                    new JwtAuthenticationFilter(jwtTokenProvider, handlerExceptionResolver),
                    UsernamePasswordAuthenticationFilter.class
            );
        return http.build();
    }

    // CORS 설정 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:3001",
                frontendBaseUrl
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true); // 쿠키를 사용하여 true로 수정

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
