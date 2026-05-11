package baristation.security.jwt;

import baristation.common.redis.RedisService;
import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;
import baristation.security.payload.dto.TokenPair;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import baristation.user.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JwtTokenProvider {
    private static final String TOKEN_TYPE_BEARER = "Bearer";
    private static final String CLAIM_TYPE = "type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final RedisService redisService;
    private final Key key;
    private final long accessExp;
    private final long refreshExp;

    public JwtTokenProvider(RedisService redisService,
                            @Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.access_expiration_time}") Duration accessExp,
                            @Value("${jwt.refresh_expiration_time}") Duration refreshExp) {
        this.redisService = redisService;
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExp = accessExp.toMillis();
        this.refreshExp = refreshExp.toMillis();
        }
    /**
     * 외부에서 호출 가능하도록 createTokenSet 구현
     */
    public TokenPair createTokenSet(User user) {
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE_BEARER)
                .build();
    }

    public String generateAccessToken(User user) {
        return createAccessToken(user);
    }

    public String generateRefreshToken(User user) {
        return createRefreshToken(user);
    }

    private String createAccessToken(User user) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(user.getUserId()));
        claims.put("role", user.getRole().name()); // 권한 주입
        claims.put("name", user.getNickname());
        // 이메일은 null일 수 있음
        claims.put("email", Optional.ofNullable(user.getEmail()).orElse(""));
//        claims.put("profile", user.getProfile());
        claims.put(CLAIM_TYPE, ACCESS_TOKEN_TYPE);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String createRefreshToken(User user) {
        // claims - jwt 토큰 속 내용 생성
        Claims claims = Jwts.claims().setSubject(String.valueOf(user.getUserId()));
        claims.put("role", user.getRole().name()); // 권한 주입
        claims.put(CLAIM_TYPE, REFRESH_TOKEN_TYPE);
        /**
         * 압축해서 하나의 문자열로
         * 헤더(어떤 암호화인지),
         * 페이로드(claims에 넣은 정보 인코딩),
         * 서명(이 정보들 모아서 해쉬함수화)
         */
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String role = claims.get("role").toString();

        // ROLE_ 접두사가 없으면 붙여줌
        if(!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(claims.getSubject(), "",
                Collections.singleton(new SimpleGrantedAuthority(role)));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
    // 토큰 검증 계약: 유효하면 정상 종료, 유효하지 않으면 예외 발생
    public void validateAccessToken(String token) {
        validateToken(token, ACCESS_TOKEN_TYPE);
    }

    public void validateRefreshToken(String token) {
        validateToken(token, REFRESH_TOKEN_TYPE);
    }

    private void validateToken(String token, String expectedType) {
        try {
            // 1. JWT 파싱 및 기본 검증 (서명, 구조, 만료일 등)
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

            // 1-1. 토큰 용도(access/refresh) 검증
            if (expectedType != null) {
                String tokenType = claims.get(CLAIM_TYPE, String.class);
                if (!expectedType.equals(tokenType)) {
                    throw new CustomException(ErrorCode.TOKEN_INVALID);
                }
            }

            // 2. Redis를 조회하여 로그아웃된(Blacklist) 토큰인지 확인
            if (redisService.hasKeyBlackList(token)) {
                throw new CustomException(ErrorCode.TOKEN_INVALID);
            }
        } catch (SecurityException | MalformedJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (CustomException e) {
            // CustomException(블랙리스트, 타입 불일치 등)은 그대로 전파
            throw e;
        }
    }

    public long getExpirationToken(String token) {
        long expirationTime = parseClaims(token).getExpiration().getTime();
        long currTime = System.currentTimeMillis();

        // 만료 시간에서 현재 시간을 뺐을 때 남은 시간이 음수면 0으로 반환 (이미 만료된 경우)
        return Math.max(0, expirationTime - currTime);
    }

    /**
     * 토큰에서 Subject(loginId) 추출
     */
    public String getSubject(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료 되어도 누구 토큰인지는 알아야 함
        }
    }
}