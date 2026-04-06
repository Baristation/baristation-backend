package dripnote.security.jwt;

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

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessExp;
    private final long refreshExp;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.access_expiration_time}") long accessExp,
                            @Value("${jwt.refresh_expiration_time}") long refreshExp) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExp = accessExp;
        this.refreshExp = refreshExp;
        }
    /**
     * 외부에서 호출 가능하도록 createTokenSet 구현
     */
    public TokenResponse createTokenSet(User user) { // TokenResponse DTO 필요
        String accessToken = createToken(user, accessExp);
        String refreshToken = createToken(user, refreshExp); // Refresh Token도 동일 로직으로 생성

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken) // Refresh Token 추가
                .tokenType("Bearer")
                .build();
    }

    // 로그인시 토큰 생성 메서드
    private String createToken(User user, long expTime) {
        // claims - jwt 토큰 속 내용 생성
        Claims claims = Jwts.claims().setSubject(user.getProviderId());
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
     * 해당 필터가 유효한지 검증
     * 시큐리티 필터에서 토큰의 유효성을 검증할 때 사용
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            // TODO: 여기서 Redis를 조회하여 로그아웃된(Blacklist) 토큰인지 확인하는 로직 추가 필요
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
                throw new MalformedJwtException("잘못된 JWT 서명입니다."); // 예외 다시 던짐
        } catch (ExpiredJwtException e) { // 만료 구분
            log.info("만료된 JWT 토큰입니다.");
            throw e; // 예외를 먹지 않고 던져서 필터가 알게 함
        } catch (Exception e) {
            log.info("유효하지 않은 JWT 토큰입니다.");
            return false;
        }
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
            return e.getClaims();
        }
    }
}