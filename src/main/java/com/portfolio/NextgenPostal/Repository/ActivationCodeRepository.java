package com.portfolio.NextgenPostal.Repository;

import com.portfolio.NextgenPostal.Entity.ActivationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivationCodeRepository extends JpaRepository<ActivationCodeEntity, Integer> {
    Optional<ActivationCodeEntity> findByEmail(String email);

}
