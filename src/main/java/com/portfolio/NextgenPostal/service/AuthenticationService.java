package com.portfolio.NextgenPostal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.NextgenPostal.DTO.*;
import com.portfolio.NextgenPostal.Entity.*;
import com.portfolio.NextgenPostal.Enum.Auth;
import com.portfolio.NextgenPostal.Repository.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PostOfficeRepository postOfficeRepository;
    private final ActivationCodeRepository activationCodeRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final Random random;


    @Transactional
    public ResponseEntity<?> registerCustomer(CustomerRegistrationRequest request) throws MessagingException {
        try {
            UserEntity user = UserEntity.builder()
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .role(Auth.Role.CUSTOMER)
                    .enabled(false)
                    .build();
            CustomerEntity customer = CustomerEntity.builder()
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .contactNumber(request.contactNumber())
                    .address(request.address())
                    .user(user)
                    .build();

            userRepository.save(user);
            customerRepository.save(customer);
            sendValidationEmail(user);
            return ResponseEntity.status(200).body("");
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public ResponseEntity<?> registerOffice(OfficeRegistrationRequest request) throws MessagingException {
        try {
            UserEntity user = UserEntity.builder()
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .role(Auth.Role.OFFICE)
                    .enabled(false)
                    .build();
            PostOfficeEntity office = PostOfficeEntity.builder()
                    .postOfficeName(request.postOfficeName())
                    .district(request.district())
                    .telephoneNumber(request.telephoneNumber())
                    .address(request.address())
                    .user(user)
                    .build();

            userRepository.save(user);
            postOfficeRepository.save(office);
            sendValidationEmail(user);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return ResponseEntity.status(200).body("");
        } catch (Exception e) {
            throw e;
        }
    }

    private void sendValidationEmail(UserEntity user) throws MessagingException {
        String activationCode = sendEmailWithActivationCode(user);
        saveActivationCode(activationCode, user);
    }

    private void reSendValidationEmail(UserEntity user) throws MessagingException {
        String activationCode = sendEmailWithActivationCode(user);
        updateActivationCode(activationCode, user);
    }

    private String sendEmailWithActivationCode(UserEntity user) throws MessagingException {
        var activationCode = generateRandomCode(10000);
        String greetingName = " ";
        String email = user.getEmail();
        Auth.Role role = user.getRole();
        if (role == Auth.Role.CUSTOMER) {
            CustomerEntity customer = customerRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Invalid"));
            greetingName = customer.getFirstName() + " " + customer.getLastName();
        } else if (role == Auth.Role.OFFICE) {
            PostOfficeEntity office = postOfficeRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Invalid"));
            greetingName = office.getPostOfficeName() + " Post office";
        }
        emailService.sendEmail(
                email,
                greetingName,
                "activate_account",
                activationCode,
                "Account Activation"
        );
        return activationCode;
    }

    private String generateRandomCode(Integer upperLimit) {
        String randomNumber = String.valueOf(random.nextInt(upperLimit));
        return randomNumber;
    }

    private void saveActivationCode(String code, UserEntity user) {
        var activationCode = ActivationCodeEntity.builder()
                .activationCode(code)
                .expiredAt(LocalDateTime.now().plusMinutes(10))
                .user(user)
                .build();

        activationCodeRepository.save(activationCode);
    }

    private void updateActivationCode(String code, UserEntity user) {
        ActivationCodeEntity activationCode = activationCodeRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Invalid"));
        activationCode.setActivationCode(code);
        activationCode.setExpiredAt(LocalDateTime.now().plusMinutes(10));

        activationCodeRepository.save(activationCode);
    }

    @Transactional
    public ResponseEntity<?> activateAccount(ActivateAccountRequest activateAccountRequest) throws MessagingException {
        String email = activateAccountRequest.email();
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid"));
        ActivationCodeEntity activationCode = activationCodeRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Invalid"));
        if (!Objects.equals(activationCode.getActivationCode(), activateAccountRequest.activationCode())) {
            return ResponseEntity.status(300).body("Activation code incorrect");
        }
        if (LocalDateTime.now().isAfter(activationCode.getExpiredAt())) {
            reSendValidationEmail(user);
            return ResponseEntity.status(300).body("Re send email");
        }
        user.setEnabled(true);
        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user,jwtToken);
        return ResponseEntity.status(200).body(jwtToken);
    }

    private void saveUserToken(UserEntity user, String jwtToken) {
        TokenEntity token = TokenEntity.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(Auth.TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(UserEntity user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getUserId());
        if (validUserTokens.isEmpty()) { return;}
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public ResponseEntity<?> authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        var user = userRepository.findByEmail(request.email()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user,jwtToken);
        return ResponseEntity.status(200).body(jwtToken);

    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = userRepository.findByEmail(userEmail).orElseThrow();
            if (jwtService.isValidRefreshToken(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user,accessToken);
                var authResponse = new AuthenticationResponse(
                        accessToken,
                        refreshToken
                );
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}
