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
public class PostOfficeEntity {
    @Id
    @GeneratedValue
    private Integer id;
    private String postOfficeName;
    private String district;
    @Column(unique = true)
    private String email;
    private String telephoneNumber;
    private String address;
}
