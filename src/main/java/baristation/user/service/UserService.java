package baristation.user.service;

import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;
import baristation.common.redis.RedisService;
import baristation.security.jwt.JwtTokenProvider;
import baristation.security.payload.dto.TokenPair;
import jakarta.servlet.http.HttpServletRequest;
import baristation.user.domain.User;
import baristation.user.payload.dto.UserUpdateRequest;
import baristation.user.repository.UserRepository;
import baristation.user.validator.NicknameValidator;
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
    private final NicknameValidator nicknameValidator;

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

    public TokenPair refresh(String refreshToken) {
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

        TokenPair newTokenPair = jwtTokenProvider.createTokenSet(user);
        redisService.setRefreshToken(userIdText, newTokenPair.refreshToken());
        log.info("회원 refresh 완료. userId: {}", userIdText);
        return newTokenPair;
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

    /**
     * 회원 정보 수정 (닉네임)
     * - 닉네임 검증 (형식, 금지어, 특수문자)
     * - 중복 체크 (대소문자 무시)
     */
    public void updateUser(HttpServletRequest request, UserUpdateRequest updateRequest) {
        String accessToken = resolveToken(request);
        Long userId = extractUserId(accessToken);
        log.info("회원 정보 수정 요청. userId: {}", userId);

        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (updateRequest == null || !StringUtils.hasText(updateRequest.nickname())) {
            throw new CustomException(ErrorCode.USER_NICKNAME_REQUIRED);
        }

        String newNickname = updateRequest.nickname();

        // 1. 닉네임 형식 검증 (길이, 허용 문자, 금지어, 특수문자)
        nicknameValidator.validate(newNickname);

        // 2. 중복 체크 (대소문자 무시, 자신의 현재 닉네임 제외)
        if (!user.getNickname().equalsIgnoreCase(newNickname)
                && userRepository.existsByNicknameIgnoreCase(newNickname)) {
            throw new CustomException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }

        // 3. 검증 완료된 닉네임 업데이트
        user.updateNickname(newNickname);

        log.info("회원 정보 수정 완료. userId: {}", userId);
        log.info("수정된 닉네임: {}", newNickname);
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
