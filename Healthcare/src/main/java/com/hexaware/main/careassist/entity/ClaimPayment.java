package com.hexaware.main.careassist.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "claim_payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClaimPayment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_id")
	private int paymentId;

	@ManyToOne
	@JoinColumn(name = "claim_id")
	private Claim claim;

	@Column(name = "payment_date")
	private LocalDateTime paymentDate;

	@Column(name = "payment_amount")
	private BigDecimal paymentAmount;

	@Column(name = "payment_mode")
	private String paymentMode;

	@Column(name = "transaction_reference")
	private String transactionReference;

}
