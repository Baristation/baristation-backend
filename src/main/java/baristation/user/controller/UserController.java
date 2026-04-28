package baristation.user.controller;

import baristation.common.payload.response.ApiResponse;
import baristation.security.payload.dto.TokenResponse;
import baristation.user.payload.dto.UserUpdateRequest;
import baristation.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshUser(
            @RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {
        TokenResponse newTokenResponse = userService.refresh(refreshToken);
        return ApiResponse.ok(newTokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        userService.logout(request);
        return ApiResponse.ok();
    }

    // 회원 정보 수정
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(
            HttpServletRequest request,
            @RequestBody UserUpdateRequest updateRequest) {
        userService.updateUser(request, updateRequest);
        return ApiResponse.ok();
    }
    // 회원 탈퇴
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(HttpServletRequest request) {
        userService.deleteUser(request);
        return ApiResponse.ok();
    }
}
