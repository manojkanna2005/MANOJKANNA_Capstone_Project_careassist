package com.hexaware.careassist.entity;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "patient_insurance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientInsurance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "enrollment_id")
	private int enrollmentId;

	@ManyToOne
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@ManyToOne
	@JoinColumn(name = "plan_id")
	private InsurancePlan insurancePlan;

	@Column(name = "enrollment_date")
	private LocalDate enrollmentDate;

	@Column(name = "expiry_date")
	private LocalDate expiryDate;

	@Column(name = "status")
	private String status;

}
