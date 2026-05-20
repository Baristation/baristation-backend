package baristation.common.exception;

import baristation.common.logging.TraceIdUtil;
import baristation.common.payload.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();

        // 에러 코드별로 로그 레벨 구분
        if (errorCode.getHttpStatus().is5xxServerError()) {
            log.error("[Exception] code={}, message={}, traceId={}",
                    errorCode.getCode(), errorCode.getMessage(), TraceIdUtil.getTraceId());
        } else if (errorCode.getHttpStatus().is4xxClientError()) {
            log.warn("[Exception] code={}, message={}, traceId={}",
                    errorCode.getCode(), errorCode.getMessage(), TraceIdUtil.getTraceId());
        }

        return ApiResponse.error(errorCode);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        // 원인을 파악할 수 있도록 에러의 전체 스택 트레이스를 에러 로그로 남깁니다.
        log.error("[Unexpected Exception] message={}, traceId={}",
                e.getMessage(), TraceIdUtil.getTraceId(), e);

        // 클라이언트에게는 내부 정보가 노출되지 않도록 공통 500 에러 코드로 응답합니다.
        return ApiResponse.error(ErrorCode.COMMON_INTERNAL_ERROR);
    }
}