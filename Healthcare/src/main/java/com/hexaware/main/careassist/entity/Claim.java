package com.hexaware.main.careassist.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "claim_id")
	private int claimId;

	@ManyToOne
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@ManyToOne
	@JoinColumn(name = "invoice_id")
	private Invoice invoice;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private InsuranceCompany insuranceCompany;

	@Column(name = "diagnosis")
	private String diagnosis;

	@Column(name = "treatment")
	private String treatment;

	@Column(name = "date_of_service")
	private LocalDate dateOfService;

	@Column(name = "claim_amount")
	private BigDecimal claimAmount;

	@Column(name = "submission_date")
	private LocalDateTime submissionDate;

	@Column(name = "approval_date")
	private LocalDateTime approvalDate;

	@Column(name = "status")
	private String status;

	@Column(name = "rejection_reason")
	private String rejectionReason;

}
