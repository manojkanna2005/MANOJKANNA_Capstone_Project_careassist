package com.hexaware.main.careassist.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hexaware.careassist.dto.UserAccountUpdateRequest;
import com.hexaware.careassist.dto.UserDTO;
import com.hexaware.careassist.entity.AppUser;
import com.hexaware.careassist.repository.AppUserRepository;
import com.hexaware.careassist.security.JwtUtil;
import com.hexaware.careassist.service.IMailService;
import com.hexaware.careassist.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplMockitoTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private IMailService mailService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserShouldEncodePasswordAndNormalizeRole() {
        UserDTO dto = new UserDTO();
        dto.setUsername("patient");
        dto.setEmail("patient@gmail.com");
        dto.setPassword("Test1234");
        dto.setRole("PATIENT");
        dto.setPhoneNumber("9876543210");

        when(appUserRepository.existsByEmail("patient@gmail.com")).thenReturn(false);
        when(jwtUtil.normalizeRole("PATIENT")).thenReturn("PATIENT");
        when(passwordEncoder.encode("Test1234")).thenReturn("encoded-password");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setUserId(1);
            return user;
        });

        UserDTO saved = userService.createUser(dto);

        assertNotNull(saved);
        assertEquals(1, saved.getUserId());
        assertEquals("PATIENT", saved.getRole());
        verify(passwordEncoder).encode("Test1234");
    }

    @Test
    void updateAccountShouldChangeOnlyBasicAccountFields() {
        AppUser user = appUser();
        UserAccountUpdateRequest request = new UserAccountUpdateRequest();
        request.setUsername("newname");
        request.setEmail("new@gmail.com");
        request.setPhoneNumber("9876543211");

        when(appUserRepository.findById(1)).thenReturn(Optional.of(user));
        when(appUserRepository.existsByEmail("new@gmail.com")).thenReturn(false);
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO updated = userService.updateAccount(1, request);

        assertEquals("newname", updated.getUsername());
        assertEquals("new@gmail.com", updated.getEmail());
        assertEquals("9876543211", updated.getPhoneNumber());
    }

    @Test
    void uploadProfilePictureShouldStoreImageAsBase64DataUrl() {
        AppUser user = appUser();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.png",
                "image/png",
                "image-content".getBytes(StandardCharsets.UTF_8));

        when(appUserRepository.findById(1)).thenReturn(Optional.of(user));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO updated = userService.uploadProfilePicture(1, file);

        assertNotNull(updated.getProfilePicture());
        assertFalse(updated.getProfilePicture().isBlank());
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void changePasswordShouldValidateOldPasswordAndSendEmail() {
        AppUser user = appUser();
        user.setPassword("encoded-old");

        when(appUserRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Old12345", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("New12345")).thenReturn("encoded-new");

        userService.changePassword(1, "Old12345", "New12345");

        verify(appUserRepository).save(user);
        verify(mailService).sendSimpleEmail(user.getEmail(), "CareAssist password changed",
                "Your CareAssist password was changed successfully. If this was not you, please contact support immediately.");
    }

    private AppUser appUser() {
        AppUser user = new AppUser();
        user.setUserId(1);
        user.setUsername("patient");
        user.setEmail("patient@gmail.com");
        user.setPassword("Test1234");
        user.setRole("PATIENT");
        user.setPhoneNumber("9876543210");
        user.setActive(true);
        return user;
    }
}
