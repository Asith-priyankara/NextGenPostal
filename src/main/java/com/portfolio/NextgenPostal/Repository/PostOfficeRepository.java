package com.portfolio.NextgenPostal.Repository;

import com.portfolio.NextgenPostal.Entity.CustomerEntity;
import com.portfolio.NextgenPostal.Entity.PostOfficeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostOfficeRepository extends JpaRepository<PostOfficeEntity, Integer> {
    Optional<PostOfficeEntity> findByEmail(String email);
}
