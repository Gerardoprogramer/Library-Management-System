package com.pm.librarymanagementsystem.controller;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.request.auth.ForgotPasswordRequest;
import com.pm.librarymanagementsystem.payload.dto.request.auth.LoginRequest;
import com.pm.librarymanagementsystem.payload.dto.request.auth.RegisterRequest;
import com.pm.librarymanagementsystem.payload.dto.request.auth.ResetPasswordRequest;
import com.pm.librarymanagementsystem.payload.dto.response.jwt.JwtResponse;
import com.pm.librarymanagementsystem.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Object>> signupHandler(
            @RequestBody @Valid RegisterRequest request,
            HttpServletResponse response
    ) {

        JwtResponse authResponse = authService.signup(request);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", authResponse.accessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofMinutes(15))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", authResponse.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario registrado correctamente", authResponse.user()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> loginHandler(
            @RequestBody @Valid LoginRequest loginRequest,
            HttpServletResponse response
    ) {

        JwtResponse authResponse = authService.login(loginRequest);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", authResponse.accessToken())
                .httpOnly(true)
                .secure(false) // ⚠ false en localhost
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofMinutes(15))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", authResponse.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(
                ApiResponse.success("Inicio de sesión exitoso", authResponse.user())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {

        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        ResponseCookie clearAccess = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie clearRefresh = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, clearAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());

        return ResponseEntity.ok(ApiResponse.success("Sesión cerrada correctamente"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Object>> refresh(
            @CookieValue(name = "refresh_token") String refreshToken,
            HttpServletResponse response
    ) {

        JwtResponse jwtResponse = authService.refresh(refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", jwtResponse.accessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofMinutes(15))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        return ResponseEntity.ok(
                ApiResponse.success("Access token renovado", jwtResponse.user())
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        authService.createPasswordResetToken(request.email());

        return ResponseEntity.ok(ApiResponse.success(
                "Se ha enviado un enlace de restablecimiento a tu correo electrónico."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request.token(), request.password());

        return ResponseEntity.ok(ApiResponse.success(
                "Restablecimiento de contraseña realizado correctamente."));
    }
}
