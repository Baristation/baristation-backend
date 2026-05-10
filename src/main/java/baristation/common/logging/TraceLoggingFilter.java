package baristation.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
// 필터 중, 가장 먼저 들어오도록
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String RESPONSE_LOG_PATTERN = "[Response] status={}, durationMs={}, traceId={}";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString();
        }

        MDC.put(TraceIdUtil.TRACE_ID_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        log.info("[Inbound] method={}, uri={}, traceId={}", request.getMethod(), request.getRequestURI(), traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (status >= 500) {
                log.error(RESPONSE_LOG_PATTERN, status, durationMs, traceId);
            } else if (status >= 400) {
                log.warn(RESPONSE_LOG_PATTERN, status, durationMs, traceId);
            } else {
                log.info(RESPONSE_LOG_PATTERN, status, durationMs, traceId);
            }

            MDC.remove(TraceIdUtil.TRACE_ID_KEY);
        }
    }
}

