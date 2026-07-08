package com.hexaware.main.careassist.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hexaware.main.careassist.entity.Claim;

public interface ClaimRepository extends JpaRepository<Claim, Integer> {
    List<Claim> findByPatientPatientId(Integer patientId);
    List<Claim> findByInsuranceCompanyCompanyId(Integer companyId);
    List<Claim> findByStatus(String status);

    boolean existsByInvoiceInvoiceIdAndStatusIn(Integer invoiceId, Collection<String> statuses);

    @Query("select coalesce(sum(c.claimAmount), 0) from Claim c "
            + "where c.patientInsurance.enrollmentId = :enrollmentId and c.status = 'APPROVED'")
    BigDecimal sumApprovedAmountByEnrollmentId(@Param("enrollmentId") Integer enrollmentId);
}
