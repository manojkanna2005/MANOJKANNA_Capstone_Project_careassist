package com.hexaware.main.careassist.entity;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "patients")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Patient {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "patient_id")
	private int patientId;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private AppUser appUser;
	@Column(name = "full_name")
	private String fullName;
	@Column(name = "date_of_birth")
	private LocalDate dateOfBirth;
	@Column(name = "gender")
	private String gender;
	@Column(name = "address")
	private String address;
	@Column(name = "symptoms")
	private String symptoms;
	@Column(name = "treatment")
	private String treatment;

}
