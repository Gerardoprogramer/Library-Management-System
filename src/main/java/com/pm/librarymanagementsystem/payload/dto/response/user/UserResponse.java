package com.pm.librarymanagementsystem.payload.dto.response.user;

import com.pm.librarymanagementsystem.domain.UserRole;

import java.time.LocalDateTime;

public record UserResponse (
Long id,
String email,
String phone,
String fullName,
UserRole role,
LocalDateTime lastLogin
){
}
