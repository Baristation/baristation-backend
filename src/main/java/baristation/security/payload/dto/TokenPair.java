package baristation.security.payload.dto;

import lombok.Builder;

@Builder
public record TokenPair(
    String accessToken,
    String refreshToken,
    String tokenType // "Bearer"
) {
}