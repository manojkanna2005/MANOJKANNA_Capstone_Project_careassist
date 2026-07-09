package com.hexaware.careassist.service;

import java.util.List;

import com.hexaware.careassist.dto.InsurancePlanDTO;

public interface IInsurancePlanService {
    InsurancePlanDTO createPlan(InsurancePlanDTO dto);
    InsurancePlanDTO updatePlan(Integer planId, InsurancePlanDTO dto);
    InsurancePlanDTO getPlanById(Integer planId);
    List<InsurancePlanDTO> getAllPlans();
    List<InsurancePlanDTO> getActivePlans();
    List<InsurancePlanDTO> getPlansByCompanyId(Integer companyId);
    InsurancePlanDTO activatePlan(Integer planId);
    InsurancePlanDTO deactivatePlan(Integer planId);
    void deletePlan(Integer planId);
}
