package com.hexaware.main.careassist.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClaimApprovalRequest {

    @NotNull(message = "Approved amount is required")
    @DecimalMin(value = "0.01", message = "Approved amount must be greater than 0")
    @DecimalMax(value = "9999999999.99", message = "Approved amount is too large")
    @Digits(integer = 10, fraction = 2, message = "Approved amount can have at most 10 digits and 2 decimal places")
    private BigDecimal approvedAmount;
}
