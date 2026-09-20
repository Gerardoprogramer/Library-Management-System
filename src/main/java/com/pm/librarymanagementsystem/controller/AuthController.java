package com.pm.librarymanagementsystem.controller;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.request.auth.ForgotPasswordRequest;
import com.pm.librarymanagementsystem.payload.dto.request.auth.LoginRequest;
import com.pm.librarymanagementsystem.payload.dto.request.auth.RegisterRequest;
import com.pm.librarymanagementsystem.payload.dto.request.auth.ResetPasswordRequest;
import com.pm.librarymanagementsystem.payload.dto.response.jwt.JwtResponse;
import com.pm.librarymanagementsystem.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@SecurityRequirements
public class AuthController {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(15);
    private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(7);

    private final AuthService authService;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site}")
    private String cookieSameSite;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Object>> signup(
            @RequestBody @Valid RegisterRequest request,
            HttpServletResponse response
    ) {
        JwtResponse authResponse = authService.signup(request);

        setAuthCookies(response, authResponse);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Usuario registrado correctamente",
                                authResponse.user()
                        )
                );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response
    ) {
        JwtResponse authResponse = authService.login(request);

        setAuthCookies(response, authResponse);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Inicio de sesión exitoso",
                        authResponse.user()
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        clearAuthCookies(response);

        return ResponseEntity.ok(
                ApiResponse.success("Sesión cerrada correctamente")
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Object>> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE) String refreshToken,
            HttpServletResponse response
    ) {
        JwtResponse authResponse = authService.refresh(refreshToken);

        setAuthCookies(response, authResponse);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Sesión renovada correctamente",
                        authResponse.user()
                )
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request
    ) {
        authService.createPasswordResetToken(request.email());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Si existe una cuenta asociada a ese correo, se enviará un enlace de recuperación."
                )
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request
    ) {
        authService.resetPassword(
                request.token(),
                request.password()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Contraseña actualizada correctamente."
                )
        );
    }

    private void setAuthCookies(
            HttpServletResponse response,
            JwtResponse authResponse
    ) {
        addCookie(
                response,
                ACCESS_TOKEN_COOKIE,
                authResponse.accessToken(),
                ACCESS_TOKEN_DURATION
        );

        addCookie(
                response,
                REFRESH_TOKEN_COOKIE,
                authResponse.refreshToken(),
                REFRESH_TOKEN_DURATION
        );
    }

    private void clearAuthCookies(HttpServletResponse response) {
        addCookie(
                response,
                ACCESS_TOKEN_COOKIE,
                "",
                Duration.ZERO
        );

        addCookie(
                response,
                REFRESH_TOKEN_COOKIE,
                "",
                Duration.ZERO
        );
    }

    private void addCookie(
            HttpServletResponse response,
            String name,
            String value,
            Duration maxAge
    ) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite(cookieSameSite)
                .maxAge(maxAge)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }

    @GetMapping("/csrf")
    public ResponseEntity<CsrfToken> csrf(
            CsrfToken csrfToken
    ) {
        return ResponseEntity.ok(csrfToken);
    }
}