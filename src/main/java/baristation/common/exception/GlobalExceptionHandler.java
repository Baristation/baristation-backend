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
        log.warn("[CustomException] code={}, traceId={}", errorCode.getCode(), TraceIdUtil.getTraceId());
        
        return ApiResponse.error(errorCode);
    }

}