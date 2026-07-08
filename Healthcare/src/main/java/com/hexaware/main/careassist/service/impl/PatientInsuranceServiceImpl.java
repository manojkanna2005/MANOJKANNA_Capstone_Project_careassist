package com.hexaware.main.careassist.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.main.careassist.dto.PatientInsuranceDTO;
import com.hexaware.main.careassist.entity.InsurancePlan;
import com.hexaware.main.careassist.entity.Patient;
import com.hexaware.main.careassist.entity.PatientInsurance;
import com.hexaware.main.careassist.exception.BusinessValidationException;
import com.hexaware.main.careassist.exception.ResourceNotFoundException;
import com.hexaware.main.careassist.repository.InsurancePlanRepository;
import com.hexaware.main.careassist.repository.PatientInsuranceRepository;
import com.hexaware.main.careassist.repository.PatientRepository;
import com.hexaware.main.careassist.service.IPatientInsuranceService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PatientInsuranceServiceImpl implements IPatientInsuranceService {

    private final PatientInsuranceRepository patientInsuranceRepository;
    private final PatientRepository patientRepository;
    private final InsurancePlanRepository planRepository;

    @Override
    public PatientInsuranceDTO selectInsurancePlan(PatientInsuranceDTO dto) {
        validateDates(dto);
        InsurancePlan plan = getPlan(dto.getPlanId());
        if (!plan.isActive()) {
            throw new BusinessValidationException("planId", "The selected insurance plan is inactive.");
        }

        PatientInsurance insurance = toEntity(dto);
        insurance.setInsurancePlan(plan);
        return toDTO(patientInsuranceRepository.save(insurance));
    }

    @Override
    public PatientInsuranceDTO updatePatientInsurance(Integer enrollmentId, PatientInsuranceDTO dto) {
        validateDates(dto);
        PatientInsurance insurance = getPatientInsuranceEntity(enrollmentId);
        InsurancePlan plan = getPlan(dto.getPlanId());
        if (!plan.isActive() && "ACTIVE".equalsIgnoreCase(dto.getStatus())) {
            throw new BusinessValidationException("planId", "An inactive plan cannot be activated.");
        }

        insurance.setPatient(getPatient(dto.getPatientId()));
        insurance.setInsurancePlan(plan);
        insurance.setEnrollmentDate(dto.getEnrollmentDate());
        insurance.setExpiryDate(dto.getExpiryDate());
        insurance.setStatus(normalizeStatus(dto));
        return toDTO(patientInsuranceRepository.save(insurance));
    }

    @Override
    @Transactional(readOnly = true)
    public PatientInsuranceDTO getPatientInsuranceById(Integer enrollmentId) {
        return toDTO(getPatientInsuranceEntity(enrollmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public PatientInsuranceDTO getActiveInsuranceByPatientId(Integer patientId) {
        PatientInsurance insurance = patientInsuranceRepository
                .findTopByPatientPatientIdAndStatusOrderByEnrollmentIdDesc(patientId, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active insurance not found for patient id: " + patientId));
        return toDTO(insurance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientInsuranceDTO> getInsuranceHistoryByPatientId(Integer patientId) {
        return patientInsuranceRepository.findByPatientPatientId(patientId).stream()
                .map(this::toDTO)
                .toList();
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

    private void validateDates(PatientInsuranceDTO dto) {
        if (dto.getExpiryDate().isBefore(dto.getEnrollmentDate())) {
            throw new BusinessValidationException(
                    "expiryDate",
                    "Expiry date cannot be before enrollment date.");
        }
    }

    private String normalizeStatus(PatientInsuranceDTO dto) {
        if (dto.getExpiryDate().isBefore(LocalDate.now())) {
            return "EXPIRED";
        }
        return dto.getStatus();
    }

    private PatientInsurance getPatientInsuranceEntity(Integer enrollmentId) {
        return patientInsuranceRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient insurance not found with id: " + enrollmentId));
    }

    private Patient getPatient(Integer patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + patientId));
    }

    private InsurancePlan getPlan(Integer planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Insurance plan not found with id: " + planId));
    }

    private PatientInsuranceDTO toDTO(PatientInsurance insurance) {
        PatientInsuranceDTO dto = new PatientInsuranceDTO();
        dto.setEnrollmentId(insurance.getEnrollmentId());
        dto.setPatientId(insurance.getPatient().getPatientId());
        dto.setPlanId(insurance.getInsurancePlan().getPlanId());
        dto.setEnrollmentDate(insurance.getEnrollmentDate());
        dto.setExpiryDate(insurance.getExpiryDate());
        dto.setStatus(insurance.getStatus());
        dto.setPlanName(insurance.getInsurancePlan().getPlanName());
        dto.setCoverageAmount(insurance.getInsurancePlan().getCoverageAmount());
        dto.setPlanActive(insurance.getInsurancePlan().isActive());
        if (insurance.getInsurancePlan().getInsuranceCompany() != null) {
            dto.setCompanyId(insurance.getInsurancePlan().getInsuranceCompany().getCompanyId());
            dto.setCompanyName(insurance.getInsurancePlan().getInsuranceCompany().getCompanyName());
        }
        return dto;
    }

    private PatientInsurance toEntity(PatientInsuranceDTO dto) {
        PatientInsurance insurance = new PatientInsurance();
        insurance.setEnrollmentId(dto.getEnrollmentId());
        insurance.setPatient(getPatient(dto.getPatientId()));
        insurance.setInsurancePlan(getPlan(dto.getPlanId()));
        insurance.setEnrollmentDate(dto.getEnrollmentDate());
        insurance.setExpiryDate(dto.getExpiryDate());
        insurance.setStatus(normalizeStatus(dto));
        return insurance;
    }
}
