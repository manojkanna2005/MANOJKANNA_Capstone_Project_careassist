package com.hexaware.careassist.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.hexaware.careassist.dto.UserAccountUpdateRequest;
import com.hexaware.careassist.dto.UserDTO;

public interface IUserService {
    UserDTO createUser(UserDTO dto);
    UserDTO updateUser(Integer userId, UserDTO dto);
    UserDTO updateAccount(Integer userId, UserAccountUpdateRequest request);
    UserDTO getUserById(Integer userId);
    UserDTO getUserByEmail(String email);
    List<UserDTO> getAllUsers();
    List<UserDTO> getUsersByRole(String role);
    UserDTO activateUser(Integer userId);
    UserDTO deactivateUser(Integer userId);
    List<UserDTO> getAllUsersExceptAdmin();
    UserDTO uploadProfilePicture(Integer userId, MultipartFile file);
    UserDTO deleteProfilePicture(Integer userId);
    void changePassword(Integer userId, String currentPassword, String newPassword);
    String forgotPassword(String email);
    void deleteUser(Integer userId);
}
