package com.pm.librarymanagementsystem.mapper;

import com.pm.librarymanagementsystem.domain.UserRole;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.response.user.UserResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.auth.RegisterRequest;

import java.time.LocalDateTime;

public class UserMapper {

    private UserMapper(){}

    /* =======================
       DTO → ENTITY
       ======================= */
    public static User toRegister(RegisterRequest request, String passwordEncoder){
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder);
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
                user.getRole(),
                user.getLastLogin()
        );
    }
}
