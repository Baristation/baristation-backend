package dripnote.user.service;

import dripnote.common.exception.CustomException;
import dripnote.common.exception.ErrorCode;
import dripnote.common.redis.RedisService;
import dripnote.security.jwt.JwtTokenProvider;
import dripnote.security.payload.dto.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import dripnote.user.domain.User;
import dripnote.user.payload.dto.UserUpdateRequest;
import dripnote.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final UserRepository userRepository;

    public void logout(HttpServletRequest request) {
        String accessToken = resolveToken(request);
        Long userId = extractUserId(accessToken);
        String userIdText = String.valueOf(userId);

        log.info("회원 로그아웃 요청. userId: {}", userIdText);

        redisService.deleteRefreshToken(userIdText);
        long expiration = jwtTokenProvider.getExpirationToken(accessToken);
        redisService.setBlackList(accessToken, "logout", Duration.ofMillis(expiration));
        log.info("로그아웃 완료. userId: {}", userIdText);
    }

    public TokenResponse refresh(String refreshToken) {
        // null/blank 체크
        if (!StringUtils.hasText(refreshToken)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        // validateRefreshToken은 CustomException을 던지므로 catch 불필요
        jwtTokenProvider.validateRefreshToken(refreshToken);

        Long userId = extractUserId(refreshToken);
        String userIdText = String.valueOf(userId);

        log.info("회원 refresh 요청. userId: {}", userIdText);

        String savedUserRefreshToken = redisService.getRefreshToken(userIdText);
        if (savedUserRefreshToken == null || !savedUserRefreshToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        TokenResponse newTokenResponse = jwtTokenProvider.createTokenSet(user);
        redisService.setRefreshToken(userIdText, newTokenResponse.refreshToken());
        log.info("회원 refresh 완료. userId: {}", userIdText);
        return newTokenResponse;
    }

    public void deleteUser(HttpServletRequest request) {
        String accessToken = resolveToken(request);
        Long userId = extractUserId(accessToken);
        String userIdText = String.valueOf(userId);

        log.info("회원탈퇴 요청. userId: {}", userIdText);

        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
        redisService.deleteRefreshToken(userIdText);

        long expiration = jwtTokenProvider.getExpirationToken(accessToken);
        redisService.setBlackList(accessToken, "delete", Duration.ofMillis(expiration));

        log.info("회원 탈퇴 완료. userId: {}", userIdText);
    }

    public void updateUser(HttpServletRequest request, UserUpdateRequest updateRequest) {
        String accessToken = resolveToken(request);
        Long userId = extractUserId(accessToken);
        log.info("회원 정보 수정 요청. userId: {}", userId);

        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (updateRequest == null || !StringUtils.hasText(updateRequest.nickname())) {
            throw new CustomException(ErrorCode.USER_NICKNAME_REQUIRED);
        }

        // updateNickname에서 발생한 IllegalArgumentException은 GlobalExceptionHandler에서 처리
        user.updateNickname(updateRequest.nickname());

        log.info("회원 정보 수정 완료. userId: {}", userId);
        log.info("수정된 닉네임: {}", updateRequest.nickname());
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new CustomException(ErrorCode.TOKEN_INVALID);
    }

    private Long extractUserId(String token) {
        String subject = jwtTokenProvider.getSubject(token);

        if (!StringUtils.hasText(subject)) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }
}
