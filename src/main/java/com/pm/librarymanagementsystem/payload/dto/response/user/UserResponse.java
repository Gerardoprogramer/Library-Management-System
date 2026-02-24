package com.pm.librarymanagementsystem.payload.dto.response.user;

import com.pm.librarymanagementsystem.domain.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse (
        UUID id,
        String email,
        String phone,
        String fullName,
        Boolean isAdmin,
        LocalDateTime lastLogin
){
}
