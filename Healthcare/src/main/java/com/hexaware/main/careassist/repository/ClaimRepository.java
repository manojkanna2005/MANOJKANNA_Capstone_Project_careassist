package com.hexaware.main.careassist.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hexaware.main.careassist.entity.Claim;

public interface ClaimRepository extends JpaRepository<Claim, Integer> {

    List<Claim> findByPatientPatientId(Integer patientId);

    List<Claim> findByInsuranceCompanyCompanyId(Integer companyId);

    @Query("select c from Claim c "
            + "where c.insuranceCompany.companyId = :companyId "
            + "and (upper(c.status) = 'PENDING' "
            + "or (upper(c.status) = 'APPROVED' "
            + "and not exists (select p.paymentId from ClaimPayment p where p.claim = c))) "
            + "order by c.submissionDate desc")
    List<Claim> findActionableClaimsByCompanyId(@Param("companyId") Integer companyId);

    List<Claim> findByStatus(String status);

    Optional<Claim> findFirstByInvoiceInvoiceIdOrderByClaimIdDesc(Integer invoiceId);

    Optional<Claim> findFirstByInvoiceInvoiceIdAndStatusIgnoreCaseOrderByClaimIdDesc(
            Integer invoiceId,
            String status);

    boolean existsByInvoiceInvoiceIdAndStatusIn(
            Integer invoiceId,
            Collection<String> statuses);

    boolean existsByInvoiceInvoiceId(Integer invoiceId);

    boolean existsByPatientInsuranceEnrollmentId(Integer enrollmentId);

    boolean existsByPatientInsuranceEnrollmentIdAndStatusIn(
            Integer enrollmentId,
            Collection<String> statuses);

    @Query("select coalesce(sum(case when c.approvedAmount is null then c.claimAmount else c.approvedAmount end), 0) "
            + "from Claim c "
            + "where c.patientInsurance.enrollmentId = :enrollmentId "
            + "and upper(c.status) = 'APPROVED'")
    BigDecimal sumApprovedAmountByEnrollmentId(@Param("enrollmentId") Integer enrollmentId);
}
