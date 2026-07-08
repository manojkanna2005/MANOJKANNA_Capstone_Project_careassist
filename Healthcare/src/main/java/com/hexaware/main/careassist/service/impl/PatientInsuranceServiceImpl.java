package com.hexaware.main.careassist.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.main.careassist.dto.PatientInsuranceDTO;
import com.hexaware.main.careassist.entity.InsurancePlan;
import com.hexaware.main.careassist.entity.Patient;
import com.hexaware.main.careassist.entity.PatientInsurance;
import com.hexaware.main.careassist.exception.ResourceNotFoundException;
import com.hexaware.main.careassist.repository.InsurancePlanRepository;
import com.hexaware.main.careassist.repository.PatientInsuranceRepository;
import com.hexaware.main.careassist.repository.PatientRepository;
import com.hexaware.main.careassist.service.IPatientInsuranceService;

@Service
@Transactional
public class PatientInsuranceServiceImpl implements IPatientInsuranceService {

	@Autowired
    private PatientInsuranceRepository patientInsuranceRepository;
    @Autowired
	private PatientRepository patientRepository;
    @Autowired
    private InsurancePlanRepository planRepository;

    @Override
    public PatientInsuranceDTO selectInsurancePlan(PatientInsuranceDTO dto) {
        if (dto.getExpiryDate().isBefore(dto.getEnrollmentDate())) {
            throw new IllegalArgumentException("Expiry date cannot be before enrollment date");
        }
        return toDTO(patientInsuranceRepository.save(toEntity(dto)));
    }

    @Override
    public PatientInsuranceDTO updatePatientInsurance(Integer enrollmentId, PatientInsuranceDTO dto) {
        PatientInsurance insurance = getPatientInsuranceEntity(enrollmentId);
        if (dto.getExpiryDate().isBefore(dto.getEnrollmentDate())) {
            throw new IllegalArgumentException("Expiry date cannot be before enrollment date");
        }
        insurance.setPatient(getPatient(dto.getPatientId()));
        insurance.setInsurancePlan(getPlan(dto.getPlanId()));
        insurance.setEnrollmentDate(dto.getEnrollmentDate());
        insurance.setExpiryDate(dto.getExpiryDate());
        insurance.setStatus(dto.getStatus());
        return toDTO(patientInsuranceRepository.save(insurance));
    }

    @Override
    public PatientInsuranceDTO getPatientInsuranceById(Integer enrollmentId) {
        return toDTO(getPatientInsuranceEntity(enrollmentId));
    }

    @Override
    public PatientInsuranceDTO getActiveInsuranceByPatientId(Integer patientId) {
        PatientInsurance insurance = patientInsuranceRepository
                .findByPatientPatientIdAndStatus(patientId, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Active insurance not found for patient id: " + patientId));
        return toDTO(insurance);
    }

    @Override
    public List<PatientInsuranceDTO> getInsuranceHistoryByPatientId(Integer patientId) {
        return patientInsuranceRepository.findByPatientPatientId(patientId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PatientInsuranceDTO cancelInsurancePlan(Integer enrollmentId) {
        PatientInsurance insurance = getPatientInsuranceEntity(enrollmentId);
        insurance.setStatus("CANCELLED");
        return toDTO(patientInsuranceRepository.save(insurance));
    }

    @Override
    public void deletePatientInsurance(Integer enrollmentId) {
        patientInsuranceRepository.delete(getPatientInsuranceEntity(enrollmentId));
    }

    private PatientInsurance getPatientInsuranceEntity(Integer enrollmentId) {
        return patientInsuranceRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient insurance not found with id: " + enrollmentId));
    }

    private Patient getPatient(Integer patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
    }

    private InsurancePlan getPlan(Integer planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance plan not found with id: " + planId));
    }

    private PatientInsuranceDTO toDTO(PatientInsurance insurance) {
        PatientInsuranceDTO dto = new PatientInsuranceDTO();
        dto.setEnrollmentId(insurance.getEnrollmentId());
        dto.setPatientId(insurance.getPatient().getPatientId());
        dto.setPlanId(insurance.getInsurancePlan().getPlanId());
        dto.setEnrollmentDate(insurance.getEnrollmentDate());
        dto.setExpiryDate(insurance.getExpiryDate());
        dto.setStatus(insurance.getStatus());
        return dto;
    }

    private PatientInsurance toEntity(PatientInsuranceDTO dto) {
        PatientInsurance insurance = new PatientInsurance();
        insurance.setEnrollmentId(dto.getEnrollmentId());
        insurance.setPatient(getPatient(dto.getPatientId()));
        insurance.setInsurancePlan(getPlan(dto.getPlanId()));
        insurance.setEnrollmentDate(dto.getEnrollmentDate());
        insurance.setExpiryDate(dto.getExpiryDate());
        insurance.setStatus(dto.getStatus());
        return insurance;
    }
}
