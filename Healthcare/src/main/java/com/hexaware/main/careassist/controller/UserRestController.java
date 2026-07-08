package com.hexaware.main.careassist.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hexaware.main.careassist.dto.ChangePasswordRequest;
import com.hexaware.main.careassist.dto.UserAccountUpdateRequest;
import com.hexaware.main.careassist.dto.UserDTO;
import com.hexaware.main.careassist.service.IUserService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    @Autowired
    private IUserService userService;

    @PostMapping("/add")
    public UserDTO createUser(@Valid @RequestBody UserDTO dto) {
        return userService.createUser(dto);
    }

    @PutMapping("/update/{userId}")
    public UserDTO updateUser(@PathVariable Integer userId, @Valid @RequestBody UserDTO dto) {
        return userService.updateUser(userId, dto);
    }

    @GetMapping("/{userId}/account")
    public UserDTO getAccount(@PathVariable Integer userId) {
        return userService.getUserById(userId);
    }

    @PatchMapping("/{userId}/account")
    public UserDTO updateAccount(@PathVariable Integer userId, @Valid @RequestBody UserAccountUpdateRequest request) {
        return userService.updateAccount(userId, request);
    }

    @PostMapping(value = "/{userId}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDTO uploadProfilePicture(@PathVariable Integer userId, @RequestParam("file") MultipartFile file) {
        return userService.uploadProfilePicture(userId, file);
    }

    @DeleteMapping("/{userId}/profile-picture")
    public UserDTO deleteProfilePicture(@PathVariable Integer userId) {
        return userService.deleteProfilePicture(userId);
    }

    @PatchMapping("/{userId}/change-password")
    public String changePassword(@PathVariable Integer userId, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return "Password changed successfully";
    }

    @GetMapping("/{userId}")
    public UserDTO getUserById(@PathVariable Integer userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/email/{email}")
    public UserDTO getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @GetMapping("/all")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/role/{role}")
    public List<UserDTO> getUsersByRole(@PathVariable String role) {
        return userService.getUsersByRole(role);
    }

    @GetMapping("/except-admin")
    public List<UserDTO> getAllUsersExceptAdmin() {
        return userService.getAllUsersExceptAdmin();
    }

    @PatchMapping("/activate/{userId}")
    public UserDTO activateUser(@PathVariable Integer userId) {
        return userService.activateUser(userId);
    }

    @PatchMapping("/deactivate/{userId}")
    public UserDTO deactivateUser(@PathVariable Integer userId) {
        return userService.deactivateUser(userId);
    }

    @DeleteMapping("/delete/{userId}")
    public String deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return "User deleted successfully";
    }
}
