package com.hexaware.main.careassist.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "insurance_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsurancePlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "plan_id")
	private int planId;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private InsuranceCompany insuranceCompany;

	@Column(name = "plan_name")
	private String planName;

	@Column(name = "plan_description")
	private String planDescription;

	@Column(name = "coverage_amount")
	private BigDecimal coverageAmount;

	@Column(name = "premium_amount")
	private BigDecimal premiumAmount;

	@Column(name = "validity_months")
	private int validityMonths;

	@Column(name = "is_active")
	private boolean isActive;

}
