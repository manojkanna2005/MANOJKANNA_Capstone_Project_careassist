package com.hexaware.main.careassist.service;

import java.util.List;
import java.util.Map;

import com.hexaware.main.careassist.dto.AdminDTO;
import com.hexaware.main.careassist.dto.ClaimDTO;
import com.hexaware.main.careassist.dto.ClaimPaymentDTO;
import com.hexaware.main.careassist.dto.UserDTO;

public interface IAdminService {
    AdminDTO createAdmin(AdminDTO dto);
    AdminDTO updateAdmin(Integer adminId, AdminDTO dto);
    AdminDTO getAdminById(Integer adminId);
    AdminDTO getAdminByUserId(Integer userId);
    List<AdminDTO> getAllAdmins();
    Map<String, Object> getDashboardSummary();
    List<UserDTO> getAllUsers();
    List<ClaimDTO> getAllClaims();
    List<ClaimPaymentDTO> getAllPayments();
    UserDTO updateUserStatus(Integer userId, boolean active);
}
