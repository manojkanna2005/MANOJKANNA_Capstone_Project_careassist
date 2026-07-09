package com.hexaware.careassist.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hexaware.careassist.dto.UserAccountUpdateRequest;
import com.hexaware.careassist.dto.UserDTO;
import com.hexaware.careassist.entity.AppUser;
import com.hexaware.careassist.exception.ResourceNotFoundException;
import com.hexaware.careassist.repository.AppUserRepository;
import com.hexaware.careassist.security.JwtUtil;
import com.hexaware.careassist.service.IMailService;
import com.hexaware.careassist.service.IUserService;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements IUserService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IMailService mailService;

    @Override
    public UserDTO createUser(UserDTO dto) {
        if (appUserRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }

        AppUser user = toEntity(dto);
        user.setUserId(0);
        user.setRole(jwtUtil.normalizeRole(dto.getRole()));
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        user.setActive(true);

        AppUser savedUser = appUserRepository.save(user);
        return toDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(Integer userId, UserDTO dto) {
        AppUser user = getUserEntity(userId);

        if (!user.getEmail().equalsIgnoreCase(dto.getEmail()) && appUserRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(jwtUtil.normalizeRole(dto.getRole()));
        user.setProfilePicture(dto.getProfilePicture());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setActive(dto.isActive());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        AppUser updatedUser = appUserRepository.save(user);
        return toDTO(updatedUser);
    }

    @Override
    public UserDTO updateAccount(Integer userId, UserAccountUpdateRequest request) {
        AppUser user = getUserEntity(userId);

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && appUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        AppUser updatedUser = appUserRepository.save(user);
        return toDTO(updatedUser);
    }

    @Override
    public UserDTO uploadProfilePicture(Integer userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select an image file to upload");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed for profile picture");
        }

        try {
            AppUser user = getUserEntity(userId);
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            user.setProfilePicture("data:" + contentType + ";base64," + base64);
            return toDTO(appUserRepository.save(user));
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to upload profile picture");
        }
    }

    @Override
    public UserDTO deleteProfilePicture(Integer userId) {
        AppUser user = getUserEntity(userId);
        user.setProfilePicture(null);
        return toDTO(appUserRepository.save(user));
    }

    @Override
    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        AppUser user = getUserEntity(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(user);
        mailService.sendSimpleEmail(
                user.getEmail(),
                "CareAssist password changed",
                "Your CareAssist password was changed successfully. If this was not you, please contact support immediately.");
    }

    @Override
    public String forgotPassword(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String temporaryPassword = generateTemporaryPassword(user);
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        appUserRepository.save(user);

        mailService.sendSimpleEmail(
                user.getEmail(),
                "CareAssist temporary password",
                "Your temporary password is: " + temporaryPassword + System.lineSeparator()
                        + "Please login and change your password immediately.");

        return "Temporary password sent to registered email";
    }

    @Override
    public UserDTO getUserById(Integer userId) {
        AppUser user = getUserEntity(userId);
        return toDTO(user);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return toDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return appUserRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getUsersByRole(String role) {
        return appUserRepository.findByRole(jwtUtil.normalizeRole(role))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getAllUsersExceptAdmin() {
        return appUserRepository.findByRoleNot("ADMIN")
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO activateUser(Integer userId) {
        AppUser user = getUserEntity(userId);
        user.setActive(true);
        AppUser updatedUser = appUserRepository.save(user);
        return toDTO(updatedUser);
    }

    @Override
    public UserDTO deactivateUser(Integer userId) {
        AppUser user = getUserEntity(userId);
        user.setActive(false);
        AppUser updatedUser = appUserRepository.save(user);
        return toDTO(updatedUser);
    }

    @Override
    public void deleteUser(Integer userId) {
        AppUser user = getUserEntity(userId);
        appUserRepository.delete(user);
    }

    private String generateTemporaryPassword(AppUser user) {
        String source = user.getEmail() + System.nanoTime();
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(source.getBytes(StandardCharsets.UTF_8));
        return "Temp" + encoded.substring(0, 8) + "A1";
    }

    private AppUser getUserEntity(Integer userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private UserDTO toDTO(AppUser user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPassword(null);
        dto.setRole(user.getRole());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setActive(user.isActive());
        return dto;
    }

    private AppUser toEntity(UserDTO dto) {
        AppUser user = new AppUser();
        user.setUserId(dto.getUserId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(jwtUtil.normalizeRole(dto.getRole()));
        user.setProfilePicture(dto.getProfilePicture());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setCreatedAt(dto.getCreatedAt());
        user.setActive(dto.isActive());
        return user;
    }
}