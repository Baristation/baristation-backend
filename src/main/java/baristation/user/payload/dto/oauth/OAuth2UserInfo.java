package baristation.user.payload.dto.oauth;

import baristation.user.enums.UserProvider;

public interface OAuth2UserInfo {
    UserProvider getProvider();    // "google", "naver", "kakao"
    String getProviderId();  // "10293847...", "abcde123..."
    String getEmail();
    String getName();
}