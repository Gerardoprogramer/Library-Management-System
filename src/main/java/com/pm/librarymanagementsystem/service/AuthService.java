package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.response.auth.AuthResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.auth.LoginRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.auth.RegisterRequest;

public interface AuthService {

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse signup(RegisterRequest request);

    AuthResponse logout();

    void createPasswordResetToken(String email);

    void resetPassword(String token, String newPassword);

}
