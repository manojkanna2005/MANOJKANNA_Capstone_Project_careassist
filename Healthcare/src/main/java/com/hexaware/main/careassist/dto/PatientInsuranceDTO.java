package com.hexaware.main.careassist.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Pattern(
            regexp = "ACTIVE|EXPIRED|CANCELLED|PENDING",
            message = "Status must be ACTIVE, EXPIRED, CANCELLED, or PENDING")
    private String status;

    // Read-only plan/company details used by claim forms and coverage feedback.
    private Integer companyId;
    private String companyName;
    private String planName;
    private BigDecimal coverageAmount;
    private Boolean planActive;
}
