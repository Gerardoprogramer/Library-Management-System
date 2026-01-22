package com.pm.librarymanagementsystem.payload.dto.response.auth;

import com.pm.librarymanagementsystem.payload.dto.response.user.UserResponse;


public record AuthResponse (
        String accessToken,
        String message,
        String title,
        UserResponse user
){}
