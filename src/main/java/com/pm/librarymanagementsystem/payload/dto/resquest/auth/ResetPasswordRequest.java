package com.pm.librarymanagementsystem.payload.dto.resquest.auth;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        String token,
        @NotBlank(message = "Password is required")
        String password
) {
}
