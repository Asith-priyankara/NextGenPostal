package com.portfolio.NextgenPostal.DTO;

public record AuthenticationResponse(
        String accessToken,
        String refreshToken
) {
}
