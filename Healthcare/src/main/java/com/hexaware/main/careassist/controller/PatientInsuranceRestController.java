package com.hexaware.main.careassist.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.main.careassist.dto.PatientInsuranceDTO;
import com.hexaware.main.careassist.service.IPatientInsuranceService;

import jakarta.validation.Valid;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/patient-insurance")
public class PatientInsuranceRestController {

	@Autowired
    private IPatientInsuranceService patientInsuranceService;

    @PostMapping("/select")
    public PatientInsuranceDTO selectInsurancePlan(@Valid @RequestBody PatientInsuranceDTO dto) {
        return patientInsuranceService.selectInsurancePlan(dto);
    }

    @PutMapping("/update/{enrollmentId}")
    public PatientInsuranceDTO updatePatientInsurance(@PathVariable Integer enrollmentId,
                                                      @Valid @RequestBody PatientInsuranceDTO dto) {
        return patientInsuranceService.updatePatientInsurance(enrollmentId, dto);
    }

    @GetMapping("/{enrollmentId}")
    public PatientInsuranceDTO getPatientInsuranceById(@PathVariable Integer enrollmentId) {
        return patientInsuranceService.getPatientInsuranceById(enrollmentId);
    }

    @GetMapping("/patient/{patientId}/active")
    public PatientInsuranceDTO getActiveInsuranceByPatientId(@PathVariable Integer patientId) {
        return patientInsuranceService.getActiveInsuranceByPatientId(patientId);
    }

    @GetMapping("/patient/{patientId}/history")
    public List<PatientInsuranceDTO> getInsuranceHistoryByPatientId(@PathVariable Integer patientId) {
        return patientInsuranceService.getInsuranceHistoryByPatientId(patientId);
    }

    @PatchMapping("/cancel/{enrollmentId}")
    public PatientInsuranceDTO cancelInsurancePlan(@PathVariable Integer enrollmentId) {
        return patientInsuranceService.cancelInsurancePlan(enrollmentId);
    }

    @DeleteMapping("/delete/{enrollmentId}")
    public String deletePatientInsurance(@PathVariable Integer enrollmentId) {
        patientInsuranceService.deletePatientInsurance(enrollmentId);
        return "Patient insurance deleted successfully";
    }
}
