package com.hexaware.main.careassist.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientInsuranceDTO {

	private int enrollmentId;

	@NotNull(message = "Patient ID is required")
	private Integer patientId;

	@NotNull(message = "Insurance Plan ID is required")
	private Integer planId;

	@NotNull(message = "Enrollment date is required")
	@PastOrPresent(message = "Enrollment date cannot be in the future")
	private LocalDate enrollmentDate;

	@NotNull(message = "Expiry date is required")
	private LocalDate expiryDate;

	@NotBlank(message = "Status is required")
	@Pattern(regexp = "ACTIVE|EXPIRED|CANCELLED|PENDING", message = "Status must be ACTIVE, EXPIRED, CANCELLED, or PENDING")
	private String status;

}