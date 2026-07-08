package com.hexaware.main.careassist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.main.careassist.entity.Claim;


public interface ClaimRepository extends JpaRepository<Claim, Integer> {
    List<Claim> findByPatientPatientId(Integer patientId);
    List<Claim> findByInsuranceCompanyCompanyId(Integer companyId);
    List<Claim> findByStatus(String status);
}
