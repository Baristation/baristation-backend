package baristation.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();
    // 왜 입장 불가능한지 설명해주는 클래스
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String exception = (String) request.getAttribute("exception");
        log.error("Commence Get Exception : {}", exception);

        if (exception == null) {
            setResponse(response, "LOGIN_NEEDED", "로그인이 필요합니다.");
        } else if (exception.equals("EXPIRED_TOKEN")) {
            setResponse(response, "EXPIRED_TOKEN", "토큰이 만료되었습니다.");
        } else if (exception.equals("INVALID_TOKEN")) {
            setResponse(response, "INVALID_TOKEN", "잘못된 토큰 서명입니다.");
        } else if (exception.equals("UNSUPPORTED_TOKEN")) {
            setResponse(response, "UNSUPPORTED_TOKEN", "지원되지 않는 토큰입니다.");
        } else {
            setResponse(response, "ACCESS_DENIED", "접근이 거부되었습니다.");
        }
    }

    // JSON 응답 만들기
    private void setResponse(HttpServletResponse response, String code, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 에러

        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);

        response.getWriter().print(objectMapper.writeValueAsString(map));
    }
}