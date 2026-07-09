package com.hexaware.careassist.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "invoice_id")
	private int invoiceId;

	@Column(name = "invoice_number")
	private String invoiceNumber;

	@ManyToOne
	@JoinColumn(name = "provider_id")
	private HealthcareProvider healthcareProvider;

	@ManyToOne
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@Column(name = "invoice_date")
	private LocalDate invoiceDate;

	@Column(name = "due_date")
	private LocalDate dueDate;

	@Column(name = "consultation_fee")
	private BigDecimal consultationFee;

	@Column(name = "diagnostic_tests_fee")
	private BigDecimal diagnosticTestsFee;

	@Column(name = "diagnostic_scan_fee")
	private BigDecimal diagnosticScanFee;

	@Column(name = "medications_fee")
	private BigDecimal medicationsFee;

	@Column(name = "tax_percentage")
	private BigDecimal taxPercentage;

	@Column(name = "tax_amount")
	private BigDecimal taxAmount;

	@Column(name = "total_amount")
	private BigDecimal totalAmount;

	@Column(name = "status")
	private String status;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

}
