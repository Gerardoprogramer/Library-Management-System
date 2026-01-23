package com.pm.librarymanagementsystem.controller;

import com.pm.librarymanagementsystem.domain.UserRole;
import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.response.user.UserResponse;
import com.pm.librarymanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping(("/me"))
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(){
        UserResponse user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Usuario autenticado", user));
    }

}
