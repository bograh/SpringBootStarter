package com.example.springstarterproject.user.auth;

import com.example.springstarterproject.exceptions.BadRequestException;
import com.example.springstarterproject.exceptions.UnauthorizedException;
import com.example.springstarterproject.security.JwtService;
import com.example.springstarterproject.user.User;
import com.example.springstarterproject.user.UserRepository;
import com.example.springstarterproject.user.dtos.AuthResponse;
import com.example.springstarterproject.user.dtos.LoginRequest;
import com.example.springstarterproject.user.dtos.SignUpRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    private String getAuthHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new UnauthorizedException("Invalid authorization header");
    }

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

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUserEmail(email);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setMessage("signup successful");
        return authResponse;
    }

    public AuthResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().trim().toLowerCase();
        String token = getAccessToken(email, loginRequest.getPassword());
        String refreshToken = generateRefreshToken();

        redisTemplate.opsForValue().set(refreshToken, token, Duration.ofHours(24));

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUserEmail(email);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setMessage("login successful");
        return authResponse;
    }

    public void logout(HttpServletRequest request) {
        String refreshToken = getAuthHeader(request);
        if (!redisTemplate.hasKey(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        redisTemplate.delete(refreshToken);
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString()
                .replaceAll("-", "");
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
