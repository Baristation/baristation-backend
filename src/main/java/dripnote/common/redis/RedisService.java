package dripnote.common.redis;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 1. RefreshToken 저장
     * 로그인 성공 시 발급된 RT를 Redis에 저장
     */
    public void setRefreshToken(String userId, String refreshToken, Duration expirationTime) {
        redisTemplate.opsForValue().set(
                "RT:" + userId,          // Key (예: RT:user123)
                refreshToken,            // Value
                expirationTime  // 시간, 날짜 기준(Duration)
        );
    }

    /**
     * 2. RefreshToken 조회
     * AccessToken 재발급(RTR) 요청 시 사용.
     */
    public String getRefreshToken(String userId) {
        return (String) redisTemplate.opsForValue().get("RT:" + userId);
    }

    /**
     * 3. RefreshToken 삭제
     * 로그아웃 시 Redis에서 RT를 날림.
     */
    public void deleteRefreshToken(String userId) {
        redisTemplate.delete("RT:" + userId);
    }

    /**
     * 4. AccessToken 블랙리스트 등록
     * 로그아웃 시 아직 만료되지 않은 AccessToken을 무효화하기 위해 저장.
     */
    public void setBlackList(String accessToken, String status, Duration expirationTime) {
        redisTemplate.opsForValue().set(
                accessToken,
                status,
                expirationTime
        );
    }

    /**
     * 5. 블랙리스트 여부 확인
     * 요청이 들어올 때마다 해당 AccessToken이 블랙리스트에 있는지 확인.
     */
    public boolean hasKeyBlackList(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(accessToken));
    }
}