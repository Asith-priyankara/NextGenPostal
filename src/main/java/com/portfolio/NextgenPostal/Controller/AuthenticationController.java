package com.portfolio.NextgenPostal.Controller;

import com.portfolio.NextgenPostal.DTO.ActivateAccountRequest;
import com.portfolio.NextgenPostal.DTO.CustomerRegistrationRequest;
import com.portfolio.NextgenPostal.DTO.OfficeRegistrationRequest;
import com.portfolio.NextgenPostal.service.AuthenticationService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/registerCustomer")
    public ResponseEntity<?> registerCustomer(
            @RequestBody @Valid CustomerRegistrationRequest request
    ) throws MessagingException {
        return authenticationService.registerCustomer(request);
    }

    @PostMapping("/registerOffice")
    public ResponseEntity<?> registerOffice(
            @RequestBody @Valid OfficeRegistrationRequest request
    ) throws MessagingException {
        return authenticationService.registerOffice(request);
    }

    @PostMapping("/activate-account")
    public ResponseEntity<?> activateAccount(
            @RequestBody @Valid ActivateAccountRequest activateAccountRequest
            ) throws MessagingException {
        return authenticationService.activateAccount(activateAccountRequest);
    }
}
