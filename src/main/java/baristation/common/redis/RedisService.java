package baristation.common.redis;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${jwt.refresh_expiration_time}")
    private Duration refreshExp;

    /**
     * RefreshToken 저장
     */
    public void setRefreshToken(String userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                "RT:" + userId,          // Key (예: RT:user123)
                refreshToken,            // Value
                refreshExp  // 시간, 날짜 기준(Duration)
        );
    }

    /**
     * RefreshToken 조회
     */
    public String getRefreshToken(String userId) {
        return (String) redisTemplate.opsForValue().get("RT:" + userId);
    }

    /**
     * RefreshToken 삭제
     */
    public void deleteRefreshToken(String userId) {
        redisTemplate.delete("RT:" + userId);
    }

    /**
     * AccessToken 블랙리스트 등록
     */
    public void setBlackList(String accessToken, String status, Duration expirationTime) {
        redisTemplate.opsForValue().set(
                accessToken,
                status,
                expirationTime
        );
    }

    /**
     * 블랙리스트 여부 확인
     */
    public boolean hasKeyBlackList(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(accessToken));
    }
}