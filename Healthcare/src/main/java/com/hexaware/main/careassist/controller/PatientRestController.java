package com.hexaware.main.careassist.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.main.careassist.dto.PatientDTO;
import com.hexaware.main.careassist.service.IPatientService;

import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/v1/patients")
public class PatientRestController {

	@Autowired
	private IPatientService patientService;

	@PostMapping("/add")
	public PatientDTO createPatientProfile(@Valid @RequestBody PatientDTO dto) {
		return patientService.createPatientProfile(dto);
	}

	@PutMapping("/update/{patientId}")
	public PatientDTO updatePatientProfile(@PathVariable Integer patientId, @Valid @RequestBody PatientDTO dto) {
		return patientService.updatePatientProfile(patientId, dto);
	}

	@GetMapping("/{patientId}")
	public PatientDTO getPatientById(@PathVariable Integer patientId) {
		return patientService.getPatientById(patientId);
	}

	@GetMapping("/user/{userId}")
	public PatientDTO getPatientByUserId(@PathVariable Integer userId) {
		return patientService.getPatientByUserId(userId);
	}

	@GetMapping("/all")
	public List<PatientDTO> getAllPatients() {
		return patientService.getAllPatients();
	}

	@DeleteMapping("/delete/{patientId}")
	public String deletePatient(@PathVariable Integer patientId) {
		patientService.deletePatient(patientId);
		return "Patient deleted successfully";
	}
}
