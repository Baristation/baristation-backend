package baristation.common.redis;
import baristation.common.logging.TraceIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
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
        log.info("[Redis] set refresh token. userId={}, traceId={}", userId, TraceIdUtil.getTraceId());
    }

    /**
     * RefreshToken 조회
     */
    public String getRefreshToken(String userId) {
        String refreshToken = (String) redisTemplate.opsForValue().get("RT:" + userId);
        log.info("[Redis] get refresh token. userId={}, exists={}, traceId={}",
                userId, refreshToken != null, TraceIdUtil.getTraceId());
        return refreshToken;
    }

    /**
     * RefreshToken 삭제
     */
    public void deleteRefreshToken(String userId) {
        redisTemplate.delete("RT:" + userId);
        log.info("[Redis] delete refresh token. userId={}, traceId={}", userId, TraceIdUtil.getTraceId());
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
        log.info("[Redis] set blacklist token. status={}, expiresMs={}, traceId={}",
                status, expirationTime.toMillis(), TraceIdUtil.getTraceId());
    }

    /**
     * 블랙리스트 여부 확인
     */
    public boolean hasKeyBlackList(String accessToken) {
        boolean isBlackList = Boolean.TRUE.equals(redisTemplate.hasKey(accessToken));
        if (isBlackList) {
            log.warn("[Redis] token is blacklisted. traceId={}", TraceIdUtil.getTraceId());
        }
        return isBlackList;
    }
}