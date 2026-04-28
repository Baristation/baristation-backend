package baristation.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import baristation.common.exception.ErrorCode;
import baristation.common.payload.response.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
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
    // 싱글톤
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 헤더에서 토큰 추출
        String token = resolveToken(request);

        try {
            // 토큰 유효성 검사 및 인증 처리
            if (token != null) {
                jwtTokenProvider.validateAccessToken(token);

                // 정상 토큰 -> 인증 객체 저장
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Security Context에 '{}' 인증 정보를 저장했습니다", authentication.getName());
            }
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
            sendJsonErrorResponse(response, ErrorCode.TOKEN_EXPIRED);
            return;
        } catch (SecurityException | MalformedJwtException e) {
            // 위조되거나 잘못된 형식
            log.info("잘못된 JWT 서명입니다.");
            sendJsonErrorResponse(response, ErrorCode.TOKEN_INVALID);
            return;
        } catch (UnsupportedJwtException e) {
            // 지원하지 않는 형식
            log.info("지원되지 않는 JWT 토큰입니다.");
            sendJsonErrorResponse(response, ErrorCode.TOKEN_INVALID);
            return;
        } catch (IllegalArgumentException e) {
            // 토큰이 없는 경우 등
            log.info("JWT 토큰이 잘못되었습니다.");
            sendJsonErrorResponse(response, ErrorCode.TOKEN_INVALID);
            return;
        } catch (Exception e) {
            log.error("JWT 필터 내부 오류: {}", e.getMessage());
            sendJsonErrorResponse(response, ErrorCode.TOKEN_INVALID);
            return;
        }

        // 다음 필터로 넘김 (성공한 요청만)
        filterChain.doFilter(request, response);
    }

    // Request Header에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 문자열만 가져옴
        }
        return null;
    }

    // JSON 형태의 에러 응답 전송
    private void sendJsonErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // ApiResponse.error()가 반환하는 ResponseEntity에서 Body만 추출
        ApiResponse<Void> errorResponse = ApiResponse.<Void>error(errorCode).getBody();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
    }
}