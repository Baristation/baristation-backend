package dripnote.user.payload.dto.oauth;

import dripnote.user.enums.UserProvider;
import java.util.Map;

public record NaverUserInfoDTO(Map<String, Object> attributes) implements OAuth2UserInfo {

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