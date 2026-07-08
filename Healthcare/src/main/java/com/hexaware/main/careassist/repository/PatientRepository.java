package com.hexaware.main.careassist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hexaware.main.careassist.entity.Patient;


public interface PatientRepository extends JpaRepository<Patient, Integer> {
    Optional<Patient> findTopByAppUserUserIdOrderByPatientIdDesc(Integer userId);
}
