package com.hexaware.careassist.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ClaimPaymentDTO {

    private int paymentId;

    @NotNull(message = "Claim ID is required")
    @Positive(message = "Claim ID must be greater than 0")
    private Integer claimId;

    private Integer invoiceId;
    private String invoiceNumber;
    private Integer patientId;
    private String patientName;
    private Integer companyId;
    private String companyName;
    private BigDecimal approvedAmount;
    private LocalDateTime paymentDate;
    private BigDecimal paymentAmount;

    @NotBlank(message = "Payment mode is required")
    @Pattern(
            regexp = "CASH|CARD|UPI|NET_BANKING|CHEQUE",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Payment mode must be CASH, CARD, UPI, NET_BANKING, or CHEQUE")
    private String paymentMode;

    @Size(max = 60, message = "Transaction reference cannot exceed 60 characters")
    @Pattern(
            regexp = "^$|^[A-Za-z0-9][A-Za-z0-9/_-]{5,59}$",
            message = "Transaction reference must be 6-60 characters using only letters, numbers, slash, underscore, or hyphen")
    private String transactionReference;
}
