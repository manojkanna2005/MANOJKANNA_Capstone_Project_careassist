package com.hexaware.careassist.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDTO {

	private int adminId;

	@NotNull(message = "User ID is required")
	private Integer userId;

	@NotBlank(message = "Full name is required")
	@Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
	@Pattern(regexp = "^[A-Za-z ]+$", message = "Full name can contain only letters and spaces")
	private String fullName;

	@NotBlank(message = "Department is required")
	@Size(min = 3, max = 30, message = "Department must be between 3 and 30 characters")
	@Pattern(regexp = "^[A-Za-z ]+$", message = "Department can contain only letters and spaces")
	private String department;

}