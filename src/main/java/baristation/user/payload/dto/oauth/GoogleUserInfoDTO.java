package baristation.user.payload.dto.oauth;

import baristation.user.enums.UserProvider;

import java.util.Map;

public record GoogleUserInfoDTO(Map<String, Object> attributes) implements OAuth2UserInfo {
    // OAuth2 응답 Map 파싱: 정적 팩토리 메서드 사용
    public static GoogleUserInfoDTO from(Map<String, Object> attributes) {
        return new GoogleUserInfoDTO(attributes);
    }

    @Override
    public UserProvider getProvider() {
        return UserProvider.GOOGLE;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}