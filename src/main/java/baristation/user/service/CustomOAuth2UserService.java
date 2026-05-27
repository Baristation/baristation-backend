package baristation.user.service;

import baristation.common.annotation.ExternalApiLog;
import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;
import baristation.user.domain.User;
import baristation.user.payload.dto.oauth.GoogleUserInfoDTO;
import baristation.user.payload.dto.oauth.KakaoUserInfoDTO;
import baristation.user.payload.dto.oauth.NaverUserInfoDTO;
import baristation.user.payload.dto.oauth.OAuth2UserInfo;
import baristation.user.enums.UserRole;
import baristation.user.repository.UserRepository;
import baristation.user.validator.NicknameValidator;
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
    private final NicknameValidator nicknameValidator;


    @Override
    @ExternalApiLog("OAuth2 User Loading")
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo oAuth2UserInfo = null;
        if (registrationId.equals("google")) {
            oAuth2UserInfo = GoogleUserInfoDTO.from(attributes);
        } else if (registrationId.equals("naver")) {
            oAuth2UserInfo = NaverUserInfoDTO.from(attributes);
        } else if (registrationId.equals("kakao")) {
            oAuth2UserInfo = KakaoUserInfoDTO.from(attributes);
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

            userRepository.save(newUser);
        }
    }

    /**
     * 고유한 닉네임 생성
     * - 기본 닉네임(사용자 이름)이 유효한지 검증
     * - 중복이면 난수를 붙여서 새로운 닉네임 생성
     * - 최대 10번 시도 후 실패하면 예외 발생
     */
    private String generateUniqueNickname(String baseName) {
        // 기본 닉네임이 유효한 형식인지 확인 (예약어 제외, 특수문자 연속 확인 등)
        if (!nicknameValidator.isValidFormat(baseName)) {
            // 기본 닉네임이 불가능하면 "user_" + 난수로 시작
            baseName = "user_" + (int)(Math.random() * 9000 + 1000);
        }

        String nickname = baseName;

        // DB에 해당 닉네임이 존재하는지 확인하고 (대소문자 무시), 있다면 난수 부여 반복
        int attempts = 0;
        int maxAttempts = 4;

        while (userRepository.existsByNicknameIgnoreCase(nickname) && attempts < maxAttempts) {
            nickname = baseName + (int)(Math.random() * 9000 + 1000);
            attempts++;
        }

        // 4번 시도 후에도 중복이면 예외 발생
        if (attempts >= maxAttempts && userRepository.existsByNicknameIgnoreCase(nickname)) {
            throw new CustomException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }

        return nickname;
    }
}
