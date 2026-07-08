package com.hexaware.main.careassist.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.main.careassist.dto.InsurancePlanDTO;
import com.hexaware.main.careassist.service.IInsurancePlanService;

import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/v1/insurance-plans")
public class InsurancePlanRestController {

	@Autowired
    private IInsurancePlanService planService;

    @PostMapping("/add")
    public InsurancePlanDTO createPlan(@Valid @RequestBody InsurancePlanDTO dto) {
        return planService.createPlan(dto);
    }

    @PutMapping("/update/{planId}")
    public InsurancePlanDTO updatePlan(@PathVariable Integer planId,
                                       @Valid @RequestBody InsurancePlanDTO dto) {
        return planService.updatePlan(planId, dto);
    }

    @GetMapping("/{planId}")
    public InsurancePlanDTO getPlanById(@PathVariable Integer planId) {
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
    public List<InsurancePlanDTO> getPlansByCompanyId(@PathVariable Integer companyId) {
        return planService.getPlansByCompanyId(companyId);
    }

    @PatchMapping("/activate/{planId}")
    public InsurancePlanDTO activatePlan(@PathVariable Integer planId) {
        return planService.activatePlan(planId);
    }

    @PatchMapping("/deactivate/{planId}")
    public InsurancePlanDTO deactivatePlan(@PathVariable Integer planId) {
        return planService.deactivatePlan(planId);
    }

    @DeleteMapping("/delete/{planId}")
    public String deletePlan(@PathVariable Integer planId) {
        planService.deletePlan(planId);
        return "Insurance plan deleted successfully";
    }
}
