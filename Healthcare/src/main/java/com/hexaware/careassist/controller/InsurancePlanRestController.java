package com.hexaware.careassist.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.careassist.dto.InsurancePlanDTO;
import com.hexaware.careassist.service.IInsurancePlanService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
@RestController
@Validated
@RequestMapping("/api/v1/insurance-plans")
public class InsurancePlanRestController {

	@Autowired
    private IInsurancePlanService planService;

    @PreAuthorize("hasAnyRole('INSURANCE', 'ADMIN')")
    @PostMapping("/add")
    public InsurancePlanDTO createPlan(@Valid @RequestBody InsurancePlanDTO dto) {
        return planService.createPlan(dto);
    }

    @PreAuthorize("hasAnyRole('INSURANCE', 'ADMIN')")
    @PutMapping("/update/{planId}")
    public InsurancePlanDTO updatePlan(@PathVariable @Positive Integer planId,
                                       @Valid @RequestBody InsurancePlanDTO dto) {
        return planService.updatePlan(planId, dto);
    }

    @GetMapping("/{planId}")
    public InsurancePlanDTO getPlanById(@PathVariable @Positive Integer planId) {
        return planService.getPlanById(planId);
    }

    @GetMapping("/all")
    public List<InsurancePlanDTO> getAllPlans() {
        return planService.getAllPlans();
    }

    @GetMapping("/active")
    public List<InsurancePlanDTO> getActivePlans() {
        return planService.getActivePlans();
    }

    @GetMapping("/company/{companyId}")
    public List<InsurancePlanDTO> getPlansByCompanyId(@PathVariable @Positive Integer companyId) {
        return planService.getPlansByCompanyId(companyId);
    }

    @PreAuthorize("hasAnyRole('INSURANCE', 'ADMIN')")
    @PatchMapping("/activate/{planId}")
    public InsurancePlanDTO activatePlan(@PathVariable @Positive Integer planId) {
        return planService.activatePlan(planId);
    }

    @PreAuthorize("hasAnyRole('INSURANCE', 'ADMIN')")
    @PatchMapping("/deactivate/{planId}")
    public InsurancePlanDTO deactivatePlan(@PathVariable @Positive Integer planId) {
        return planService.deactivatePlan(planId);
    }

    @PreAuthorize("hasAnyRole('INSURANCE', 'ADMIN')")
    @DeleteMapping("/delete/{planId}")
    public String deletePlan(@PathVariable @Positive Integer planId) {
        planService.deletePlan(planId);
        return "Insurance plan deleted successfully";
    }
}
