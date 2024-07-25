package com.portfolio.NextgenPostal.service;

import com.portfolio.NextgenPostal.DTO.ActivateAccountRequest;
import com.portfolio.NextgenPostal.DTO.CustomerRegistrationRequest;
import com.portfolio.NextgenPostal.DTO.OfficeRegistrationRequest;
import com.portfolio.NextgenPostal.Entity.ActivationCodeEntity;
import com.portfolio.NextgenPostal.Entity.CustomerEntity;
import com.portfolio.NextgenPostal.Entity.PostOfficeEntity;
import com.portfolio.NextgenPostal.Entity.UserEntity;
import com.portfolio.NextgenPostal.Enum.Auth;
import com.portfolio.NextgenPostal.Repository.ActivationCodeRepository;
import com.portfolio.NextgenPostal.Repository.CustomerRepository;
import com.portfolio.NextgenPostal.Repository.PostOfficeRepository;
import com.portfolio.NextgenPostal.Repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PostOfficeRepository postOfficeRepository;
    private final ActivationCodeRepository activationCodeRepository;
    private final EmailService emailService;
    private final Random random;


    @Transactional
    public ResponseEntity<?> registerCustomer(CustomerRegistrationRequest request) throws MessagingException {
        try{
            UserEntity user = UserEntity.builder()
                    .email(request.email())
                    .password(request.password())
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
        } catch (Exception e){
            throw e;
        }
    }

    @Transactional
    public ResponseEntity<?> registerOffice(OfficeRegistrationRequest request) throws MessagingException {
        try {
            UserEntity user = UserEntity.builder()
                    .email(request.email())
                    .password(request.password())
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
            return ResponseEntity.status(200).body("");
        } catch (Exception e) {
            throw e;
        }
    }

    private void sendValidationEmail(UserEntity user) throws MessagingException {
        String activationCode = sendEmailWithActivationCode(user);
        saveActivationCode(activationCode,user);
    }

    private void reSendValidationEmail(UserEntity user) throws MessagingException {
        String activationCode = sendEmailWithActivationCode(user);
        updateActivationCode(activationCode,user);
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

    private String generateRandomCode (Integer upperLimit) {
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

        return ResponseEntity.status(200).body("Account activate");
    }

}
