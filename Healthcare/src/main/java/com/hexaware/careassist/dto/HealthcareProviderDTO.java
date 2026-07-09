package com.hexaware.careassist.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthcareProviderDTO {

	private int providerId;

	@NotNull(message = "User ID is required")
	private Integer userId;

	@NotBlank(message = "Provider name is required")
	@Size(min = 3, max = 100, message = "Provider name must be between 3 and 100 characters")
	private String providerName;

	@NotBlank(message = "Specialization is required")
	@Size(min = 3, max = 50, message = "Specialization must be between 3 and 50 characters")
	private String specialization;

	@NotBlank(message = "License number is required")
	@Size(min = 5, max = 30, message = "License number must be between 5 and 30 characters")
	private String licenseNumber;

	@NotBlank(message = "Address is required")
	@Size(min = 10, max = 255, message = "Address must be between 10 and 255 characters")
	private String address;
}