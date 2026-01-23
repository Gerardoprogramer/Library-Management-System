package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.response.user.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse getCurrentUser();

    public List<UserResponse>  getAllUsers();
}
