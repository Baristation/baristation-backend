package dripnote.common.payload.response;

import dripnote.common.exception.ErrorCode;

public record ApiResponse<T>(
        String statusCode,
        String message,
        T data
) {
    // 성공 응답
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("200", "OK", data);
    }

    // 성공 응답, 메시지 커스텀
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>("200", message, data);
    }

    // 실패 응답
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    // 실패 응답, data 포함 가능
    public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), data);
    }
}