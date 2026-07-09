package com.hexaware.careassist.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {

	private int patientId;

	@NotNull(message = "User ID is required")
	private Integer userId;

	@NotBlank(message = "Full name is required")
	@Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
	@Pattern(regexp = "^[A-Za-z ]+$", message = "Full name can contain only letters and spaces")
	private String fullName;

	@NotNull(message = "Date of birth is required")
	@Past(message = "Date of birth must be a past date")
	private LocalDate dateOfBirth;

	@NotBlank(message = "Gender is required")
	@Pattern(regexp = "MALE|FEMALE|OTHER", message = "Gender must be MALE, FEMALE, or OTHER")
	private String gender;

	@NotBlank(message = "Address is required")
	@Size(min = 10, max = 255, message = "Address must be between 10 and 255 characters")
	private String address;

	@NotBlank(message = "Symptoms are required")
	@Size(min = 3, max = 500, message = "Symptoms must be between 3 and 500 characters")
	private String symptoms;

	@NotBlank(message = "Treatment is required")
	@Size(min = 3, max = 500, message = "Treatment must be between 3 and 500 characters")
	private String treatment;
}