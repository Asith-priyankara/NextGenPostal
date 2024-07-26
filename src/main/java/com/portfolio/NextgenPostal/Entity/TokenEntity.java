package com.portfolio.NextgenPostal.Entity;

import com.portfolio.NextgenPostal.Enum.Auth;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "token")
public class TokenEntity {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique = true)
    private String toekn;

    @Enumerated(EnumType.STRING)
    private Auth.TokenType tokenType = Auth.TokenType.BEARER;

    private boolean revoked;

    private boolean expired;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private UserEntity user;
}
