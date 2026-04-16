package dripnote.user.payload.dto.oauth;

import dripnote.user.enums.UserProvider;
import lombok.Builder;

import java.util.Map;

@Builder
public record GoogleUserInfoDTO(Map<String, Object> attributes) implements OAuth2UserInfo {

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