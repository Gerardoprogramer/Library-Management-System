package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.UserMapper;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.response.user.UserResponse;
import com.pm.librarymanagementsystem.repository.UserRepository;
import com.pm.librarymanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserResponse getCurrentUser() {

        User user = userRepository.findById(getCurrentUserId()).orElseThrow(
                ()-> new NotFoundException("Usuario no encontrado"));

        return UserMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public User getCurrentUserEntity() {

        return userRepository.findById(getCurrentUserId()).orElseThrow(
                ()-> new NotFoundException("Usuario no encontrado"));
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("No se encontro el Usuario"));
    }

    private UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

}
