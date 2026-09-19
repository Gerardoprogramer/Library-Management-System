package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.exception.InvalidTokenException;
import com.pm.librarymanagementsystem.modal.RefreshToken;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenServiceImpl refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        refreshTokenService =
                new RefreshTokenServiceImpl(
                        refreshTokenRepository
                );

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("gera@gmail.com");
    }

    @Test
    void createRefreshToken_shouldStoreHashInsteadOfRawToken() {

        String rawToken =
                refreshTokenService.createRefreshToken(user);

        assertNotNull(rawToken);
        assertFalse(rawToken.isBlank());

        ArgumentCaptor<RefreshToken> tokenCaptor =
                ArgumentCaptor.forClass(
                        RefreshToken.class
                );

        verify(refreshTokenRepository)
                .save(tokenCaptor.capture());

        RefreshToken savedToken =
                tokenCaptor.getValue();

        assertNotNull(savedToken.getToken());

        assertNotEquals(
                rawToken,
                savedToken.getToken()
        );

        assertEquals(
                sha256(rawToken),
                savedToken.getToken()
        );

        assertEquals(
                user,
                savedToken.getUser()
        );

        assertTrue(
                savedToken.getExpiryDate()
                        .isAfter(
                                LocalDateTime.now()
                                        .plusDays(6)
                        )
        );
    }

    @Test
    void validateAndConsume_shouldReturnUserAndDeleteToken() {

        String rawToken =
                "valid-refresh-token";

        String hashedToken =
                sha256(rawToken);

        RefreshToken refreshToken =
                RefreshToken.builder()
                        .token(hashedToken)
                        .user(user)
                        .expiryDate(
                                LocalDateTime.now()
                                        .plusDays(1)
                        )
                        .build();

        when(refreshTokenRepository.findByToken(
                hashedToken
        )).thenReturn(
                Optional.of(refreshToken)
        );

        User result =
                refreshTokenService.validateAndConsume(
                        rawToken
                );

        assertEquals(user, result);

        verify(refreshTokenRepository)
                .findByToken(hashedToken);

        verify(refreshTokenRepository)
                .delete(refreshToken);
    }

    @Test
    void validateAndConsume_shouldRejectTokenReuse() {

        String rawToken =
                "single-use-refresh-token";

        String hashedToken =
                sha256(rawToken);

        RefreshToken refreshToken =
                RefreshToken.builder()
                        .token(hashedToken)
                        .user(user)
                        .expiryDate(
                                LocalDateTime.now()
                                        .plusDays(1)
                        )
                        .build();

        when(refreshTokenRepository.findByToken(
                hashedToken
        )).thenReturn(
                Optional.of(refreshToken),
                Optional.empty()
        );

        User result =
                refreshTokenService.validateAndConsume(
                        rawToken
                );

        assertEquals(user, result);

        InvalidTokenException exception =
                assertThrows(
                        InvalidTokenException.class,
                        () ->
                                refreshTokenService
                                        .validateAndConsume(
                                                rawToken
                                        )
                );

        assertEquals(
                "Refresh token inválido o expirado",
                exception.getMessage()
        );

        verify(
                refreshTokenRepository,
                times(2)
        ).findByToken(hashedToken);

        verify(
                refreshTokenRepository,
                times(1)
        ).delete(refreshToken);
    }

    private String sha256(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}