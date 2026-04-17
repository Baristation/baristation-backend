package dripnote.security.handler;

import dripnote.common.exception.CustomException;
import dripnote.common.exception.ErrorCode;
import dripnote.common.redis.RedisService;
import dripnote.security.jwt.JwtTokenProvider;
import dripnote.security.payload.dto.TokenResponse;
import dripnote.user.domain.User;
import dripnote.user.payload.dto.oauth.GoogleUserInfoDTO;
import dripnote.user.payload.dto.oauth.KakaoUserInfoDTO;
import dripnote.user.payload.dto.oauth.NaverUserInfoDTO;
import dripnote.user.payload.dto.oauth.OAuth2UserInfo;
import dripnote.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    // OAuth2 로그인 성공 시 처리할 로직
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisService redisService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {


        // 기존 OAuth2 토큰 정보에서 provider와 사용자 정보를 추출.
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
        String provider = oauth2Token.getAuthorizedClientRegistrationId();
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        Map<String, Object> attributes = oauth2User.getAttributes();

        // provider에 맞는 oauthDTO 받기.
        OAuth2UserInfo userInfo = null;
        if (provider.equals("google")) {
            userInfo = GoogleUserInfoDTO.from(attributes);
        } else if (provider.equals("naver")) {
            userInfo = NaverUserInfoDTO.from(attributes);
        } else if (provider.equals("kakao")) {
            userInfo = KakaoUserInfoDTO.from(attributes);
        } else {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        User user = userRepository.findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
                .orElseThrow(() ->
                    new CustomException(ErrorCode.USER_NOT_FOUND));

        TokenResponse tokenResponse = jwtTokenProvider.createTokenSet(user);
        redisService.setRefreshToken(String.valueOf(user.getUserId()), tokenResponse.refreshToken());
        // 리액트로 보낼 url 생성
        String targetUrl = UriComponentsBuilder.fromUriString("https://localhost:3000/main")
                .queryParam("accessToken", tokenResponse.accessToken())
                .queryParam("refreshToken", tokenResponse.refreshToken())
                .build().toUriString();

        log.info("로그인 성공! JWT 발급 완료. targetUrl: {}", targetUrl);

        // url로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
