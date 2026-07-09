package com.hexaware.careassist.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "healthcare_providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthcareProvider {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "provider_id")
	private int providerId;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private AppUser appUser;
	@Column(name = "provider_name")
	private String providerName;
	@Column(name = "specialization")
	private String specialization;
	@Column(name = "license_number")
	private String licenseNumber;
	@Column(name = "address")
	private String address;

}
