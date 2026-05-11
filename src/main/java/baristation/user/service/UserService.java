package baristation.user.service;

import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;
import baristation.common.logging.TraceIdUtil;
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

        redisService.deleteRefreshToken(userIdText);
        long expiration = jwtTokenProvider.getExpirationToken(accessToken);
        redisService.setBlackList(accessToken, "logout", Duration.ofMillis(expiration));

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


        String savedUserRefreshToken = redisService.getRefreshToken(userIdText);
        if (savedUserRefreshToken == null || !savedUserRefreshToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        TokenPair newTokenPair = jwtTokenProvider.createTokenSet(user);
        redisService.setRefreshToken(userIdText, newTokenPair.refreshToken());
        return newTokenPair;
    }

    /**
     * 주어진 토큰(subject가 userId로 들어있다는 전제)에서 user의 닉네임을 조회합니다.
     * 주로 TokenResponse를 구성할 때 사용합니다.
     */
//    public String getNicknameFromToken(String token) {
//        if (!StringUtils.hasText(token)) {
//            throw new CustomException(ErrorCode.TOKEN_INVALID);
//        }
//
//        Long userId = extractUserId(token);
//        return userRepository.getUserByUserId(userId)
//                .map(User::getNickname)
//                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
//    }

    public void deleteUser(HttpServletRequest request) {
        String accessToken = resolveToken(request);
        Long userId = extractUserId(accessToken);
        String userIdText = String.valueOf(userId);

        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
        redisService.deleteRefreshToken(userIdText);

        long expiration = jwtTokenProvider.getExpirationToken(accessToken);
        redisService.setBlackList(accessToken, "delete", Duration.ofMillis(expiration));

    }

    /**
     * 회원 정보 수정 (닉네임)
     * - 닉네임 검증 (형식, 금지어, 특수문자)
     * - 중복 체크 (대소문자 무시)
     */
    public void updateUser(HttpServletRequest request, UserUpdateRequest updateRequest) {
        String accessToken = resolveToken(request);
        Long userId = extractUserId(accessToken);

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
