package com.hexaware.careassist.controller;

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

import com.hexaware.careassist.dto.HealthcareProviderDTO;
import com.hexaware.careassist.service.IHealthcareProviderService;

import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/v1/providers")
public class HealthcareProviderRestController {
	
	@Autowired
	private IHealthcareProviderService providerService;

	@PostMapping("/add")
	public HealthcareProviderDTO createProviderProfile(@Valid @RequestBody HealthcareProviderDTO dto) {
		return providerService.createProviderProfile(dto);
	}

	@PutMapping("/update/{providerId}")
	public HealthcareProviderDTO updateProviderProfile(@PathVariable Integer providerId,
			@Valid @RequestBody HealthcareProviderDTO dto) {
		return providerService.updateProviderProfile(providerId, dto);
	}

	@GetMapping("/{providerId}")
	public HealthcareProviderDTO getProviderById(@PathVariable Integer providerId) {
		return providerService.getProviderById(providerId);
	}

	@GetMapping("/user/{userId}")
	public HealthcareProviderDTO getProviderByUserId(@PathVariable Integer userId) {
		return providerService.getProviderByUserId(userId);
	}

	@GetMapping("/all")
	public List<HealthcareProviderDTO> getAllProviders() {
		return providerService.getAllProviders();
	}

	@DeleteMapping("/delete/{providerId}")
	public String deleteProvider(@PathVariable Integer providerId) {
		providerService.deleteProvider(providerId);
		return "Healthcare provider deleted successfully";
	}
}
