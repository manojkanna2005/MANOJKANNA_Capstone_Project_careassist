package com.hexaware.careassist.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDTO {

    private int claimId;

    @NotNull(message = "Patient ID is required")
    @Positive(message = "Patient ID must be greater than 0")
    private Integer patientId;

    @NotNull(message = "Invoice ID is required")
    @Positive(message = "Invoice ID must be greater than 0")
    private Integer invoiceId;

    @NotNull(message = "Insurance company ID is required")
    @Positive(message = "Insurance company ID must be greater than 0")
    private Integer companyId;

    @NotNull(message = "Active insurance policy is required")
    @Positive(message = "Insurance enrollment ID must be greater than 0")
    private Integer enrollmentId;

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
    @DecimalMin(value = "0.01", message = "Claim amount must be greater than 0")
    @DecimalMax(value = "9999999999.99", message = "Claim amount is too large")
    @Digits(integer = 10, fraction = 2, message = "Claim amount can have at most 10 digits and 2 decimal places")
    private BigDecimal claimAmount;

    private BigDecimal approvedAmount;
    private LocalDateTime submissionDate;
    private LocalDateTime approvalDate;

    @Pattern(regexp = "PENDING|APPROVED|REJECTED", message = "Status must be PENDING, APPROVED, or REJECTED")
    private String status;

    @Size(max = 255, message = "Rejection reason cannot exceed 255 characters")
    private String rejectionReason;

    private String patientName;
    private String patientEmail;
    private String invoiceNumber;
    private BigDecimal invoiceAmount;
    private Integer providerId;
    private String providerName;
    private String companyName;
    private Integer planId;
    private String planName;
    private BigDecimal coverageAmount;
    private BigDecimal approvedCoverageUsed;
    private BigDecimal remainingCoverage;
    private BigDecimal maxApprovableAmount;
    private Integer insurancePaymentId;
    private LocalDateTime insurancePaymentDate;
    private BigDecimal insurancePaidAmount;
    private String insurancePaymentMode;
    private String insuranceTransactionReference;
    private boolean insurancePaymentProcessed;
    private Long documentCount;
}
