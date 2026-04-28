package baristation.common.payload.response;

import baristation.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record ApiResponse<T>(
        String statusCode,
        String message,
        T data
) {
    // 성공 응답 (200 OK)
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(new ApiResponse<>("200", "OK", data));
    }

    // 성공 응답 (메시지 커스텀)
    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(new ApiResponse<>("200", message, data));
    }

    // 실패 응답 (ErrorCode에 정의된 HttpStatus 사용)
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus()) // ErrorCode에 HttpStatus 필드가 있다고 가정
                .body(new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null));
    }

    // 실패 응답 (Data 포함)
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode, T data) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), data));
    }
    // 204
    public static ResponseEntity<ApiResponse<Void>> noContent() {
        return ResponseEntity
                .status(HttpStatus.OK) // 또는 HttpStatus.NO_CONTENT
                .body(new ApiResponse<>("204", "No Content", null));
    }
}