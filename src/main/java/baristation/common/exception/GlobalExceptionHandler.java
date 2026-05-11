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

}