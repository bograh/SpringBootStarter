package com.example.springstarterproject.user.dtos;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String refreshToken;
    private String userEmail;
    private String message;
}
