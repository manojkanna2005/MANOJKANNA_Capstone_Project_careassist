package com.hexaware.careassist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.careassist.entity.InsuranceCompany;


public interface InsuranceCompanyRepository extends JpaRepository<InsuranceCompany, Integer> {
    Optional<InsuranceCompany> findTopByAppUserUserIdOrderByCompanyIdDesc(Integer userId);
}
