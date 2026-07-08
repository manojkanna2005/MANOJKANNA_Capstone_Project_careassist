package com.hexaware.main.careassist.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDTO {

	private int claimId;

	@NotNull(message = "Patient ID is required")
	private Integer patientId;

	@NotNull(message = "Invoice ID is required")
	private Integer invoiceId;

	@NotNull(message = "Insurance company ID is required")
	private Integer companyId;

	@NotBlank(message = "Diagnosis is required")
	@Size(min = 3, max = 100, message = "Diagnosis must be between 3 and 100 characters")
	private String diagnosis;

	@NotBlank(message = "Treatment is required")
	@Size(min = 3, max = 100, message = "Treatment must be between 3 and 100 characters")
	private String treatment;

	@NotNull(message = "Date of service is required")
	@PastOrPresent(message = "Date of service cannot be in the future")
	private LocalDate dateOfService;

	@NotNull(message = "Claim amount is required")
	@DecimalMin(value = "1.0", message = "Claim amount must be greater than 0")
	private BigDecimal claimAmount;

	@NotNull(message = "Submission date is required")
	@PastOrPresent(message = "Submission date cannot be in the future")
	private LocalDateTime submissionDate;

	@PastOrPresent(message = "Approval date cannot be in the future")
	private LocalDateTime approvalDate;

	@NotBlank(message = "Status is required")
	@Pattern(regexp = "PENDING|APPROVED|REJECTED", message = "Status must be PENDING, APPROVED, or REJECTED")
	private String status;

	@Size(max = 255, message = "Rejection reason cannot exceed 255 characters")
	private String rejectionReason;
}