package com.pm.librarymanagementsystem.configurations;

import com.pm.librarymanagementsystem.modal.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class JwtProvider {

    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(15);

    private final SecretKey key;

    public JwtProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("authorities", user.getRole().toString())
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + ACCESS_TOKEN_DURATION.toMillis()
                        )
                )
                .signWith(key)
                .compact();
    }

    public String extractUserId(String token) {
        return parseClaims(token)
                .get("userId", String.class);
    }

    public List<GrantedAuthority> extractAuthorities(String token) {
        String authorities = parseClaims(token)
                .get("authorities", String.class);

        if (authorities == null || authorities.isBlank()) {
            return Collections.emptyList();
        }

        return AuthorityUtils
                .commaSeparatedStringToAuthorityList(authorities);
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}