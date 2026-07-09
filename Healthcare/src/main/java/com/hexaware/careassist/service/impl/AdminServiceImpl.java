package com.hexaware.careassist.service.impl;

import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.careassist.dto.AdminDTO;
import com.hexaware.careassist.dto.ClaimDTO;
import com.hexaware.careassist.dto.ClaimPaymentDTO;
import com.hexaware.careassist.dto.UserDTO;
import com.hexaware.careassist.entity.Admin;
import com.hexaware.careassist.entity.AppUser;
import com.hexaware.careassist.entity.ClaimPayment;
import com.hexaware.careassist.exception.ResourceNotFoundException;
import com.hexaware.careassist.repository.AdminRepository;
import com.hexaware.careassist.repository.AppUserRepository;
import com.hexaware.careassist.repository.ClaimPaymentRepository;
import com.hexaware.careassist.repository.ClaimRepository;
import com.hexaware.careassist.service.IAdminService;
import com.hexaware.careassist.service.IClaimService;

@Service
@Transactional
@Slf4j
public class AdminServiceImpl implements IAdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ClaimPaymentRepository paymentRepository;

    @Autowired
    private IClaimService claimService;

    @Override
    public AdminDTO createAdmin(AdminDTO dto) {
        Admin admin = adminRepository.findTopByAppUserUserIdOrderByAdminIdDesc(dto.getUserId())
                .orElseGet(Admin::new);

        if (admin.getAdminId() > 0) {
            log.warn("Admin profile already exists for user id {}. Updating existing admin id {} instead of creating a duplicate.",
                    dto.getUserId(), admin.getAdminId());
        }

        copyDtoToEntity(dto, admin);
        Admin savedAdmin = adminRepository.save(admin);
        return adminToDTO(savedAdmin);
    }

    @Override
    public AdminDTO updateAdmin(Integer adminId, AdminDTO dto) {
        Admin admin = getAdminEntity(adminId);
        copyDtoToEntity(dto, admin);
        Admin updatedAdmin = adminRepository.save(admin);
        return adminToDTO(updatedAdmin);
    }

    @Override
    public AdminDTO getAdminById(Integer adminId) {
        Admin admin = getAdminEntity(adminId);
        return adminToDTO(admin);
    }

    @Override
    public AdminDTO getAdminByUserId(Integer userId) {
        Admin admin = adminRepository.findTopByAppUserUserIdOrderByAdminIdDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found for user id: " + userId));
        return adminToDTO(admin);
    }

    @Override
    public List<AdminDTO> getAllAdmins() {
        return adminRepository.findAll().stream().map(this::adminToDTO).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalUsers", appUserRepository.count());
        summary.put("totalClaims", claimRepository.count());
        summary.put("totalPayments", paymentRepository.count());

        summary.put("pendingClaims", claimRepository.findByStatus("PENDING").size());
        summary.put("approvedClaims", claimRepository.findByStatus("APPROVED").size());
        summary.put("rejectedClaims", claimRepository.findByStatus("REJECTED").size());

        return summary;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return appUserRepository.findAll().stream().map(this::userToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ClaimDTO> getAllClaims() {
        return claimService.getAllClaims();
    }

    @Override
    public List<ClaimPaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream().map(this::paymentToDTO).collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUserStatus(Integer userId, boolean active) {
        AppUser user = getUser(userId);
        user.setActive(active);
        AppUser updatedUser = appUserRepository.save(user);
        return userToDTO(updatedUser);
    }

    private Admin getAdminEntity(Integer adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));
    }

    private AppUser getUser(Integer userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private void copyDtoToEntity(AdminDTO dto, Admin admin) {
        admin.setAppUser(getUser(dto.getUserId()));
        admin.setFullName(dto.getFullName());
        admin.setDepartment(dto.getDepartment());
    }

    private AdminDTO adminToDTO(Admin admin) {
        AdminDTO dto = new AdminDTO();

        dto.setAdminId(admin.getAdminId());
        dto.setUserId(admin.getAppUser().getUserId());
        dto.setFullName(admin.getFullName());
        dto.setDepartment(admin.getDepartment());

        return dto;
    }

    private UserDTO userToDTO(AppUser user) {
        UserDTO dto = new UserDTO();

        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPassword(user.getPassword());
        dto.setRole(user.getRole());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setActive(user.isActive());

        return dto;
    }

    // private ClaimDTO claimToDTO(Claim claim) {
    //     ClaimDTO dto = new ClaimDTO();

    //     dto.setClaimId(claim.getClaimId());
    //     dto.setPatientId(claim.getPatient().getPatientId());
    //     dto.setInvoiceId(claim.getInvoice().getInvoiceId());
    //     dto.setCompanyId(claim.getInsuranceCompany().getCompanyId());
    //     dto.setDiagnosis(claim.getDiagnosis());
    //     dto.setTreatment(claim.getTreatment());
    //     dto.setDateOfService(claim.getDateOfService());
    //     dto.setClaimAmount(claim.getClaimAmount());
    //     dto.setSubmissionDate(claim.getSubmissionDate());
    //     dto.setApprovalDate(claim.getApprovalDate());
    //     dto.setStatus(claim.getStatus());
    //     dto.setRejectionReason(claim.getRejectionReason());

    //     return dto;
    //}

    private ClaimPaymentDTO paymentToDTO(ClaimPayment payment) {
        ClaimPaymentDTO dto = new ClaimPaymentDTO();

        dto.setPaymentId(payment.getPaymentId());
        dto.setClaimId(payment.getClaim().getClaimId());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setPaymentAmount(payment.getPaymentAmount());
        dto.setPaymentMode(payment.getPaymentMode());
        dto.setTransactionReference(payment.getTransactionReference());

        return dto;
    }
}
