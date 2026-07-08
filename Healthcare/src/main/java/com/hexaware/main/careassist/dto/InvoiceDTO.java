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
public class InvoiceDTO {

	private int invoiceId;

	@NotBlank(message = "Invoice number is required")
	@Size(min = 3, max = 30, message = "Invoice number must be between 3 and 30 characters")
	private String invoiceNumber;

	@NotNull(message = "Healthcare provider ID is required")
	private Integer providerId;

	@NotNull(message = "Patient ID is required")
	private Integer patientId;

	@NotNull(message = "Invoice date is required")
	@PastOrPresent(message = "Invoice date cannot be in the future")
	private LocalDate invoiceDate;

	@NotNull(message = "Due date is required")
	private LocalDate dueDate;

	@NotNull(message = "Consultation fee is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "Consultation fee cannot be negative")
	private BigDecimal consultationFee;

	@NotNull(message = "Diagnostic tests fee is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "Diagnostic tests fee cannot be negative")
	private BigDecimal diagnosticTestsFee;

	@NotNull(message = "Diagnostic scan fee is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "Diagnostic scan fee cannot be negative")
	private BigDecimal diagnosticScanFee;

	@NotNull(message = "Medications fee is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "Medications fee cannot be negative")
	private BigDecimal medicationsFee;

	@NotNull(message = "Tax percentage is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "Tax percentage cannot be negative")
	@DecimalMax(value = "100.0", inclusive = true, message = "Tax percentage cannot exceed 100")
	private BigDecimal taxPercentage;

	private BigDecimal taxAmount;

	private BigDecimal totalAmount;

	@NotBlank(message = "Status is required")
	@Pattern(regexp = "PENDING|PAID|OVERDUE|CANCELLED", message = "Status must be PENDING, PAID, OVERDUE, or CANCELLED")
	private String status;

	@NotNull(message = "Created date is required")
	@PastOrPresent(message = "Created date cannot be in the future")
	private LocalDateTime createdAt;
}