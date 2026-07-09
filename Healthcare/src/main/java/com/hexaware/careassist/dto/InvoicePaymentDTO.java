package com.hexaware.careassist.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePaymentDTO {

    private int paymentId;
    private Integer invoiceId;
    private String invoiceNumber;
    private Integer patientId;
    private String patientName;
    private Integer providerId;
    private String providerName;
    private Integer claimId;
    private String claimStatus;
    private BigDecimal invoiceTotal;
    private BigDecimal insurancePaidAmount;
    private BigDecimal paymentAmount;
    private BigDecimal remainingAmount;
    private LocalDateTime paymentDate;
    private String paymentMethod;
    private String transactionReference;
    private String paymentStatus;
}
