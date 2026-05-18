# JWT 예외 처리 리팩토링 전략 (검수 완료)

## ✅ 검수 결과: 제시된 비판이 100% 타당

### 🔴 기존 코드의 3가지 치명적 문제

#### 1️⃣ **Dead Code 문제** (도달 불가능한 catch 블록)
```
JwtTokenProvider.validateToken() 내부:
- 모든 JWT 라이브러리 예외를 catch하여 CustomException으로 변환
- 따라서 ExpiredJwtException, MalformedJwtException 등은 절대 필터까지 전파되지 않음

JwtAuthenticationFilter 필터:
- Line 47: catch (ExpiredJwtException e) { ... } ← 실행 불가 ❌
- Line 55: catch (SecurityException | MalformedJwtException | ...) ← 실행 불가 ❌
- 오직 CustomException만 실행됨
```

#### 2️⃣ **예외 블랙홀(Exception Swallowing) 문제** ⚠️
```
현재 필터 코드에 catch (Exception e)를 추가하면:
- 시스템 수준의 치명적 에러 (NullPointerException, OOM 등)도 모조리 포괄
- "유효하지 않은 토큰" JSON으로 응답하게 됨
- 실제 버그 원인이 은폐되어 프로덕션 디버깅 극도로 어려움
```

#### 3️⃣ **중복된 예외 처리 로직**
```
sendJsonErrorResponse() 메서드:
- ObjectMapper를 사용해 JSON 구성하고 response에 직접 쓰는 로직
- GlobalExceptionHandler에서도 동일한 로직 존재
- 예외 응답 포맷이 두 곳에서 관리됨 (유지보수성 악화)
```

---

## 🎯 전략 비교

| 구분 | 전략 A (경량화) | 전략 B (정석) |
|------|----------------|-------------|
| **복잡도** | 낮음 | 중간 |
| **필터 책임** | 예외 처리 | 예외 감지만 |
| **코드 중복** | 있음 | 없음 |
| **유지보수** | 양호 | 우수 |
| **스프링 정석** | 아님 | 맞음 |
| **추천도** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## 💻 전략 A: 예외 변환 및 응답의 명확한 분리 (경량화)

### 핵심 개념
1. `JwtTokenProvider`: 모든 JWT 예외를 `CustomException`으로 변환
2. `JwtAuthenticationFilter`: 오직 `CustomException`만 처리
3. 불필요한 catch 블록 제거

### 📝 구현 코드

#### Step 1: JwtAuthenticationFilter (간소화)
```java
package baristation.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import baristation.common.exception.ErrorCode;
import baristation.common.exception.CustomException;
import baristation.common.payload.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        try {
            if (token != null && !token.isBlank()) {
                jwtTokenProvider.validateAccessToken(token);
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // 성공 시 다음 필터로
            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            // JwtTokenProvider에서 던진 CustomException만 처리
            // (모든 JWT 라이브러리 예외는 이미 CustomException으로 변환됨)
            sendJsonErrorResponse(response, e.getErrorCode());
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendJsonErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> errorResponse = ApiResponse.<Void>error(errorCode).getBody();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}
```

#### Step 2: JwtTokenProvider.validateToken() (명확화)
```java
private void validateToken(String token, String expectedType) {
    try {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // 토큰 타입 검증
        if (expectedType != null) {
            String tokenType = claims.get(CLAIM_TYPE, String.class);
            if (!expectedType.equals(tokenType)) {
                throw new CustomException(ErrorCode.TOKEN_INVALID);
            }
        }

        // 블랙리스트 확인
        if (redisService.hasKeyBlackList(token)) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    } catch (CustomException e) {
        // 명시적으로 던진 CustomException은 그대로 전파
        throw e;
    } catch (ExpiredJwtException e) {
        throw new CustomException(ErrorCode.TOKEN_EXPIRED);
    } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
        throw new CustomException(ErrorCode.TOKEN_INVALID);
    }
}
```

### ✅ 전략 A의 장점
- ✨ 필터 코드가 간결함
- 🎯 책임이 명확함 (Provider: 변환, Filter: 처리)
- 🚀 즉시 적용 가능

### ⚠️ 전략 A의 한계
- 필터에서 직접 JSON 응답을 구성 (중복 코드)
- 전역 예외 처리기와 응답 포맷 관리가 분리됨

---

