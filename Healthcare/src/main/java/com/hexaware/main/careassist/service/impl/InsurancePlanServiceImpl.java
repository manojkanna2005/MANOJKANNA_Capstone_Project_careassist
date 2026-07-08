package com.hexaware.main.careassist.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.main.careassist.dto.InsurancePlanDTO;
import com.hexaware.main.careassist.entity.InsuranceCompany;
import com.hexaware.main.careassist.entity.InsurancePlan;
import com.hexaware.main.careassist.exception.ResourceNotFoundException;
import com.hexaware.main.careassist.repository.InsuranceCompanyRepository;
import com.hexaware.main.careassist.repository.InsurancePlanRepository;
import com.hexaware.main.careassist.service.IInsurancePlanService;

@Service
@Transactional
public class InsurancePlanServiceImpl implements IInsurancePlanService {

	@Autowired
    private InsurancePlanRepository planRepository;
    @Autowired
	private InsuranceCompanyRepository companyRepository;

    @Override
    public InsurancePlanDTO createPlan(InsurancePlanDTO dto) {
        return toDTO(planRepository.save(toEntity(dto)));
    }

    @Override
    public InsurancePlanDTO updatePlan(Integer planId, InsurancePlanDTO dto) {
        InsurancePlan plan = getPlanEntity(planId);
        plan.setInsuranceCompany(getCompany(dto.getCompanyId()));
        plan.setPlanName(dto.getPlanName());
        plan.setPlanDescription(dto.getPlanDescription());
        plan.setCoverageAmount(dto.getCoverageAmount());
        plan.setPremiumAmount(dto.getPremiumAmount());
        plan.setValidityMonths(dto.getValidityMonths());
        plan.setActive(dto.isActive());
        return toDTO(planRepository.save(plan));
    }

    @Override
    public InsurancePlanDTO getPlanById(Integer planId) {
        return toDTO(getPlanEntity(planId));
    }

    @Override
    public List<InsurancePlanDTO> getAllPlans() {
        return planRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<InsurancePlanDTO> getActivePlans() {
        return planRepository.findAll().stream()
                .filter(InsurancePlan::isActive)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InsurancePlanDTO> getPlansByCompanyId(Integer companyId) {
        return planRepository.findByInsuranceCompanyCompanyId(companyId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InsurancePlanDTO activatePlan(Integer planId) {
        InsurancePlan plan = getPlanEntity(planId);
        plan.setActive(true);
        return toDTO(planRepository.save(plan));
    }

    @Override
    public InsurancePlanDTO deactivatePlan(Integer planId) {
        InsurancePlan plan = getPlanEntity(planId);
        plan.setActive(false);
        return toDTO(planRepository.save(plan));
    }

    @Override
    public void deletePlan(Integer planId) {
        planRepository.delete(getPlanEntity(planId));
    }

    private InsurancePlan getPlanEntity(Integer planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance plan not found with id: " + planId));
    }

    private InsuranceCompany getCompany(Integer companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company not found with id: " + companyId));
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
        plan.setPlanId(dto.getPlanId());
        plan.setInsuranceCompany(getCompany(dto.getCompanyId()));
        plan.setPlanName(dto.getPlanName());
        plan.setPlanDescription(dto.getPlanDescription());
        plan.setCoverageAmount(dto.getCoverageAmount());
        plan.setPremiumAmount(dto.getPremiumAmount());
        plan.setValidityMonths(dto.getValidityMonths());
        plan.setActive(dto.isActive());
        return plan;
    }
}
