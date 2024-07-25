package com.portfolio.NextgenPostal.DTO;

import jakarta.validation.constraints.*;

public record OfficeRegistrationRequest(
        @NotEmpty(message = "Postoffice name is mandatory")
        @NotBlank(message = "Postoffice name is mandatory")
        String postOfficeName,
        @NotEmpty(message = "Address is mandatory")
        @NotBlank(message = "Address is mandatory")
        String address,
        @NotEmpty(message = "District is mandatory")
        @NotBlank(message = "District is mandatory")
        String district,
        @Pattern(regexp = "\\d{9,10}", message = "Telephone number must be 9 or 10 digits")
        String telephoneNumber,
        @NotEmpty(message = "Email is mandatory")
        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email is not formatted")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@slpost\\.lk$", message = "Email should be in the format anything@slpost.lk")
        String email,
        @NotEmpty(message = "Password is mandatory")
        @NotBlank(message = "Password is mandatory")
        @Size(min = 8, message = "Password should be 8 characters long minimum")
        String password,
        Integer role
) {
}
