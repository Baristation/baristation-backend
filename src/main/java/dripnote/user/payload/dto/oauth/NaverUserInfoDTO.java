package dripnote.user.payload.dto.oauth;

import dripnote.user.enums.UserProvider;

import java.util.Map;

public record NaverUserInfoDTO(Map<String, Object> attributes) implements OAuth2UserInfo {
    // OAuth2 응답 Map 파싱: 정적 팩토리 메서드 사용
    public static NaverUserInfoDTO from(Map<String, Object> attributes) {
        return new NaverUserInfoDTO(attributes);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getResponse() {
        return (Map<String, Object>) attributes.get("response");
    }

    @Override
    public UserProvider getProvider() {
        return UserProvider.NAVER;
    }

    @Override
    public String getProviderId() {
        return (String) getResponse().get("id");
    }

    @Override
    public String getEmail() {
        return (String) getResponse().get("email");
    }

    @Override
    public String getName() {
        return (String) getResponse().get("name");
    }
}