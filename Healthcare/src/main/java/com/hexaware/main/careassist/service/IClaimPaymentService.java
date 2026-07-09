package com.hexaware.main.careassist.service;

import java.util.List;
import com.hexaware.main.careassist.dto.ClaimPaymentDTO;

public interface IClaimPaymentService {
    ClaimPaymentDTO processClaimPayment(ClaimPaymentDTO dto);
    ClaimPaymentDTO getPaymentById(Integer paymentId);
    ClaimPaymentDTO getPaymentByClaimId(Integer claimId);
    List<ClaimPaymentDTO> getAllPayments();
}
