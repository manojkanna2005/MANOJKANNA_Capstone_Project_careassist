package com.hexaware.careassist.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationDTO {

	private int notificationId;

	@NotNull(message = "User ID is required")
	private Integer userId;

	@NotBlank(message = "Subject is required")
	@Size(min = 3, max = 100, message = "Subject must be between 3 and 100 characters")
	private String subject;

	@NotBlank(message = "Message is required")
	@Size(min = 10, max = 1000, message = "Message must be between 10 and 1000 characters")
	private String message;

	@NotNull(message = "Sent time is required")
	@PastOrPresent(message = "Sent time cannot be in the future")
	private LocalDateTime sentAt;

	@NotBlank(message = "Status is required")
	@Pattern(regexp = "PENDING|SENT|FAILED", message = "Status must be PENDING, SENT, or FAILED")
	private String status;
}