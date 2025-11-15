package com.example.springstarterproject.user.auth;

import com.example.springstarterproject.exceptions.BadRequestException;
import com.example.springstarterproject.exceptions.UnauthorizedException;
import com.example.springstarterproject.security.JwtService;
import com.example.springstarterproject.user.User;
import com.example.springstarterproject.user.UserRepository;
import com.example.springstarterproject.user.dtos.*;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    public AuthResponse signup(SignUpRequest signUpRequest) {
        String email = signUpRequest.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("User with email exists already");
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        userRepository.save(newUser);

        String token = getAccessToken(email, signUpRequest.getPassword());
        String refreshToken = generateRefreshToken();

        redisTemplate.opsForValue().set(refreshToken, token, Duration.ofHours(24));

        AuthResponseDTO authResponseDTO = new AuthResponseDTO();
        authResponseDTO.setUserEmail(email);
        authResponseDTO.setRefreshToken(refreshToken);
        authResponseDTO.setMessage("signup successful");

        CookieHeaders cookieHeaders = setCookies(refreshToken, null);

        AuthResponse response = new AuthResponse();
        response.setAuthResponseDTO(authResponseDTO);
        response.setCookieHeaders(cookieHeaders);
        response.setToken(token);
        return response;
    }

    public AuthResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().trim().toLowerCase();
        String token = getAccessToken(email, loginRequest.getPassword());
        String refreshToken = generateRefreshToken();

        redisTemplate.opsForValue().set(refreshToken, token, Duration.ofHours(24));

        AuthResponseDTO authResponseDTO = new AuthResponseDTO();
        authResponseDTO.setUserEmail(email);
        authResponseDTO.setRefreshToken(refreshToken);
        authResponseDTO.setMessage("login successful");

        CookieHeaders cookieHeaders = setCookies(refreshToken, null);

        AuthResponse response = new AuthResponse();
        response.setAuthResponseDTO(authResponseDTO);
        response.setCookieHeaders(cookieHeaders);
        response.setToken(token);

        return response;
    }

    public void logout(HttpServletRequest request) {
        String refreshToken = jwtService.getSessionTokenFromRequest(request);
        if (!redisTemplate.hasKey(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        redisTemplate.delete(refreshToken);
    }

    public CookieHeaders setCookies(
            String sessionId, @Nullable String ipAddress
    ) {
        ResponseCookie sessionIdCookie = ResponseCookie.from("SESSION_ID", sessionId)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ofDays(7))
                .build();

        CookieHeaders cookieHeaders = new CookieHeaders();
        cookieHeaders.setSessionCookie(sessionIdCookie);
        return cookieHeaders;
    }

    private String generateRefreshToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).replaceAll("[_-]", "");
    }

    private String getAccessToken(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            return jwtService.generateToken(authentication);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid Username or Password");
        }
    }
}
