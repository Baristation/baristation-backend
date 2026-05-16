package baristation.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import baristation.common.exception.ErrorCode;
import baristation.common.exception.CustomException;
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
            if (token != null && !token.isBlank()) {
                jwtTokenProvider.validateAccessToken(token);

                // 정상 토큰 -> 인증 객체 저장
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            sendJsonErrorResponse(response, ErrorCode.TOKEN_EXPIRED);
            return;
        } catch (CustomException e) {
            // JwtTokenProvider에서 던진 CustomException을 여기서 처리하여
            // 일관된 JSON 응답을 반환합니다.
            sendJsonErrorResponse(response, e.getErrorCode());
            return;
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            // JWT 관련 예외는 모두 GlobalExceptionHandler에서 처리
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