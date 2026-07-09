package com.hexaware.careassist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.careassist.entity.Patient;


public interface PatientRepository extends JpaRepository<Patient, Integer> {
    Optional<Patient> findTopByAppUserUserIdOrderByPatientIdDesc(Integer userId);
    Optional<Patient> findTopByAppUserEmailIgnoreCaseOrderByPatientIdDesc(String email);
}
