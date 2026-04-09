package dripnote.security.jwt;

import dripnote.common.redis.RedisService;
import dripnote.security.payload.dto.TokenResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import dripnote.user.domain.User;
//import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
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
    public TokenResponse createTokenSet(User user) { // TokenResponse DTO 필요
        String accessToken = createToken(user, accessExp);
        String refreshToken = createToken(user, refreshExp); // Refresh Token도 동일 로직으로 생성

        return TokenResponse.of(accessToken, refreshToken);
    }
    public String generateAccessToken(User user) {
        return createToken(user, accessExp);
    }

    public String generateRefreshToken(User user) {
        return createToken(user, refreshExp);
    }
    // 로그인시 토큰 생성 메서드
    private String createToken(User user, long expTime) {
        // claims - jwt 토큰 속 내용 생성
        Claims claims = Jwts.claims().setSubject(String.valueOf(user.getUserId()));
        claims.put("role", user.getRole().name()); // 권한 주입
        /**
         * 압축해서 하나의 문자열로
         * 헤더(어떤 암호화인지),
         * 페이로드(claims에 넣은 정보 인코딩),
         * 서명(이 정보들 모아서 해쉬함수화)
         */
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expTime))
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
    /**
     * 3. 토큰의 유효성 검증 로직.
     * JWT 자체의 유효성뿐만 아니라 Redis 블랙리스트 여부까지 확인합니다.
     */
    public boolean validateToken(String token) {
        try {
            // 1. JWT 파싱 및 기본 검증 (서명, 구조, 만료일 등)
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

            // 2. Redis를 조회하여 로그아웃된(Blacklist) 토큰인지 확인
            if (redisService.hasKeyBlackList(token)) {
                log.info("로그아웃된 JWT 토큰입니다.");
                return false; // 블랙리스트에 있으면 유효하지 않은 토큰으로 처리
            }

            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
            throw new MalformedJwtException("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
            throw e;
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
            return false;
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
            return false;
        } catch (Exception e) {
            log.info("유효하지 않은 JWT 토큰입니다.");
            return false;
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