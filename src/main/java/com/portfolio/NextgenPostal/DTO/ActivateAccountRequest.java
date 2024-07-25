package com.portfolio.NextgenPostal.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ActivateAccountRequest(
        @NotEmpty(message = "Email is mandatory")
        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email is not formatted")
        String email,
        @NotEmpty(message = "Activation code is mandatory")
        @NotBlank(message = "Activation code is mandatory")
        String activationCode
) {
}
