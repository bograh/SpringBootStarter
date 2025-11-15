package com.example.springstarterproject.user.dtos;

import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
public class CookieHeaders {
    private ResponseCookie sessionCookie;
    private ResponseCookie ipAddressCookie;
}