## 💻 전략 B: HandlerExceptionResolver를 통한 정석적 처리

### 핵심 개념
1. 필터는 **예외 감지만** 담당
2. `HandlerExceptionResolver`에 예외를 위임
3. `GlobalExceptionHandler`가 모든 예외를 **중앙 제어**
4. 필터 내 JSON 응답 로직 제거

### 📝 구현 코드

#### Step 1: HandlerExceptionResolver 구성 클래스
```java
package baristation.security.config;

import baristation.common.exception.CustomException;
import baristation.common.payload.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 필터에서 발생한 CustomException을
 * GlobalExceptionHandler로 위임하기 위한 리졸버
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtExceptionResolver implements HandlerExceptionResolver {

    private final ObjectMapper objectMapper;

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Object handler,
                                        Exception ex) {
        if (!(ex instanceof CustomException)) {
            return null; // 다른 예외는 처리하지 않음
        }

        try {
            CustomException customEx = (CustomException) ex;
            response.setStatus(customEx.getErrorCode().getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ApiResponse<Void> errorResponse = ApiResponse.error(customEx.getErrorCode());
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponse);

            log.warn("JWT 필터 예외 처리: {}", customEx.getErrorCode().getMessage());
        } catch (IOException e) {
            log.error("응답 작성 중 오류 발생", e);
        }

        return new ModelAndView();
    }
}
```

#### Step 2: 개선된 JwtAuthenticationFilter
```java
package baristation.security.jwt;

import baristation.common.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        try {
            if (token != null && !token.isBlank()) {
                // 토큰 검증 및 인증 설정
                jwtTokenProvider.validateAccessToken(token);
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // 성공한 요청만 다음 필터로
            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            // CustomException을 HandlerExceptionResolver로 위임
            // → GlobalExceptionHandler에서 처리됨 (중앙 제어)
            handlerExceptionResolver.resolveException(request, response, null, e);
        } catch (Exception e) {
            // 예상치 못한 에러는 로깅하고 위임
            log.error("JWT 필터 내 예상치 못한 예외", e);
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

#### Step 3: 필터 등록 (SecurityConfig)
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/signup").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider, handlerExceptionResolver),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
```

### ✅ 전략 B의 장점
- 🎯 스프링 정석적 접근
- 🔧 중앙 집중식 예외 관리 (GlobalExceptionHandler)
- 🧹 필터에서 JSON 응답 로직 제거 (중복 제거)
- 📊 모든 예외가 일관되게 처리됨
- 🛡️ 시스템 예외와 비즈니스 예외 구분 가능
- 🚀 확장성 우수

### ⚠️ 전략 B의 주의점
- `HandlerExceptionResolver` 주입 필요
- 약간의 추가 설정 코드 필요

---

## 📊 결론 및 권장사항

### 🏆 추천 전략: **전략 B (정석)**

**이유:**
1. 스프링 프레임워크의 정석적인 예외 처리 패턴
2. 필터와 컨트롤러 단계의 예외를 동일하게 관리
3. 중복 코드 제거로 유지보수성 향상
4. 향후 추가 필터 확장 시 확장성 우수

### 단계별 적용 계획

#### Phase 1: 즉시 적용 (전략 A)
```
- 필터의 dead code 제거
- catch (Exception e) 패턴 제거
- JwtTokenProvider 명확화
```

#### Phase 2: 리팩토링 (전략 B 권장)
```
- HandlerExceptionResolver 구성
- 필터 구조 개선
- 응답 로직 중앙화
```

### 체크리스트
- [ ] `JwtTokenProvider`: 명시적 CustomException 재throw 확인
- [ ] `JwtAuthenticationFilter`: Dead code 제거
- [ ] `JwtAuthenticationFilter`: `catch (Exception e)` 제거
- [ ] `HandlerExceptionResolver` 적용 (권장)
- [ ] 테스트: 토큰 만료, 유효하지 않은 토큰, 시스템 에러

---

## 🔍 마이그레이션 로드맵

```
현재 상태
   ↓
[Phase 1] 전략 A 적용 (1~2일)
   - Dead code 정리
   - 명확성 개선
   ↓
테스트 및 검증 (1일)
   ↓
[Phase 2] 전략 B 적용 (2~3일) - 선택사항
   - HandlerExceptionResolver 구성
   - 중앙 집중식 관리
   ↓
최종 프로덕션 배포
```

