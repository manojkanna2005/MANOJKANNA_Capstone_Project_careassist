package com.hexaware.careassist.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admins")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Admin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "admin_id")
	private int adminId;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private AppUser appUser;

	@Column(name = "full_name")
	private String fullName;

	@Column(name = "department")
	private String department;

}
