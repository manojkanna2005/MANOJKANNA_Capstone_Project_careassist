package com.hexaware.careassist.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tokenType;
    private int userId;
    private String username;
    private String email;
    private String role;
    private String profilePicture;
}