package baristation.user.controller;

import baristation.common.cookie.CookieUtil;
import baristation.common.payload.response.ApiResponse;
import baristation.security.payload.dto.TokenPair;
import baristation.security.payload.dto.TokenResponse;
import baristation.user.payload.dto.UserUpdateRequest;
import baristation.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CookieUtil cookieUtil;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshUser(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        TokenPair newTokenPair = userService.refresh(refreshToken);

        // 쿠키를 생성하여 response 헤더에 저장
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(newTokenPair.refreshToken()).toString());

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(newTokenPair.accessToken())
                .tokenType(newTokenPair.tokenType())
                .build();

        return ApiResponse.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.logout(request);

        // refreshToken 쿠키를 삭제하기위해 maxAge 0으로 설정하고 response 헤더에 저장
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshTokenCookie().toString());

        return ApiResponse.ok();
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(
            HttpServletRequest request,
            @RequestBody UserUpdateRequest updateRequest) {
        userService.updateUser(request, updateRequest);
        return ApiResponse.ok();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(HttpServletRequest request, HttpServletResponse response) {
        userService.deleteUser(request);

        // refreshToken 쿠키를 삭제하기위해 maxAge 0으로 설정하고 response 헤더에 저장
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshTokenCookie().toString());
        return ApiResponse.ok();
    }
}
