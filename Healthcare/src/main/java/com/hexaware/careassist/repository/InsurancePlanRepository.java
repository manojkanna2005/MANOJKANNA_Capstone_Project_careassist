package com.hexaware.careassist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.careassist.entity.InsurancePlan;


public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, Integer> {
    List<InsurancePlan> findByInsuranceCompanyCompanyId(Integer companyId);
}
