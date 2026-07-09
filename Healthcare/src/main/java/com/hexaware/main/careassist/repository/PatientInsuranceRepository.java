package com.hexaware.main.careassist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.hexaware.main.careassist.entity.PatientInsurance;

public interface PatientInsuranceRepository extends JpaRepository<PatientInsurance, Integer> {
    List<PatientInsurance> findByPatientPatientId(Integer patientId);
    List<PatientInsurance> findByInsurancePlanPlanId(Integer planId);
    boolean existsByInsurancePlanPlanId(Integer planId);
    List<PatientInsurance> findByPatientPatientIdAndStatusOrderByEnrollmentIdDesc(Integer patientId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select insurance from PatientInsurance insurance where insurance.enrollmentId = :enrollmentId")
    Optional<PatientInsurance> findByIdForUpdate(@Param("enrollmentId") Integer enrollmentId);
    Optional<PatientInsurance> findTopByPatientPatientIdAndStatusOrderByEnrollmentIdDesc(Integer patientId, String status);
    Optional<PatientInsurance> findTopByPatientPatientIdAndInsurancePlanInsuranceCompanyCompanyIdAndStatusOrderByEnrollmentIdDesc(
            Integer patientId,
            Integer companyId,
            String status);
}
