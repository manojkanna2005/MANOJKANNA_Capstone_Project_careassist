package com.hexaware.careassist.service;

import java.util.List;

import com.hexaware.careassist.dto.PatientInsuranceDTO;

public interface IPatientInsuranceService {
    PatientInsuranceDTO selectInsurancePlan(PatientInsuranceDTO dto);
    PatientInsuranceDTO updatePatientInsurance(Integer enrollmentId, PatientInsuranceDTO dto);
    PatientInsuranceDTO getPatientInsuranceById(Integer enrollmentId);
    PatientInsuranceDTO getActiveInsuranceByPatientId(Integer patientId);
    List<PatientInsuranceDTO> getActiveInsurancesByPatientId(Integer patientId);
    List<PatientInsuranceDTO> getInsuranceHistoryByPatientId(Integer patientId);
    PatientInsuranceDTO cancelInsurancePlan(Integer enrollmentId);
    void deletePatientInsurance(Integer enrollmentId);
}
