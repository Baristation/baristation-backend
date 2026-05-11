package baristation.security.payload.dto;

import lombok.Builder;

@Builder
public record TokenResponse(
    String userName,
    String accessToken,
    String tokenType // "Bearer"
) {
}