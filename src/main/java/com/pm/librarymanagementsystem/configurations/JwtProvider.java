package com.pm.librarymanagementsystem.configurations;

import com.pm.librarymanagementsystem.modal.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtProvider {

    private final SecretKey key;

    public JwtProvider(@Value("${jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // ACCESS TOKEN (15 min)
    public String generateAccessToken(User user) {

        String authorities = user.getRole().toString();

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("authorities", authorities)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))
                .signWith(key)
                .compact();
    }

    // REFRESH TOKEN (7 días)
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7))
                .signWith(key)
                .compact();
    }

    // Extraer email (subject)
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // Extraer authorities
    public List<GrantedAuthority> extractAuthorities(String token) {
        String authorities = parseClaims(token).get("authorities", String.class);

        if (authorities == null || authorities.isBlank()) {
            return Collections.emptyList();
        }

        return AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
    }

    // Validar token
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
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

    public String extractUserId(String token) {
        return parseClaims(token).get("userId", String.class);
    }
}
