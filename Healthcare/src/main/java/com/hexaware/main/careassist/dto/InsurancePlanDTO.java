package com.hexaware.main.careassist.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsurancePlanDTO {

	private int planId;

	@NotNull(message = "Insurance company ID is required")
	private Integer companyId;

	@NotBlank(message = "Plan name is required")
	@Size(min = 3, max = 100, message = "Plan name must be between 3 and 100 characters")
	private String planName;

	@NotBlank(message = "Plan description is required")
	@Size(min = 10, max = 500, message = "Plan description must be between 10 and 500 characters")
	private String planDescription;

	@NotNull(message = "Coverage amount is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Coverage amount must be greater than 0")
	private BigDecimal coverageAmount;

	@NotNull(message = "Premium amount is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Premium amount must be greater than 0")
	private BigDecimal premiumAmount;

	@Min(value = 1, message = "Validity months must be at least 1")
	@Max(value = 120, message = "Validity months cannot exceed 120")
	private int validityMonths;

	private boolean isActive;
}