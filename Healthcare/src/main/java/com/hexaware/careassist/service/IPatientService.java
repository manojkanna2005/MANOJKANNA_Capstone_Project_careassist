package com.hexaware.careassist.service;

import java.util.List;

import com.hexaware.careassist.dto.PatientDTO;

public interface IPatientService {
    PatientDTO createPatientProfile(PatientDTO dto);
    PatientDTO updatePatientProfile(Integer patientId, PatientDTO dto);
    PatientDTO getPatientById(Integer patientId);
    PatientDTO getPatientByUserId(Integer userId);
    List<PatientDTO> getAllPatients();
    void deletePatient(Integer patientId);
}
