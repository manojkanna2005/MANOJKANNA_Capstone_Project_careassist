package com.hexaware.main.careassist.service;

import java.util.List;
import com.hexaware.main.careassist.dto.PatientInsuranceDTO;

public interface IPatientInsuranceService {
    PatientInsuranceDTO selectInsurancePlan(PatientInsuranceDTO dto);
    PatientInsuranceDTO updatePatientInsurance(Integer enrollmentId, PatientInsuranceDTO dto);
    PatientInsuranceDTO getPatientInsuranceById(Integer enrollmentId);
    PatientInsuranceDTO getActiveInsuranceByPatientId(Integer patientId);
    List<PatientInsuranceDTO> getInsuranceHistoryByPatientId(Integer patientId);
    PatientInsuranceDTO cancelInsurancePlan(Integer enrollmentId);
    void deletePatientInsurance(Integer enrollmentId);
}
