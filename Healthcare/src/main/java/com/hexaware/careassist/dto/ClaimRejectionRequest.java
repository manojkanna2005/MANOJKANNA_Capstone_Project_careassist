package com.hexaware.careassist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClaimRejectionRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(min = 5, max = 255, message = "Rejection reason must be between 5 and 255 characters")
    private String rejectionReason;
}
