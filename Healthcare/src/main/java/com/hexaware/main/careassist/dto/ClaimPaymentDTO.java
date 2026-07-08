package com.hexaware.main.careassist.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimPaymentDTO {

	private int paymentId;

	@NotNull(message = "Claim ID is required")
	private Integer claimId;

	@NotNull(message = "Payment date is required")
	private LocalDateTime paymentDate;

	@NotNull(message = "Payment amount is required")
	@DecimalMin(value = "1.0", message = "Payment amount must be greater than 0")
	private BigDecimal paymentAmount;

	@NotBlank(message = "Payment mode is required")
	@Pattern(regexp = "CASH|CARD|UPI|NET_BANKING|CHEQUE", message = "Payment mode must be CASH, CARD, UPI, NET_BANKING, or CHEQUE")
	private String paymentMode;

	@NotBlank(message = "Transaction reference is required")
	@Size(min = 5, max = 50, message = "Transaction reference must be between 5 and 50 characters")
	private String transactionReference;
}