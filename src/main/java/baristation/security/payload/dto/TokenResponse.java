package baristation.security.payload.dto;

import lombok.Builder;

@Builder
public record TokenResponse(
    String accessToken,
    String tokenType // "Bearer"
) {
}