package com.hexaware.main.careassist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hexaware.main.careassist.entity.HealthcareProvider;


public interface HealthcareProviderRepository extends JpaRepository<HealthcareProvider, Integer> {
    Optional<HealthcareProvider> findTopByAppUserUserIdOrderByProviderIdDesc(Integer userId);
}
