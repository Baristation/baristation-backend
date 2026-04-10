package dripnote.user.controller;

import dripnote.common.exception.CustomException;
import dripnote.common.exception.ErrorCode;
import dripnote.common.response.ApiResponse;
import dripnote.security.payload.dto.TokenResponse;
import dripnote.user.payload.dto.UserUpdateRequest;
import dripnote.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshUser(
            @RequestHeader(value = "Refresh-Token") String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCode.TOKEN_INVALID));
        }

        try {
            TokenResponse newTokenResponse = userService.refresh(refreshToken);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.ok("토큰 재발급 성공", newTokenResponse));
        } catch (CustomException e) {
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.error(e.getErrorCode()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String accessToken = resolveToken(request);

        if (accessToken == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCode.TOKEN_INVALID));
        }

        try {
            userService.logout(accessToken);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.ok("로그아웃 성공", null));
        } catch (CustomException e) {
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.error(e.getErrorCode()));
        }
    }

    // 회원 정보 수정
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(
            HttpServletRequest request,
            @RequestBody UserUpdateRequest updateRequest) {

        String accessToken = resolveToken(request);

        if (accessToken == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCode.TOKEN_INVALID));
        }

        try {
            if (userService.updateUser(accessToken, updateRequest)) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(ApiResponse.ok("회원 정보 수정 성공", null));
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(ErrorCode.TOKEN_INVALID));
            }
        } catch (CustomException e) {
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.error(e.getErrorCode()));
        }
    }
    // 회원 탈퇴
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(HttpServletRequest request) {
        String accessToken = resolveToken(request);
        
        if (accessToken == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCode.TOKEN_INVALID));
        }

        try {
            userService.deleteUser(accessToken);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.ok("회원 탈퇴 성공", null));
        } catch (CustomException e) {
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.error(e.getErrorCode()));
        }
    }

    // 토큰 추출 헬퍼 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
