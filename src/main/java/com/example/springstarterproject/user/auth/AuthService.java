package com.example.springstarterproject.user.auth;

import com.example.springstarterproject.exceptions.BadRequestException;
import com.example.springstarterproject.security.JwtService;
import com.example.springstarterproject.user.User;
import com.example.springstarterproject.user.UserRepository;
import com.example.springstarterproject.user.dtos.AuthResponse;
import com.example.springstarterproject.user.dtos.LoginRequest;
import com.example.springstarterproject.user.dtos.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse signup(SignUpRequest signUpRequest) {
        String email = signUpRequest.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("User with email exists already");
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        userRepository.save(newUser);

        return authenticate(email, signUpRequest.getPassword());
    }

    public AuthResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().trim().toLowerCase();
        return authenticate(email, loginRequest.getPassword());
    }

    private AuthResponse authenticate(String email, String password) {
        AuthResponse authResponse = new AuthResponse();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtService.generateToken(authentication);
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new BadRequestException("User not found with email: " + email)
            );
            authResponse.setMessage("success");
            authResponse.setAccessToken(token);
            authResponse.setUserEmail(user.getEmail());

            return authResponse;

        } catch (Exception ex) {
            throw new BadRequestException("Invalid Username or Password");
        }
    }

}
