package com.pm.librarymanagementsystem.mapper;

import com.pm.librarymanagementsystem.domain.UserRole;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.response.user.UserResponse;
import com.pm.librarymanagementsystem.payload.dto.request.auth.RegisterRequest;

import java.time.LocalDateTime;

public class UserMapper {

    private UserMapper(){}

    /* =======================
       DTO → ENTITY
       ======================= */
    public static User toRegister(
            RegisterRequest request,
            String encodedPassword,
            String normalizedEmail
    ) {
        User user = new User();

        user.setEmail(normalizedEmail);
        user.setPassword(encodedPassword);
        user.setFullName(request.fullName());
        user.setLastLogin(LocalDateTime.now());
        user.setRole(UserRole.ROLE_USER);

        return user;
    }


        /* =======================
       ENTITY → DTO
       ======================= */

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getFullName(),
                user.getRole() == UserRole.ROLE_ADMIN,
                user.getLastLogin()
        );
    }
}
