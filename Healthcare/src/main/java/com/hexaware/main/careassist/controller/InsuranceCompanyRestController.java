package com.hexaware.main.careassist.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.main.careassist.dto.InsuranceCompanyDTO;
import com.hexaware.main.careassist.service.IInsuranceCompanyService;

import jakarta.validation.Valid;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/insurance-companies")
public class InsuranceCompanyRestController {
	
	@Autowired
	private IInsuranceCompanyService companyService;

	@PostMapping("/add")
	public InsuranceCompanyDTO createCompanyProfile(@Valid @RequestBody InsuranceCompanyDTO dto) {
		return companyService.createCompanyProfile(dto);
	}

	@PutMapping("/update/{companyId}")
	public InsuranceCompanyDTO updateCompanyProfile(@PathVariable Integer companyId,
			@Valid @RequestBody InsuranceCompanyDTO dto) {
		return companyService.updateCompanyProfile(companyId, dto);
	}

	@GetMapping("/{companyId}")
	public InsuranceCompanyDTO getCompanyById(@PathVariable Integer companyId) {
		return companyService.getCompanyById(companyId);
	}

	@GetMapping("/user/{userId}")
	public InsuranceCompanyDTO getCompanyByUserId(@PathVariable Integer userId) {
		return companyService.getCompanyByUserId(userId);
	}

	@GetMapping("/all")
	public List<InsuranceCompanyDTO> getAllCompanies() {
		return companyService.getAllCompanies();
	}

	@DeleteMapping("/delete/{companyId}")
	public String deleteCompany(@PathVariable Integer companyId) {
		companyService.deleteCompany(companyId);
		return "Insurance company deleted successfully";
	}
}
