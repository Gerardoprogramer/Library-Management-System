package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.AuthProvider;
import com.pm.librarymanagementsystem.domain.UserRole;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.repository.UserRepository;
import com.pm.librarymanagementsystem.util.EmailNormalizer;
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

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args){
        initializeAdminUser();
    }

    private void initializeAdminUser() {

        String normalizedEmail = EmailNormalizer.normalize(adminEmail);

        if (userRepository.findByEmail(normalizedEmail).isEmpty()) {

            User user = User.builder()
                    .password(passwordEncoder.encode(adminPassword))
                    .email(normalizedEmail)
                    .fullName("Obsidian Admin")
                    .role(UserRole.ROLE_ADMIN)
                    .authProvider(AuthProvider.LOCAL)
                    .build();

            userRepository.save(user);
        }
    }
}
