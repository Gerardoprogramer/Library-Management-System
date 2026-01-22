package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.AuthProvider;
import com.pm.librarymanagementsystem.domain.UserRole;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializationComponents implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Override
    public void run(String... args){
        initializeAdminUser();
    }

    private void initializeAdminUser(){

        if(userRepository.findByEmail(adminEmail).isEmpty()){

            User user = User.builder()
                    .password(passwordEncoder.encode(adminPassword))
                    .email(adminEmail)
                    .fullName("Obsidian Admin")
                    .role(UserRole.ROLE_ADMIN)
                    .authProvider(AuthProvider.LOCAL)
                    .build();

            User admin = userRepository.save(user);
        }
    }
}
