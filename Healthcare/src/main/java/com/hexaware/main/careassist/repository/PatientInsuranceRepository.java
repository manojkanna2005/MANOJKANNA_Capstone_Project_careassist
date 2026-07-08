package com.hexaware.main.careassist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.main.careassist.entity.PatientInsurance;


public interface PatientInsuranceRepository extends JpaRepository<PatientInsurance, Integer> {
    List<PatientInsurance> findByPatientPatientId(Integer patientId);
    Optional<PatientInsurance> findByPatientPatientIdAndStatus(Integer patientId, String status);
}
