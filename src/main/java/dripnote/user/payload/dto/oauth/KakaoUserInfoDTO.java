package dripnote.user.payload.dto.oauth;

import dripnote.user.enums.UserProvider;
import lombok.Builder;

import java.util.Map;

@Builder
public record KakaoUserInfoDTO(Map<String, Object> attributes) implements OAuth2UserInfo {
    @Override
    public UserProvider getProvider() {
        return UserProvider.KAKAO;
    }

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");
        return String.valueOf(id);
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }
        Object email = kakaoAccount.get("email");
        return email != null ? String.valueOf(email) : null;
    }

    @Override
    public String getName() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) return "kakao_user";

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null) return "kakao_user";

        Object nickname = profile.get("nickname");
        return nickname != null ? String.valueOf(nickname) : "kakao_user";
    }
}