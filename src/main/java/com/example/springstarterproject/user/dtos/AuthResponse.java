package com.example.springstarterproject.user.dtos;

import lombok.Data;

@Data
public class AuthResponse {
    private AuthResponseDTO authResponseDTO;
    private CookieHeaders cookieHeaders;
    private String token;
}
