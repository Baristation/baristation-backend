package dripnote.user.service;

import dripnote.common.redis.RedisService;
import dripnote.user.domain.User;
import dripnote.user.payload.dto.oauth.GoogleUserInfoDTO;
import dripnote.user.payload.dto.oauth.KakaoUserInfoDTO;
import dripnote.user.payload.dto.oauth.NaverUserInfoDTO;
import dripnote.user.payload.dto.oauth.OAuth2UserInfo;
import dripnote.user.enums.UserRole;
import dripnote.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final RedisService redisService;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo oAuth2UserInfo = null;
        if (registrationId.equals("google")) {
            log.info("구글 로그인 요청");
            oAuth2UserInfo = new GoogleUserInfoDTO(attributes);
        } else if (registrationId.equals("naver")) {
            log.info("네이버 로그인 요청");
            oAuth2UserInfo = new NaverUserInfoDTO(attributes);
        } else if (registrationId.equals("kakao")) {
            log.info("카카오 로그인 요청");
            oAuth2UserInfo = new KakaoUserInfoDTO(attributes);
        }
        saveOrUpdateUser(oAuth2UserInfo);

        // 5. 시큐리티 세션에 담기 위해 원본 객체를 그대로 반환합니다.
        // (이 반환값이 나중에 우리가 만들 SuccessHandler로 고스란히 전달됩니다.)

        return oAuth2User;
    }

    // Id를 통해 조회하도록 수정하였습니다.
    private void saveOrUpdateUser(OAuth2UserInfo userInfo) {
        if (userInfo == null) return;

        Optional<User> userOptional = userRepository.findByProviderAndProviderId(
                userInfo.getProvider(),
                userInfo.getProviderId()
        );
        // refershToken redis 저장
        if (userOptional.isEmpty()) {
            // 닉네임 중복 방지
            String nickname = generateUniqueNickname(userInfo.getName());

            User newUser = User.builder()
                    .email(userInfo.getEmail())
                    .provider(userInfo.getProvider())
                    .providerId(userInfo.getProviderId())
                    .nickname(nickname)
                    .role(UserRole.USER)
                    .build();

            userRepository.save(newUser);
            log.info("신규 소셜 유저 회원가입 완료! : {} ", userInfo.getName());
        }
    }
    private String generateUniqueNickname(String baseName) {
        String nickname = baseName;
        // 닉네임 중복 확인
        while (userRepository.existsByNickname(nickname)) {
            // 중복이라면 뒤에 4자리 난수를 붙여서 다시 체크
            nickname = baseName + "_" + (int)(Math.random() * 9000 + 1000);
        }
        return nickname;
    }
}
