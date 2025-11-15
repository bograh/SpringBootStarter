package com.example.springstarterproject.security;

import com.example.springstarterproject.exceptions.NotFoundException;
import com.example.springstarterproject.exceptions.UnauthorizedException;
import com.example.springstarterproject.user.User;
import com.example.springstarterproject.user.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${security.jwt.secret}")
    private String JWT_SECRET;

    @Value("${security.jwt.password-reset-token-expiration:900000}")
    private long PASSWORD_RESET_TOKEN_EXPIRATION;

    @Value("${security.jwt.token-expiration:3600000}")
    private long TOKEN_EXPIRATION;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
    }

    public String getTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String refreshToken = bearerToken.substring(7);
            return redisTemplate.opsForValue().get(refreshToken);
        }
        return null;
    }

    public String generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("User not found")
        );

        Date now = new Date();
        Date expiration = new Date(now.getTime() + PASSWORD_RESET_TOKEN_EXPIRATION);

        return Jwts.builder()
                .subject("username")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSecretKey())
                .compact();
    }

    public String generateToken(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException("User not found")
        );
        String userId = user.getId().toString();
        Date now = new Date();
        Date expiration = new Date(now.getTime() + TOKEN_EXPIRATION);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSecretKey())
                .claim("email", email)
                .compact();
    }

    public String getUserEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("email", String.class);
    }

    // validate Jwt token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw new UnauthorizedException("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            throw new UnauthorizedException("JWT token is expired");
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            throw new UnauthorizedException("JWT token is unsupported");
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            throw new UnauthorizedException("JWT claims string is empty");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
