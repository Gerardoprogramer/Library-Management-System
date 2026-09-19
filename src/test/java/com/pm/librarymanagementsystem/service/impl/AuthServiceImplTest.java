package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.configurations.JwtProvider;
import com.pm.librarymanagementsystem.domain.UserRole;
import com.pm.librarymanagementsystem.exception.ConflictException;
import com.pm.librarymanagementsystem.exception.InvalidTokenException;
import com.pm.librarymanagementsystem.modal.PasswordResetToken;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.auth.LoginRequest;
import com.pm.librarymanagementsystem.payload.dto.request.auth.RegisterRequest;
import com.pm.librarymanagementsystem.payload.dto.response.jwt.JwtResponse;
import com.pm.librarymanagementsystem.repository.PasswordResetTokenRepository;
import com.pm.librarymanagementsystem.repository.RefreshTokenRepository;
import com.pm.librarymanagementsystem.repository.UserRepository;
import com.pm.librarymanagementsystem.service.EmailService;
import com.pm.librarymanagementsystem.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private CustomUserServiceImpl customUserServiceImpl;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();

        user.setId(UUID.randomUUID());
        user.setEmail("gera@gmail.com");
        user.setFullName("Gerardo Martínez");
        user.setRole(UserRole.ROLE_USER);
        user.setPassword("encoded-password");

        ReflectionTestUtils.setField(
                authService,
                "frontendUrl",
                "http://localhost:3000/reset-password?token="
        );
    }

    @Test
    void login_shouldNormalizeEmailAndReturnTokens() {

        LoginRequest request =
                new LoginRequest(
                        "  Gera@Gmail.Com  ",
                        "correct-password"
                );

        when(userRepository.findByEmail("gera@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "correct-password",
                "encoded-password"
        )).thenReturn(true);

        when(jwtProvider.generateAccessToken(user))
                .thenReturn("access-token");

        when(refreshTokenService.createRefreshToken(user))
                .thenReturn("refresh-token");

        JwtResponse response = authService.login(request);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());

        assertEquals(
                "gera@gmail.com",
                response.user().email()
        );

        assertNotNull(user.getLastLogin());

        verify(userRepository)
                .findByEmail("gera@gmail.com");

        verify(passwordEncoder)
                .matches(
                        "correct-password",
                        "encoded-password"
                );

        verify(jwtProvider)
                .generateAccessToken(user);

        verify(refreshTokenService)
                .createRefreshToken(user);
    }

    @Test
    void login_shouldRejectInvalidPassword() {

        LoginRequest request =
                new LoginRequest(
                        "gera@gmail.com",
                        "wrong-password"
                );

        when(userRepository.findByEmail("gera@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "encoded-password"
        )).thenReturn(false);

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        verify(jwtProvider, never())
                .generateAccessToken(any());

        verify(refreshTokenService, never())
                .createRefreshToken(any());
    }

    @Test
    void login_shouldRejectUnknownEmailWithGenericError() {

        LoginRequest request =
                new LoginRequest(
                        "unknown@gmail.com",
                        "password"
                );

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        BadCredentialsException exception =
                assertThrows(
                        BadCredentialsException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Credenciales inválidas",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtProvider, never())
                .generateAccessToken(any());

        verify(refreshTokenService, never())
                .createRefreshToken(any());
    }

    @Test
    void signup_shouldNormalizeEmailEncodePasswordAndReturnTokens() {

        RegisterRequest request =
                new RegisterRequest(
                        "  Gera@Gmail.Com  ",
                        "password123",
                        "Gerardo Martínez"
                );

        when(userRepository.findByEmail("gera@gmail.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {

                    User savedUser = invocation.getArgument(0);

                    savedUser.setId(UUID.randomUUID());

                    return savedUser;
                });

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername("gera@gmail.com")
                        .password("encoded-password")
                        .authorities("ROLE_USER")
                        .build();

        when(customUserServiceImpl.loadUserByUsername("gera@gmail.com"))
                .thenReturn(userDetails);

        when(jwtProvider.generateAccessToken(any(User.class)))
                .thenReturn("access-token");

        when(refreshTokenService.createRefreshToken(any(User.class)))
                .thenReturn("refresh-token");

        JwtResponse response =
                authService.signup(request);

        assertEquals(
                "access-token",
                response.accessToken()
        );

        assertEquals(
                "refresh-token",
                response.refreshToken()
        );

        assertEquals(
                "gera@gmail.com",
                response.user().email()
        );

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(userCaptor.capture());

        User savedUser =
                userCaptor.getValue();

        assertEquals(
                "gera@gmail.com",
                savedUser.getEmail()
        );

        assertEquals(
                "encoded-password",
                savedUser.getPassword()
        );

        assertEquals(
                "Gerardo Martínez",
                savedUser.getFullName()
        );

        assertEquals(
                UserRole.ROLE_USER,
                savedUser.getRole()
        );

        verify(userRepository)
                .findByEmail("gera@gmail.com");

        verify(passwordEncoder)
                .encode("password123");
    }

    @Test
    void signup_shouldRejectCaseInsensitiveDuplicateEmail() {

        RegisterRequest request =
                new RegisterRequest(
                        "  GERA@GMAIL.COM  ",
                        "password123",
                        "Otro usuario"
                );

        when(userRepository.findByEmail("gera@gmail.com"))
                .thenReturn(Optional.of(user));

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> authService.signup(request)
                );

        assertEquals(
                "El correo ya está registrado",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail("gera@gmail.com");

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));

        verify(jwtProvider, never())
                .generateAccessToken(any());

        verify(refreshTokenService, never())
                .createRefreshToken(any());
    }

    @Test
    void createPasswordResetToken_shouldNormalizeEmailCreateTokenAndSendEmail() {

        when(userRepository.findByEmail("gera@gmail.com"))
                .thenReturn(Optional.of(user));

        authService.createPasswordResetToken(
                "  Gera@Gmail.Com  "
        );

        verify(userRepository)
                .findByEmail("gera@gmail.com");

        verify(passwordResetTokenRepository)
                .deleteByUser(user);

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(
                        PasswordResetToken.class
                );

        verify(passwordResetTokenRepository)
                .save(tokenCaptor.capture());

        PasswordResetToken savedToken =
                tokenCaptor.getValue();

        assertNotNull(savedToken.getToken());
        assertFalse(savedToken.getToken().isBlank());

        assertEquals(
                user,
                savedToken.getUser()
        );

        assertTrue(
                savedToken.getExpiryDate()
                        .isAfter(LocalDateTime.now())
        );

        verify(emailService)
                .sendPasswordResetEmail(
                        eq("gera@gmail.com"),
                        eq("Restablecimiento de contraseña"),
                        contains(savedToken.getToken())
                );
    }

    @Test
    void createPasswordResetToken_shouldSilentlyIgnoreUnknownEmail() {

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(
                () -> authService.createPasswordResetToken(
                        "  UNKNOWN@GMAIL.COM  "
                )
        );

        verify(userRepository)
                .findByEmail("unknown@gmail.com");

        verify(passwordResetTokenRepository, never())
                .save(any());

        verify(passwordResetTokenRepository, never())
                .deleteByUser(any());

        verify(emailService, never())
                .sendPasswordResetEmail(
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void resetPassword_shouldUpdatePasswordAndRevokeAllSessions() {

        String token = "valid-reset-token";

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .token(token)
                        .user(user)
                        .expiryDate(
                                LocalDateTime.now()
                                        .plusMinutes(10)
                        )
                        .build();

        when(passwordResetTokenRepository.findByToken(token))
                .thenReturn(Optional.of(resetToken));

        when(passwordEncoder.encode("newPassword123"))
                .thenReturn("new-encoded-password");

        authService.resetPassword(
                token,
                "newPassword123"
        );

        assertEquals(
                "new-encoded-password",
                user.getPassword()
        );

        verify(passwordEncoder)
                .encode("newPassword123");

        verify(refreshTokenRepository)
                .deleteByUser(user);

        verify(passwordResetTokenRepository)
                .deleteByUser(user);
    }

    @Test
    void resetPassword_shouldRejectInvalidToken() {

        String token = "invalid-token";

        when(passwordResetTokenRepository.findByToken(token))
                .thenReturn(Optional.empty());

        InvalidTokenException exception =
                assertThrows(
                        InvalidTokenException.class,
                        () -> authService.resetPassword(
                                token,
                                "newPassword123"
                        )
                );

        assertEquals(
                "Token inválido o expirado",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(refreshTokenRepository, never())
                .deleteByUser(any());

        verify(passwordResetTokenRepository, never())
                .deleteByUser(any());
    }

    @Test
    void resetPassword_shouldRejectExpiredToken() {

        String token = "expired-token";

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .token(token)
                        .user(user)
                        .expiryDate(
                                LocalDateTime.now()
                                        .minusMinutes(1)
                        )
                        .build();

        when(passwordResetTokenRepository.findByToken(token))
                .thenReturn(Optional.of(resetToken));

        InvalidTokenException exception =
                assertThrows(
                        InvalidTokenException.class,
                        () -> authService.resetPassword(
                                token,
                                "newPassword123"
                        )
                );

        assertEquals(
                "Token inválido o expirado",
                exception.getMessage()
        );

        verify(passwordResetTokenRepository)
                .delete(resetToken);

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(refreshTokenRepository, never())
                .deleteByUser(any());
    }
}