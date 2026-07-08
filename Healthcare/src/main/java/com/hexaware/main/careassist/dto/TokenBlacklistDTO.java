package com.hexaware.main.careassist.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklistDTO {

	private int id;

	@NotBlank(message = "Token is required")
	@Size(min = 20, max = 1000, message = "Token length must be between 20 and 1000 characters")
	private String token;

	@NotNull(message = "Invalidated date and time is required")
	@PastOrPresent(message = "Invalidated date and time cannot be in the future")
	private LocalDateTime invalidatedAt;

}