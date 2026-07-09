package com.hexaware.main.careassist.dto;

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
public class InvoiceDTO {

    private int invoiceId;

    @NotBlank(message = "Invoice number is required")
    @Size(min = 3, max = 30, message = "Invoice number must be between 3 and 30 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9][A-Za-z0-9/_-]{2,29}$",
            message = "Invoice number can contain only letters, numbers, slash, underscore, and hyphen")
    private String invoiceNumber;

    @NotNull(message = "Healthcare provider ID is required")
    @Positive(message = "Healthcare provider ID must be greater than 0")
    private Integer providerId;

    @NotNull(message = "Patient ID is required")
    @Positive(message = "Patient ID must be greater than 0")
    private Integer patientId;

    @NotNull(message = "Invoice date is required")
    @PastOrPresent(message = "Invoice date cannot be in the future")
    private LocalDate invoiceDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotNull(message = "Consultation fee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Consultation fee cannot be negative")
    @DecimalMax(value = "9999999999.99", message = "Consultation fee is too large")
    @Digits(integer = 10, fraction = 2, message = "Consultation fee can have at most 10 digits and 2 decimal places")
    private BigDecimal consultationFee;

    @NotNull(message = "Diagnostic tests fee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Diagnostic tests fee cannot be negative")
    @DecimalMax(value = "9999999999.99", message = "Diagnostic tests fee is too large")
    @Digits(integer = 10, fraction = 2, message = "Diagnostic tests fee can have at most 10 digits and 2 decimal places")
    private BigDecimal diagnosticTestsFee;

    @NotNull(message = "Diagnostic scan fee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Diagnostic scan fee cannot be negative")
    @DecimalMax(value = "9999999999.99", message = "Diagnostic scan fee is too large")
    @Digits(integer = 10, fraction = 2, message = "Diagnostic scan fee can have at most 10 digits and 2 decimal places")
    private BigDecimal diagnosticScanFee;

    @NotNull(message = "Medications fee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Medications fee cannot be negative")
    @DecimalMax(value = "9999999999.99", message = "Medications fee is too large")
    @Digits(integer = 10, fraction = 2, message = "Medications fee can have at most 10 digits and 2 decimal places")
    private BigDecimal medicationsFee;

    @NotNull(message = "Tax percentage is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tax percentage cannot be negative")
    @DecimalMax(value = "100.0", inclusive = true, message = "Tax percentage cannot exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Tax percentage can have at most 2 decimal places")
    private BigDecimal taxPercentage;

    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    @Pattern(regexp = "PENDING|UNPAID|PAID|OVERDUE|CANCELLED", message = "Status must be PENDING, UNPAID, PAID, OVERDUE, or CANCELLED")
    private String status;

    @PastOrPresent(message = "Created date cannot be in the future")
    private LocalDateTime createdAt;

    private String patientName;
    private String providerName;

    private Integer claimId;
    private String claimStatus;
    private BigDecimal claimAmount;
    private BigDecimal approvedAmount;
    private BigDecimal planCoverageAmount;
    private BigDecimal remainingPlanCoverage;

    private Integer insurancePaymentId;
    private LocalDateTime insurancePaymentDate;
    private BigDecimal insurancePaidAmount;
    private String insurancePaymentMode;
    private String insuranceTransactionReference;
    private boolean insurancePaymentProcessed;

    private Integer paymentId;
    private LocalDateTime paymentDate;
    private BigDecimal paymentAmount;
    private String paymentMethod;
    private String transactionReference;
    private String paymentStatus;

    private BigDecimal patientPaidAmount;
    private BigDecimal remainingAmount;
    private boolean paymentEligible;
    private String paymentEligibilityReason;
}
