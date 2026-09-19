package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.exception.InvalidTokenException;
import com.pm.librarymanagementsystem.modal.RefreshToken;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.repository.RefreshTokenRepository;
import com.pm.librarymanagementsystem.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final int TOKEN_BYTES = 32;
    private static final int TOKEN_EXPIRATION_DAYS = 7;

    private final RefreshTokenRepository refreshTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = generateSecureToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(hashToken(rawToken))
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(TOKEN_EXPIRATION_DAYS))
                .build();

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Override
    @Transactional
    public User validateAndConsume(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(hashToken(rawToken))
                .orElseThrow(this::invalidToken);

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw invalidToken();
        }

        User user = refreshToken.getUser();

        refreshTokenRepository.delete(refreshToken);

        return user;
    }

    @Override
    @Transactional
    public void deleteByToken(String rawToken) {
        refreshTokenRepository
                .findByToken(hashToken(rawToken))
                .ifPresent(refreshTokenRepository::delete);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[TOKEN_BYTES];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 no está disponible",
                    e
            );
        }
    }

    private InvalidTokenException invalidToken() {
        return new InvalidTokenException(
                "Refresh token inválido o expirado"
        );
    }
}
