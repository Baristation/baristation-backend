package dripnote.user.service;

import dripnote.common.exception.CustomException;
import dripnote.common.exception.ErrorCode;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo oAuth2UserInfo = null;
        if (registrationId.equals("google")) {
            log.info("구글 로그인 요청");
            oAuth2UserInfo = GoogleUserInfoDTO
                    .builder()
                    .attributes(attributes)
                    .build();
        } else if (registrationId.equals("naver")) {
            log.info("네이버 로그인 요청");
            oAuth2UserInfo = NaverUserInfoDTO
                    .builder()
                    .attributes(attributes)
                    .build();
        } else if (registrationId.equals("kakao")) {
            log.info("카카오 로그인 요청");
            oAuth2UserInfo = KakaoUserInfoDTO
                    .builder()
                    .attributes(attributes)
                    .build();
        }
        saveOrUpdateUser(oAuth2UserInfo);

        return oAuth2User;
    }

    // Id를 통해 조회하도록 수정하였습니다.
    private void saveOrUpdateUser(OAuth2UserInfo userInfo) {
        // 유효하지 않은 OAuth2UserInfo는 예외 처리
        if (userInfo == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Optional<User> userOptional = userRepository.findByProviderAndProviderId(
                userInfo.getProvider(),
                userInfo.getProviderId()
        );

        if (userOptional.isEmpty()) {
            // 복잡한 Retry 없이 사전 검사(while)만으로 닉네임 생성
            String nickname = generateUniqueNickname(userInfo.getName());

            User newUser = User.builder()
                    .email(userInfo.getEmail())
                    .provider(userInfo.getProvider())
                    .providerId(userInfo.getProviderId())
                    .nickname(nickname)
                    .role(UserRole.USER)
                    .build();

            // 여기서 만약 극악의 확률로 동시성 충돌(UNIQUE 위배)이 발생한다면?
            // -> 어차피 GlobalExceptionHandler에서 잡아서 처리되도록 내버려 둡니다.
            userRepository.save(newUser);
            log.info("신규 소셜 유저 회원가입 완료! : {} ", userInfo.getName());
        }
    }

    // 이 메서드 하나면 충분합니다.
    private String generateUniqueNickname(String baseName) {
        String nickname = baseName;

        // DB에 해당 닉네임이 존재하는지 확인하고, 있다면 난수 부여 반복
        while (userRepository.existsByNickname(nickname)) {
            nickname = baseName + "_" + (int)(Math.random() * 9000 + 1000);
        }

        return nickname;
    }
}
