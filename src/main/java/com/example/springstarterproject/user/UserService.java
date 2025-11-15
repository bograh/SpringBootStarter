package com.example.springstarterproject.user;

import com.example.springstarterproject.exceptions.UnauthorizedException;
import com.example.springstarterproject.security.JwtService;
import com.example.springstarterproject.user.dtos.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    private String getTokenFromRequest(HttpServletRequest request) {
        return jwtService.getTokenFromHeader(request);
    }

    public UserResponse getProfile(HttpServletRequest request) {
        String email = jwtService.getUserEmailFromToken(getTokenFromRequest(request));
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException("User email not found")
        );
        UserResponse userResponse = new UserResponse();
        userResponse.setEmail(user.getEmail());
        userResponse.setRole(user.getRole().name());
        return userResponse;
    }

}
