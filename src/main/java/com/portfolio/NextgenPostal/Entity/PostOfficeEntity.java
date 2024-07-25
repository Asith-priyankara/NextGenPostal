package com.portfolio.NextgenPostal.Entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "officer")
public class PostOfficeEntity  {
    @Id
    @GeneratedValue
    private Integer officeId;

    private String postOfficeName;

    private String district;

    private String telephoneNumber;

    private String address;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private UserEntity user;
}
