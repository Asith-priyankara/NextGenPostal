package com.portfolio.NextgenPostal.service;

import com.portfolio.NextgenPostal.DTO.ActivateAccountRequest;
import com.portfolio.NextgenPostal.DTO.CustomerRegistrationRequest;
import com.portfolio.NextgenPostal.DTO.OfficeRegistrationRequest;
import com.portfolio.NextgenPostal.Entity.ActivationCodeEntity;
import com.portfolio.NextgenPostal.Entity.CustomerEntity;
import com.portfolio.NextgenPostal.Entity.PostOfficeEntity;
import com.portfolio.NextgenPostal.Entity.UserEntity;
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
        if (request.role() == 3) {
            try{
                var user = UserEntity.builder()
                        .email(request.email())
                        .password(request.password())
                        .role(3)
                        .enabled(false)
                        .createdDate(LocalDateTime.now())
                        .build();
                var customer = CustomerEntity.builder()
                        .firstName(request.firstName())
                        .lastName(request.lastName())
                        .email(request.email())
                        .contactNumber(request.contactNumber())
                        .address(request.address())
                        .build();

                userRepository.save(user);
                customerRepository.save(customer);
                sendValidationEmail(user.getEmail(), customer.getFirstName()+customer.getLastName());
                return ResponseEntity.status(200).body("");
            } catch (Exception e){
                throw e;
            }
        } else {
            return ResponseEntity.status(300).body("Invalid");
        }
    }

    @Transactional
    public ResponseEntity<?> registerOffice(OfficeRegistrationRequest request) throws MessagingException {
        if (request.role() == 2) {
            try {
                var user = UserEntity.builder()
                        .email(request.email())
                        .password(request.password())
                        .role(2)
                        .enabled(false)
                        .build();
                var office = PostOfficeEntity.builder()
                        .postOfficeName(request.postOfficeName())
                        .district(request.district())
                        .email(request.email())
                        .telephoneNumber(request.telephoneNumber())
                        .address(request.address())
                        .build();

                userRepository.save(user);
                postOfficeRepository.save(office);
                sendValidationEmail(user.getEmail(), office.getPostOfficeName());
                return ResponseEntity.status(200).body("");
            } catch (Exception e) {
                throw e;
            }
        } else {
            return ResponseEntity.status(300).body("Invalid");
        }
    }

    private void sendValidationEmail(String email, String username) throws MessagingException {
        var activationCode = generateRandomCode(10000);
        saveActivationCode(activationCode,email);
        emailService.sendEmail(
                email,
                username,
                "activate_account",
                activationCode,
                "Account Activation"
        );
    }

    private String generateRandomCode (Integer upperLimit) {
        String randomNumber = String.valueOf(random.nextInt(upperLimit));
        return randomNumber;
    }
    private void saveActivationCode(String code, String email) {
        var activationCode = ActivationCodeEntity.builder()
                .activationCode(code)
                .email(email)
                .expiredAt(LocalDateTime.now().plusMinutes(10))
                .build();

        activationCodeRepository.save(activationCode);
    }

    private void updateActivationCode(String code, String email) {
        ActivationCodeEntity activationCode = activationCodeRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid"));
        activationCode.setActivationCode(code);
        activationCode.setExpiredAt(LocalDateTime.now().plusMinutes(10));

        activationCodeRepository.save(activationCode);
    }

    @Transactional
    public ResponseEntity<?> activateAccount(ActivateAccountRequest activateAccountRequest) throws MessagingException {
        String email = activateAccountRequest.email();
        ActivationCodeEntity activationCode = activationCodeRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid"));
        if (!Objects.equals(activationCode.getActivationCode(), activateAccountRequest.activationCode())) {
            return ResponseEntity.status(300).body("Activation code incorrect");
        }
        if (LocalDateTime.now().isAfter(activationCode.getExpiredAt())) {
            reSendValidationEmail(activationCode.getEmail());
            return ResponseEntity.status(300).body("Re send email");
        }
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid"));
        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.status(200).body("Account activate");

    }

    @Transactional
    protected void reSendValidationEmail(String email) throws MessagingException {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid"));
        String username = " ";
        if (user.getRole() == 3){
            CustomerEntity customer = customerRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid"));
            username= customer.getFirstName()+" "+customer.getLastName();
        } else if (user.getRole() == 2) {
            PostOfficeEntity postOffice = postOfficeRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid"));
            username = postOffice.getPostOfficeName();
        }
        var activationCode = generateRandomCode(10000);
        updateActivationCode(activationCode,email);
        emailService.sendEmail(
                email,
                username,
                "activate_account",
                activationCode,
                "Account Activation"
        );


    }
}
