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
    private Integer activationCodeId;

    private String activationCode;

    private LocalDateTime expiredAt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private UserEntity user;
}
