package com.hexaware.main.careassist.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.main.careassist.dto.EmailNotificationDTO;
import com.hexaware.main.careassist.dto.ForgotPasswordRequest;
import com.hexaware.main.careassist.dto.LoginRequest;
import com.hexaware.main.careassist.dto.LoginResponse;
import com.hexaware.main.careassist.dto.UserDTO;
import com.hexaware.main.careassist.entity.AppUser;
import com.hexaware.main.careassist.repository.AppUserRepository;
import com.hexaware.main.careassist.security.JwtUtil;
import com.hexaware.main.careassist.service.IEmailNotificationService;
import com.hexaware.main.careassist.service.ITokenBlacklistService;
import com.hexaware.main.careassist.service.IUserService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IUserService userService;

    @Autowired
    private IEmailNotificationService emailNotificationService;

    @Autowired
    private ITokenBlacklistService tokenBlacklistService;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserDTO dto) {

        if (dto.getRole() != null && dto.getRole().equalsIgnoreCase("ADMIN")) {
            throw new IllegalArgumentException("Admin cannot be registered from public register API");
        }

        UserDTO savedUser = userService.createUser(dto);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        AppUser user = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String role = jwtUtil.normalizeRole(user.getRole());
        String token = jwtUtil.generateToken(user);

        EmailNotificationDTO notification = new EmailNotificationDTO();
        notification.setUserId(user.getUserId());
        notification.setSubject("CareAssist login alert");
        notification.setMessage("You logged in to your CareAssist profile.");
        notification.setSentAt(LocalDateTime.now());
        notification.setStatus("PENDING");
        emailNotificationService.createNotification(notification);

        LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                role,
                user.getProfilePicture()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(userService.forgotPassword(request.getEmail()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
        }
        return ResponseEntity.ok("Logout successful. Token blacklisted.");
    }

    @GetMapping("/test")
    public String testAuthApi() {
        return "Auth API v1 is working";
    }
}
