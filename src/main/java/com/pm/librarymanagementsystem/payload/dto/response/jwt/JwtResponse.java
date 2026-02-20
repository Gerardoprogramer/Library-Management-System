package com.pm.librarymanagementsystem.payload.dto.response.jwt;

import com.pm.librarymanagementsystem.payload.dto.response.user.UserResponse;

public record JwtResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}
