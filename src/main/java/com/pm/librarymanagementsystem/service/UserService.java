package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.response.user.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse getCurrentUser();

    List<UserResponse>  getAllUsers();

    User getCurrentUserEntity();

    User findById(UUID id);
}
