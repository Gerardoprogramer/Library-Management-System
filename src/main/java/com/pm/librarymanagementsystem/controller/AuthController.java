package com.pm.librarymanagementsystem.controller;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.request.auth.ForgotPasswordRequest;
import com.pm.librarymanagementsystem.payload.dto.request.auth.LoginRequest;
import com.pm.librarymanagementsystem.payload.dto.request.auth.RegisterRequest;
import com.pm.librarymanagementsystem.payload.dto.request.auth.ResetPasswordRequest;
import com.pm.librarymanagementsystem.payload.dto.response.jwt.JwtResponse;
import com.pm.librarymanagementsystem.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<JwtResponse>> signupHandler(
            @RequestBody @Valid RegisterRequest request) {

        JwtResponse authResponse = authService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario registrado correctamente", authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> loginHandler(
            @RequestBody @Valid LoginRequest loginRequest) {

        JwtResponse authResponse = authService.login(loginRequest);

        return ResponseEntity.ok(ApiResponse.success("Inicio de sesión exitoso", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");
        authService.logout(refreshToken);

        return ResponseEntity.ok(ApiResponse.success("Sesión cerrada correctamente"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(
            @RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");
        JwtResponse response = authService.refresh(refreshToken);

        return ResponseEntity.ok(ApiResponse.success("Access token renovado", response));
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
