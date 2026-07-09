package com.hexaware.careassist.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceCompanyDTO {

	private int companyId;

	@NotNull(message = "User ID is required")
	private Integer userId;

	@NotBlank(message = "Company name is required")
	@Size(min = 3, max = 100, message = "Company name must be between 3 and 100 characters")
	private String companyName;

	@NotBlank(message = "Registration number is required")
	@Size(min = 5, max = 30, message = "Registration number must be between 5 and 30 characters")
	private String registrationNumber;

	@NotBlank(message = "Address is required")
	@Size(min = 10, max = 255, message = "Address must be between 10 and 255 characters")
	private String address;

	@NotBlank(message = "Contact email is required")
	@Email(message = "Invalid email format")
	private String contactEmail;

}