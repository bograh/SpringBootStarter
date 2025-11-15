package com.example.springstarterproject.user.dtos;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String userEmail;
    private String message;
}
