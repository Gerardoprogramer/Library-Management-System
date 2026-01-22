package com.pm.librarymanagementsystem.controller;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.response.auth.AuthResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.auth.ForgotPasswordRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.auth.LoginRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.auth.RegisterRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.auth.ResetPasswordRequest;
import com.pm.librarymanagementsystem.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signupHandler(
            @RequestBody @Valid RegisterRequest request){
        AuthResponse authResponse = authService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario registrado correctamente", authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginHandler(
            @RequestBody @Valid LoginRequest loginRequest
            ){
        AuthResponse authResponse = authService.login(loginRequest);

        return ResponseEntity.ok(ApiResponse.success("Inicio de seccion exitoso",authResponse));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
       @RequestBody ForgotPasswordRequest request
    ){
        authService.createPasswordResetToken(request.email());

        return ResponseEntity.ok(ApiResponse.success(
                "Se ha enviado un enlace de restablecimiento a tu correo electrónico."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody ResetPasswordRequest request
    ){
        authService.resetPassword(request.token(), request.password());

        return ResponseEntity.ok(ApiResponse.success(
                "Restablecimiento de contraseña realizado correctamente."));
    }
}
