package com.hexaware.main.careassist.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.main.careassist.dto.PatientInsuranceDTO;
import com.hexaware.main.careassist.entity.InsurancePlan;
import com.hexaware.main.careassist.entity.Patient;
import com.hexaware.main.careassist.entity.PatientInsurance;
import com.hexaware.main.careassist.exception.BusinessValidationException;
import com.hexaware.main.careassist.exception.ResourceNotFoundException;
import com.hexaware.main.careassist.repository.ClaimRepository;
import com.hexaware.main.careassist.repository.InsurancePlanRepository;
import com.hexaware.main.careassist.repository.PatientInsuranceRepository;
import com.hexaware.main.careassist.repository.PatientRepository;
import com.hexaware.main.careassist.service.IPatientInsuranceService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PatientInsuranceServiceImpl implements IPatientInsuranceService {

    private static final Set<String> OPEN_CLAIM_STATUSES = Set.of("PENDING", "APPROVED");

    private final PatientInsuranceRepository patientInsuranceRepository;
    private final PatientRepository patientRepository;
    private final InsurancePlanRepository planRepository;
    private final ClaimRepository claimRepository;

    @Override
    public PatientInsuranceDTO selectInsurancePlan(PatientInsuranceDTO dto) {
        validateDates(dto);
        Patient patient = getPatient(dto.getPatientId());
        validatePatientOwner(patient);

        InsurancePlan plan = getPlan(dto.getPlanId());
        validatePlanPeriod(dto, plan, true);
        if (!plan.isActive()) {
            throw new BusinessValidationException("planId", "The selected insurance plan is inactive.");
        }
        if (plan.getCoverageAmount() == null
                || plan.getCoverageAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("planId", "The selected plan has no valid coverage limit.");
        }
        boolean duplicateCurrentEnrollment = patientInsuranceRepository
                .findByPatientPatientIdAndStatusOrderByEnrollmentIdDesc(
                        patient.getPatientId(),
                        "ACTIVE")
                .stream()
                .anyMatch(existing -> existing.getInsurancePlan() != null
                        && existing.getInsurancePlan().getPlanId() == plan.getPlanId()
                        && isCurrentlyActive(existing));
        if (duplicateCurrentEnrollment) {
            throw new BusinessValidationException(
                    "planId",
                    "This patient already has a currently active enrollment for the selected plan.");
        }

        PatientInsurance insurance = toEntity(dto);
        insurance.setPatient(patient);
        insurance.setInsurancePlan(plan);
        return toDTO(patientInsuranceRepository.save(insurance));
    }

    @Override
    public PatientInsuranceDTO updatePatientInsurance(Integer enrollmentId, PatientInsuranceDTO dto) {
        validateDates(dto);
        PatientInsurance insurance = getPatientInsuranceEntity(enrollmentId);
        validatePatientOwner(insurance.getPatient());

        if (claimRepository.existsByPatientInsuranceEnrollmentId(enrollmentId)) {
            throw new BusinessValidationException(
                    "enrollmentId",
                    "An insurance enrollment linked to claims cannot be changed.");
        }

        Patient patient = getPatient(dto.getPatientId());
        validatePatientOwner(patient);
        InsurancePlan plan = getPlan(dto.getPlanId());
        validatePlanPeriod(dto, plan, false);
        if (!plan.isActive() && "ACTIVE".equalsIgnoreCase(dto.getStatus())) {
            throw new BusinessValidationException("planId", "An inactive plan cannot be activated.");
        }

        insurance.setPatient(patient);
        insurance.setInsurancePlan(plan);
        insurance.setEnrollmentDate(dto.getEnrollmentDate());
        insurance.setExpiryDate(dto.getExpiryDate());
        insurance.setStatus(normalizeStatus(dto));
        return toDTO(patientInsuranceRepository.save(insurance));
    }

    @Override
    @Transactional(readOnly = true)
    public PatientInsuranceDTO getPatientInsuranceById(Integer enrollmentId) {
        PatientInsurance insurance = getPatientInsuranceEntity(enrollmentId);
        validatePatientOwner(insurance.getPatient());
        return toDTO(insurance);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientInsuranceDTO getActiveInsuranceByPatientId(Integer patientId) {
        Patient patient = getPatient(patientId);
        validatePatientOwner(patient);
        PatientInsurance insurance = patientInsuranceRepository
                .findByPatientPatientIdAndStatusOrderByEnrollmentIdDesc(patientId, "ACTIVE")
                .stream()
                .filter(this::isCurrentlyActive)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active insurance not found for patient id: " + patientId));
        return toDTO(insurance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientInsuranceDTO> getActiveInsurancesByPatientId(Integer patientId) {
        Patient patient = getPatient(patientId);
        validatePatientOwner(patient);
        return patientInsuranceRepository
                .findByPatientPatientIdAndStatusOrderByEnrollmentIdDesc(patientId, "ACTIVE")
                .stream()
                .filter(this::isCurrentlyActive)
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientInsuranceDTO> getInsuranceHistoryByPatientId(Integer patientId) {
        Patient patient = getPatient(patientId);
        validatePatientOwner(patient);
        return patientInsuranceRepository.findByPatientPatientId(patientId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public PatientInsuranceDTO cancelInsurancePlan(Integer enrollmentId) {
        PatientInsurance insurance = getPatientInsuranceEntity(enrollmentId);
        validatePatientOwner(insurance.getPatient());
        if (claimRepository.existsByPatientInsuranceEnrollmentIdAndStatusIn(
                enrollmentId,
                OPEN_CLAIM_STATUSES)) {
            throw new BusinessValidationException(
                    "enrollmentId",
                    "A policy with a pending or approved claim cannot be cancelled.");
        }
        insurance.setStatus("CANCELLED");
        return toDTO(patientInsuranceRepository.save(insurance));
    }

    @Override
    public void deletePatientInsurance(Integer enrollmentId) {
        PatientInsurance insurance = getPatientInsuranceEntity(enrollmentId);
        validatePatientOwner(insurance.getPatient());
        if (claimRepository.existsByPatientInsuranceEnrollmentId(enrollmentId)) {
            throw new BusinessValidationException(
                    "enrollmentId",
                    "An insurance enrollment linked to claims cannot be deleted.");
        }
        patientInsuranceRepository.delete(insurance);
    }


    private void validatePlanPeriod(
            PatientInsuranceDTO dto,
            InsurancePlan plan,
            boolean requireCurrentCoverage) {
        if (plan.getValidityMonths() < 1 || plan.getValidityMonths() > 120) {
            throw new BusinessValidationException(
                    "planId",
                    "The selected plan has an invalid validity period.");
        }

        LocalDate expectedExpiry = dto.getEnrollmentDate().plusMonths(plan.getValidityMonths());
        if (!expectedExpiry.equals(dto.getExpiryDate())) {
            throw new BusinessValidationException(
                    "expiryDate",
                    "Expiry date must match the selected plan validity and should be "
                            + expectedExpiry + ".");
        }

        if (requireCurrentCoverage) {
            LocalDate today = LocalDate.now();
            if (today.isBefore(dto.getEnrollmentDate()) || today.isAfter(dto.getExpiryDate())) {
                throw new BusinessValidationException(
                        "enrollmentDate",
                        "A newly selected policy must cover the current date.");
            }
            if (!"ACTIVE".equalsIgnoreCase(dto.getStatus())) {
                throw new BusinessValidationException(
                        "status",
                        "A selected insurance plan must start with ACTIVE status.");
            }
        }
    }

    private void validateDates(PatientInsuranceDTO dto) {
        if (dto.getEnrollmentDate() == null) {
            throw new BusinessValidationException(
                    "enrollmentDate",
                    "Enrollment date is required.");
        }
        if (dto.getExpiryDate() == null) {
            throw new BusinessValidationException(
                    "expiryDate",
                    "Expiry date is required.");
        }
        if (dto.getEnrollmentDate().isAfter(LocalDate.now())) {
            throw new BusinessValidationException(
                    "enrollmentDate",
                    "Enrollment date cannot be in the future.");
        }
        if (dto.getExpiryDate().isBefore(dto.getEnrollmentDate())) {
            throw new BusinessValidationException(
                    "expiryDate",
                    "Expiry date cannot be before enrollment date.");
        }
    }

    private boolean isCurrentlyActive(PatientInsurance insurance) {
        LocalDate today = LocalDate.now();
        return "ACTIVE".equalsIgnoreCase(insurance.getStatus())
                && insurance.getInsurancePlan() != null
                && insurance.getInsurancePlan().isActive()
                && insurance.getEnrollmentDate() != null
                && insurance.getExpiryDate() != null
                && !today.isBefore(insurance.getEnrollmentDate())
                && !today.isAfter(insurance.getExpiryDate());
    }

    private String normalizeStatus(PatientInsuranceDTO dto) {
        if (dto.getExpiryDate().isBefore(LocalDate.now())) {
            return "EXPIRED";
        }
        return dto.getStatus() == null ? "ACTIVE" : dto.getStatus().trim().toUpperCase();
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

    private void validatePatientOwner(Patient patient) {
        Authentication authentication = currentAuthentication();
        if (authentication == null || hasRole(authentication, "ROLE_ADMIN")) {
            return;
        }
        boolean ownsPatient = hasRole(authentication, "ROLE_PATIENT")
                && patient != null
                && patient.getAppUser() != null
                && authentication.getName().equalsIgnoreCase(patient.getAppUser().getEmail());
        if (!ownsPatient) {
            throw new AccessDeniedException("Patients can access only their own insurance enrollments.");
        }
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    private Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return authentication;
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

        BigDecimal coverage = safeAmount(insurance.getInsurancePlan().getCoverageAmount());
        BigDecimal used = insurance.getEnrollmentId() == 0
                ? BigDecimal.ZERO
                : safeAmount(claimRepository.sumApprovedAmountByEnrollmentId(insurance.getEnrollmentId()));
        dto.setCoverageAmount(coverage);
        dto.setApprovedCoverageUsed(used);
        dto.setRemainingCoverage(coverage.subtract(used).max(BigDecimal.ZERO));
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

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
