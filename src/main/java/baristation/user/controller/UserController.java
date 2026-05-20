package baristation.user.controller;

import baristation.common.cookie.CookieUtil;
import baristation.common.logging.TraceIdUtil;
import baristation.common.payload.response.ApiResponse;
import baristation.security.annotation.CurrentUserId;
import baristation.security.payload.dto.TokenPair;
import baristation.security.payload.dto.TokenResponse;
import baristation.user.payload.dto.UserUpdateRequest;
import baristation.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final CookieUtil cookieUtil;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshUser(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        log.info("[Auth] refresh start. hasRefreshToken={}, traceId={}",
                refreshToken != null && !refreshToken.isBlank(), TraceIdUtil.getTraceId());

        TokenPair newTokenPair = userService.refresh(refreshToken);

        // 쿠키를 생성하여 response 헤더에 저장
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(newTokenPair.refreshToken()).toString());

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(newTokenPair.accessToken())
                .tokenType(newTokenPair.tokenType())
                .build();

        log.info("[Auth] refresh done. tokenType={}, traceId={}", tokenResponse.tokenType(), TraceIdUtil.getTraceId());

        return ApiResponse.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("[Auth] logout start. traceId={}", TraceIdUtil.getTraceId());
        userService.logout(request);

        // refreshToken 쿠키를 삭제하기위해 maxAge 0으로 설정하고 response 헤더에 저장
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshTokenCookie().toString());

        log.info("[Auth] logout done. traceId={}", TraceIdUtil.getTraceId());

        return ApiResponse.ok();
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(
            @CurrentUserId Long userId,
            @RequestPart(value = "data") UserUpdateRequest updateRequest,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        log.info("[Auth] updateUserInfo start. traceId={}", TraceIdUtil.getTraceId());
        userService.updateUser(userId, updateRequest, profileImage);

        log.info("[Auth] updateUserInfo done. traceId={}", TraceIdUtil.getTraceId());
        return ApiResponse.ok();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(HttpServletRequest request, HttpServletResponse response) {
        log.info("[Auth] deleteAccount start. traceId={}", TraceIdUtil.getTraceId());
        userService.deleteUser(request);

        // refreshToken 쿠키를 삭제하기위해 maxAge 0으로 설정하고 response 헤더에 저장
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshTokenCookie().toString());
        log.info("[Auth] deleteAccount done. traceId={}", TraceIdUtil.getTraceId());
        return ApiResponse.ok();
    }
}
