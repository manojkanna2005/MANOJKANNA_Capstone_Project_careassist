package com.hexaware.careassist.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "email_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id")
	private int notificatoinId;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private AppUser appUser;

	@Column(name = "subject")
	private String subject;

	@Column(name = "message")
	private String message;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	@Column(name = "status")
	private String status;

}
