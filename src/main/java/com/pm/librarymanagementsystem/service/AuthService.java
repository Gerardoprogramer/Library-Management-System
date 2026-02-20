package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.request.auth.LoginRequest;
import com.pm.librarymanagementsystem.payload.dto.request.auth.RegisterRequest;
import com.pm.librarymanagementsystem.payload.dto.response.jwt.JwtResponse;

public interface AuthService {

    JwtResponse login(LoginRequest loginRequest);

    JwtResponse signup(RegisterRequest request);

    void logout(String refreshToken);

    void createPasswordResetToken(String email);

    void resetPassword(String token, String newPassword);

    JwtResponse refresh(String refreshTokenRequest);

}
