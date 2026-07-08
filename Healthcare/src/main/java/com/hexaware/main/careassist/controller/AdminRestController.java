package com.hexaware.main.careassist.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.main.careassist.dto.AdminDTO;
import com.hexaware.main.careassist.dto.ClaimDTO;
import com.hexaware.main.careassist.dto.ClaimPaymentDTO;
import com.hexaware.main.careassist.dto.UserDTO;
import com.hexaware.main.careassist.service.IAdminService;
import com.hexaware.main.careassist.service.IUserService;

import jakarta.validation.Valid;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminRestController {

	@Autowired
	private IAdminService adminService;

	@Autowired
	private IUserService userService;

	@PostMapping("/add")
	public AdminDTO createAdmin(@Valid @RequestBody AdminDTO dto) {
		return adminService.createAdmin(dto);
	}

	@PutMapping("/update/{adminId}")
	public AdminDTO updateAdmin(@PathVariable Integer adminId, @Valid @RequestBody AdminDTO dto) {
		return adminService.updateAdmin(adminId, dto);
	}

	@GetMapping("/{adminId}")
	public AdminDTO getAdminById(@PathVariable Integer adminId) {
		return adminService.getAdminById(adminId);
	}

	@GetMapping("/user/{userId}")
	public AdminDTO getAdminByUserId(@PathVariable Integer userId) {
		return adminService.getAdminByUserId(userId);
	}

	@GetMapping("/all")
	public List<AdminDTO> getAllAdmins() {
		return adminService.getAllAdmins();
	}

	@GetMapping("/dashboard")
	public Map<String, Object> getDashboardSummary() {
		return adminService.getDashboardSummary();
	}

	@GetMapping("/users")
	public List<UserDTO> getAllUsersExceptAdmin() {
		return userService.getAllUsersExceptAdmin();
	}

	@GetMapping("/claims")
	public List<ClaimDTO> getAllClaims() {
		return adminService.getAllClaims();
	}

	@GetMapping("/payments")
	public List<ClaimPaymentDTO> getAllPayments() {
		return adminService.getAllPayments();
	}

	@PatchMapping("/users/{userId}/status")
	public UserDTO updateUserStatus(@PathVariable Integer userId, @RequestParam boolean active) {
		return adminService.updateUserStatus(userId, active);
	}
}
