package com.hexaware.main.careassist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.main.careassist.entity.ClaimPayment;

public interface ClaimPaymentRepository extends JpaRepository<ClaimPayment, Integer> {
    Optional<ClaimPayment> findByClaimClaimId(Integer claimId);
}
