package com.portfolio.NextgenPostal.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ActivateAccountRequest(
        String email,
        @NotEmpty(message = "Activation code is mandatory")
        @NotBlank(message = "Activation code is mandatory")
        String activationCode
) {
}
