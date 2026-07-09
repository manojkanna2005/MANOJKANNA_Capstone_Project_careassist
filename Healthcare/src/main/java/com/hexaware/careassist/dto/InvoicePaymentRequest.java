package com.hexaware.careassist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InvoicePaymentRequest {

    @NotNull(message = "Invoice ID is required")
    @Positive(message = "Invoice ID must be greater than 0")
    private Integer invoiceId;

    @NotBlank(message = "Payment method is required")
    @Pattern(
            regexp = "CARD|UPI|NET_BANKING|CASH",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Payment method must be CARD, UPI, NET_BANKING, or CASH"
    )
    private String paymentMethod;
}
