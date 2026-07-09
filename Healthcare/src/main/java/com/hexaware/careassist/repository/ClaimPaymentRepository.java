package com.hexaware.careassist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.careassist.entity.ClaimPayment;

public interface ClaimPaymentRepository extends JpaRepository<ClaimPayment, Integer> {

    Optional<ClaimPayment> findByClaimClaimId(Integer claimId);

    Optional<ClaimPayment> findFirstByClaimInvoiceInvoiceIdOrderByPaymentIdDesc(Integer invoiceId);

    List<ClaimPayment> findByClaimInsuranceCompanyCompanyIdOrderByPaymentDateDesc(Integer companyId);

    boolean existsByClaimClaimId(Integer claimId);

    boolean existsByTransactionReferenceIgnoreCase(String transactionReference);
}
