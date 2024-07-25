package com.portfolio.NextgenPostal.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "activation_code")
@EntityListeners(AuditingEntityListener.class)
public class ActivationCodeEntity {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(unique = true)
    private String email;
    private String activationCode;
    private LocalDateTime expiredAt;
}
