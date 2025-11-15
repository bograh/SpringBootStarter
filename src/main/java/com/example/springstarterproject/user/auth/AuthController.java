package com.example.springstarterproject.user.auth;

import com.example.springstarterproject.user.dtos.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDTO> signup(@Valid @RequestBody SignUpRequest signUpRequest) {
        AuthResponse authResponse = authService.signup(signUpRequest);
        AuthResponseDTO response = authResponse.getAuthResponseDTO();
        CookieHeaders cookieHeaders = authResponse.getCookieHeaders();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieHeaders.getSessionCookie().toString())
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.SET_COOKIE)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        AuthResponseDTO response = authResponse.getAuthResponseDTO();
        CookieHeaders cookieHeaders = authResponse.getCookieHeaders();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieHeaders.getSessionCookie().toString())
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.SET_COOKIE)
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
