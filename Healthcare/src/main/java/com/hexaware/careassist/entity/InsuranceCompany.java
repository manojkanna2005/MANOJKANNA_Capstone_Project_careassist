package com.hexaware.careassist.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "insurance_companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceCompany {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "company_id")
	private int companyId;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private AppUser appUser;

	@Column(name = "company_name")
	private String companyName;

	@Column(name = "registration_number")
	private String registrationNumber;

	@Column(name = "address")
	private String address;

	@Column(name = "contact_email")
	private String contactEmail;

}
