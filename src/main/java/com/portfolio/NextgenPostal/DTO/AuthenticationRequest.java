package com.portfolio.NextgenPostal.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record AuthenticationRequest(
        @NotEmpty(message = "Username(Email) is mandatory")
        @NotBlank(message = "Username(Email) is mandatory")
        String email,
        @NotEmpty(message = "Password is mandatory")
        @NotBlank(message = "Password is mandatory")
        String password) {
}
