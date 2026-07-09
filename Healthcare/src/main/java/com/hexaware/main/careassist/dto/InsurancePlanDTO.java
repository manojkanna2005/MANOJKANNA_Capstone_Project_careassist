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
	@Positive(message = "Insurance company ID must be greater than 0")
	private Integer companyId;

	@NotBlank(message = "Plan name is required")
	@Size(min = 3, max = 100, message = "Plan name must be between 3 and 100 characters")
	private String planName;

	@NotBlank(message = "Plan description is required")
	@Size(min = 10, max = 500, message = "Plan description must be between 10 and 500 characters")
	private String planDescription;

	@NotNull(message = "Coverage amount is required")
	@DecimalMin(value = "0.01", message = "Coverage amount must be greater than 0")
	@DecimalMax(value = "9999999999.99", message = "Coverage amount is too large")
	@Digits(integer = 10, fraction = 2, message = "Coverage amount can have at most 10 digits and 2 decimal places")
	private BigDecimal coverageAmount;

	@NotNull(message = "Premium amount is required")
	@DecimalMin(value = "0.01", message = "Premium amount must be greater than 0")
	@DecimalMax(value = "9999999999.99", message = "Premium amount is too large")
	@Digits(integer = 10, fraction = 2, message = "Premium amount can have at most 10 digits and 2 decimal places")
	private BigDecimal premiumAmount;

	@Min(value = 1, message = "Validity months must be at least 1")
	@Max(value = 120, message = "Validity months cannot exceed 120")
	private int validityMonths;

	private boolean isActive;
}