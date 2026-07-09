package com.hexaware.main.careassist.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.main.careassist.dto.InsurancePlanDTO;
import com.hexaware.main.careassist.entity.InsuranceCompany;
import com.hexaware.main.careassist.entity.InsurancePlan;
import com.hexaware.main.careassist.entity.PatientInsurance;
import com.hexaware.main.careassist.exception.BusinessValidationException;
import com.hexaware.main.careassist.exception.ResourceNotFoundException;
import com.hexaware.main.careassist.repository.ClaimRepository;
import com.hexaware.main.careassist.repository.InsuranceCompanyRepository;
import com.hexaware.main.careassist.repository.InsurancePlanRepository;
import com.hexaware.main.careassist.repository.PatientInsuranceRepository;
import com.hexaware.main.careassist.service.IInsurancePlanService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InsurancePlanServiceImpl implements IInsurancePlanService {

    private static final BigDecimal MAX_MONEY_AMOUNT = new BigDecimal("9999999999.99");

    private final InsurancePlanRepository planRepository;
    private final InsuranceCompanyRepository companyRepository;
    private final PatientInsuranceRepository patientInsuranceRepository;
    private final ClaimRepository claimRepository;

    @Override
    public InsurancePlanDTO createPlan(InsurancePlanDTO dto) {
        InsuranceCompany company = getCompany(dto.getCompanyId());
        validateCompanyOwner(company);
        validateAmounts(dto);

        InsurancePlan plan = toEntity(dto);
        plan.setInsuranceCompany(company);
        return toDTO(planRepository.save(plan));
    }

    @Override
    public InsurancePlanDTO updatePlan(Integer planId, InsurancePlanDTO dto) {
        InsurancePlan plan = getPlanEntity(planId);
        validateCompanyOwner(plan.getInsuranceCompany());
        validateAmounts(dto);

        if (dto.getCompanyId() == null
                || dto.getCompanyId() != plan.getInsuranceCompany().getCompanyId()) {
            throw new BusinessValidationException(
                    "companyId",
                    "An existing insurance plan cannot be moved to another company.");
        }

        validateCoverageNotBelowReservedAmount(planId, dto.getCoverageAmount());
        if (!dto.isActive() && hasCurrentActiveEnrollments(planId)) {
            throw new BusinessValidationException(
                    "active",
                    "A plan with current active patient enrollments cannot be deactivated.");
        }

        plan.setPlanName(dto.getPlanName().trim());
        plan.setPlanDescription(dto.getPlanDescription().trim());
        plan.setCoverageAmount(dto.getCoverageAmount());
        plan.setPremiumAmount(dto.getPremiumAmount());
        plan.setValidityMonths(dto.getValidityMonths());
        plan.setActive(dto.isActive());
        return toDTO(planRepository.save(plan));
    }

    @Override
    @Transactional(readOnly = true)
    public InsurancePlanDTO getPlanById(Integer planId) {
        return toDTO(getPlanEntity(planId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsurancePlanDTO> getAllPlans() {
        return planRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsurancePlanDTO> getActivePlans() {
        return planRepository.findAll().stream()
                .filter(InsurancePlan::isActive)
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsurancePlanDTO> getPlansByCompanyId(Integer companyId) {
        return planRepository.findByInsuranceCompanyCompanyId(companyId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public InsurancePlanDTO activatePlan(Integer planId) {
        InsurancePlan plan = getPlanEntity(planId);
        validateCompanyOwner(plan.getInsuranceCompany());
        plan.setActive(true);
        return toDTO(planRepository.save(plan));
    }

    @Override
    public InsurancePlanDTO deactivatePlan(Integer planId) {
        InsurancePlan plan = getPlanEntity(planId);
        validateCompanyOwner(plan.getInsuranceCompany());
        if (hasCurrentActiveEnrollments(planId)) {
            throw new BusinessValidationException(
                    "planId",
                    "A plan with current active patient enrollments cannot be deactivated.");
        }
        plan.setActive(false);
        return toDTO(planRepository.save(plan));
    }

    @Override
    public void deletePlan(Integer planId) {
        InsurancePlan plan = getPlanEntity(planId);
        validateCompanyOwner(plan.getInsuranceCompany());
        if (patientInsuranceRepository.existsByInsurancePlanPlanId(planId)) {
            throw new BusinessValidationException(
                    "planId",
                    "A plan used by patient insurance enrollments cannot be deleted. Deactivate it after all enrollments expire instead.");
        }
        planRepository.delete(plan);
    }

    private void validateAmounts(InsurancePlanDTO dto) {
        validatePositiveMoney(dto.getCoverageAmount(), "coverageAmount", "Coverage amount");
        validatePositiveMoney(dto.getPremiumAmount(), "premiumAmount", "Premium amount");
        if (dto.getValidityMonths() < 1 || dto.getValidityMonths() > 120) {
            throw new BusinessValidationException(
                    "validityMonths",
                    "Validity months must be between 1 and 120.");
        }
        if (dto.getPlanName() == null
                || dto.getPlanName().trim().length() < 3
                || dto.getPlanName().trim().length() > 100) {
            throw new BusinessValidationException(
                    "planName",
                    "Plan name must be between 3 and 100 characters.");
        }
        if (dto.getPlanDescription() == null
                || dto.getPlanDescription().trim().length() < 10
                || dto.getPlanDescription().trim().length() > 500) {
            throw new BusinessValidationException(
                    "planDescription",
                    "Plan description must be between 10 and 500 characters.");
        }
    }

    private void validatePositiveMoney(BigDecimal amount, String field, String label) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException(field, label + " must be greater than 0.");
        }
        if (amount.compareTo(MAX_MONEY_AMOUNT) > 0) {
            throw new BusinessValidationException(field, label + " cannot exceed " + MAX_MONEY_AMOUNT + ".");
        }
        if (Math.max(0, amount.stripTrailingZeros().scale()) > 2) {
            throw new BusinessValidationException(field, label + " can have at most 2 decimal places.");
        }
    }

    private void validateCoverageNotBelowReservedAmount(Integer planId, BigDecimal newCoverage) {
        for (PatientInsurance enrollment : patientInsuranceRepository.findByInsurancePlanPlanId(planId)) {
            BigDecimal approvedUsed = safeAmount(
                    claimRepository.sumApprovedAmountByEnrollmentId(enrollment.getEnrollmentId()));
            if (newCoverage.compareTo(approvedUsed) < 0) {
                throw new BusinessValidationException(
                        "coverageAmount",
                        "Coverage cannot be reduced below " + approvedUsed
                                + " because that amount is already approved for enrollment "
                                + enrollment.getEnrollmentId() + ".");
            }
        }
    }

    private boolean hasCurrentActiveEnrollments(Integer planId) {
        LocalDate today = LocalDate.now();
        return patientInsuranceRepository.findByInsurancePlanPlanId(planId).stream()
                .anyMatch(enrollment -> "ACTIVE".equalsIgnoreCase(enrollment.getStatus())
                        && enrollment.getEnrollmentDate() != null
                        && enrollment.getExpiryDate() != null
                        && !today.isBefore(enrollment.getEnrollmentDate())
                        && !today.isAfter(enrollment.getExpiryDate()));
    }

    private void validateCompanyOwner(InsuranceCompany company) {
        Authentication authentication = currentAuthentication();
        if (authentication == null || hasRole(authentication, "ROLE_ADMIN")) {
            return;
        }
        if (!hasRole(authentication, "ROLE_INSURANCE")
                || company == null
                || company.getAppUser() == null
                || !authentication.getName().equalsIgnoreCase(company.getAppUser().getEmail())) {
            throw new AccessDeniedException(
                    "Insurance companies can modify only their own plans.");
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

    private InsurancePlan getPlanEntity(Integer planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Insurance plan not found with id: " + planId));
    }

    private InsuranceCompany getCompany(Integer companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Insurance company not found with id: " + companyId));
    }

    private InsurancePlanDTO toDTO(InsurancePlan plan) {
        InsurancePlanDTO dto = new InsurancePlanDTO();
        dto.setPlanId(plan.getPlanId());
        dto.setCompanyId(plan.getInsuranceCompany().getCompanyId());
        dto.setPlanName(plan.getPlanName());
        dto.setPlanDescription(plan.getPlanDescription());
        dto.setCoverageAmount(plan.getCoverageAmount());
        dto.setPremiumAmount(plan.getPremiumAmount());
        dto.setValidityMonths(plan.getValidityMonths());
        dto.setActive(plan.isActive());
        return dto;
    }

    private InsurancePlan toEntity(InsurancePlanDTO dto) {
        InsurancePlan plan = new InsurancePlan();
        plan.setPlanName(dto.getPlanName().trim());
        plan.setPlanDescription(dto.getPlanDescription().trim());
        plan.setCoverageAmount(dto.getCoverageAmount());
        plan.setPremiumAmount(dto.getPremiumAmount());
        plan.setValidityMonths(dto.getValidityMonths());
        plan.setActive(dto.isActive());
        return plan;
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
