package com.pm.librarymanagementsystem.payload.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        String token,
        @NotBlank(message = "Password is required")
        String password
) {
}
