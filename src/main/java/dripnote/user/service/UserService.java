package dripnote.user.service;

import dripnote.common.exception.CustomException;
import dripnote.common.exception.ErrorCode;
import dripnote.common.redis.RedisService;
import dripnote.security.jwt.JwtTokenProvider;
import dripnote.security.payload.dto.TokenResponse;
import dripnote.user.domain.User;
import dripnote.user.payload.dto.UserUpdateRequest;
import dripnote.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final UserRepository userRepository;

    public void logout(String accessToken) {
        String userId = jwtTokenProvider.getSubject(accessToken);
        redisService.deleteRefreshToken(userId);
        long expiration = jwtTokenProvider.getExpirationToken(accessToken);
        redisService.setBlackList(accessToken, "logout", Duration.ofMillis(expiration));
    }

    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        String userId = jwtTokenProvider.getSubject(refreshToken);
        String savedUserRefreshToken = redisService.getRefreshToken(userId);
        if (savedUserRefreshToken == null || !savedUserRefreshToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }
        User user = userRepository.getUserByUserId(Long.parseLong(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        TokenResponse newTokenResponse = jwtTokenProvider.createTokenSet(user);
        redisService.setRefreshToken(userId, newTokenResponse.refreshToken());
        return newTokenResponse;
    }


    public void deleteUser(String accessToken) {
        String userId = jwtTokenProvider.getSubject(accessToken);
        User user = userRepository.getUserByUserId(Long.parseLong(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
        redisService.deleteRefreshToken(userId);

        long expiration = jwtTokenProvider.getExpirationToken(accessToken);
        redisService.setBlackList(accessToken, "delete", Duration.ofMillis(expiration));
    }

    public boolean updateUser(String accessToken, UserUpdateRequest updateRequest) {
        String userId = jwtTokenProvider.getSubject(accessToken);
        Optional<User> userOptional = userRepository.getUserByUserId(Long.parseLong(userId));
        if (userOptional.isEmpty()) {

            return false;
        }

        User user = userOptional.get();
        if (StringUtils.hasText(updateRequest.nickname())) {
            user.updateNickname(updateRequest.nickname());
            return true;
        }
        return false;
    }
}
