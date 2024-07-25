package com.portfolio.NextgenPostal.DTO;

import jakarta.validation.constraints.*;

public record CustomerRegistrationRequest(
        @NotEmpty(message = "Firstname is mandatory")
        @NotBlank(message = "Firstname is mandatory")
        String firstName,
        @NotEmpty(message = "Lastname is mandatory")
        @NotBlank(message = "Lastname is mandatory")
        String lastName,
        @NotEmpty(message = "Address is mandatory")
        @NotBlank(message = "Address is mandatory")
        String address,
        @Pattern(regexp = "\\d{9,10}", message = "Contact number must be 9 or 10 digits")
        String contactNumber,
        @NotEmpty(message = "Email is mandatory")
        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email is not formatted")
        String email,
        @NotEmpty(message = "Password is mandatory")
        @NotBlank(message = "Password is mandatory")
        @Size(min = 8, message = "Password should be 8 characters long minimum")
        String password,
        Integer role
) {
}
