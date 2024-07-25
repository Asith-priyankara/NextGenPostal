package com.portfolio.NextgenPostal.Repository;

import com.portfolio.NextgenPostal.Entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer> {
    Optional<CustomerEntity> findByEmail(String email);
}
