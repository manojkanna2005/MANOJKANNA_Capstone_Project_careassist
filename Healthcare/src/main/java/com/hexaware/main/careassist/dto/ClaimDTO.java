package com.hexaware.main.careassist.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
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
    private Integer patientId;

    @NotNull(message = "Invoice ID is required")
    private Integer invoiceId;

    @NotNull(message = "Insurance company ID is required")
    private Integer companyId;

    @NotNull(message = "Active insurance policy is required")
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
    @DecimalMin(value = "1.0", message = "Claim amount must be greater than 0")
    private BigDecimal claimAmount;

    private LocalDateTime submissionDate;
    private LocalDateTime approvalDate;

    @Pattern(regexp = "PENDING|APPROVED|REJECTED", message = "Status must be PENDING, APPROVED, or REJECTED")
    private String status;

    @Size(max = 255, message = "Rejection reason cannot exceed 255 characters")
    private String rejectionReason;

    // Enriched claim-history fields returned by the backend.
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
    private Long documentCount;
}
