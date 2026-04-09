package dripnote.user.service;

import dripnote.common.redis.RedisService;
import dripnote.security.jwt.JwtTokenProvider;
import dripnote.security.payload.dto.TokenResponse;
import dripnote.user.domain.User;
import dripnote.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public TokenResponse refersh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        String userId = jwtTokenProvider.getSubject(refreshToken);
        String savedUserRefreshToken = redisService.getRefreshToken(userId);
        if (savedUserRefreshToken == null || !savedUserRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("Refresh Token이 일치하지 않거나 만료되었습니다.");
        }
        Optional<User> optionalUser = userRepository.getUserByUserId(Long.parseLong(userId));
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        TokenResponse newTokenResponse = jwtTokenProvider.createTokenSet(optionalUser.get());
        redisService.setRefreshToken(userId,
                newTokenResponse.refreshToken());
        return newTokenResponse;
    }
}
