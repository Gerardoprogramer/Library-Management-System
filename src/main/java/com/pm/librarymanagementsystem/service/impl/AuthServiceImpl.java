package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.configurations.JwtProvider;
import com.pm.librarymanagementsystem.exception.ConflictException;
import com.pm.librarymanagementsystem.exception.InvalidTokenException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.UserMapper;
import com.pm.librarymanagementsystem.modal.PasswordResetToken;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.response.auth.AuthResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.auth.LoginRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.auth.RegisterRequest;
import com.pm.librarymanagementsystem.repository.PasswordResetTokenRepository;
import com.pm.librarymanagementsystem.repository.UserRepository;
import com.pm.librarymanagementsystem.service.AuthService;
import com.pm.librarymanagementsystem.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomUserServiceImpl customUserServiceImpl;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend.reset-password-url}")
    private String frontendUrl;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticate(request.email(), request.password());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return new AuthResponse(
                token,
                "Inicio de sesión exitoso",
                "Bienvenid@ " + user.getFullName(),
                UserMapper.toResponse(user)
        );
    }

    private Authentication authenticate(String email, String password) {
        UserDetails userDetails = customUserServiceImpl.loadUserByUsername(email);

        if (userDetails == null ||
                !passwordEncoder.matches(password, userDetails.getPassword())) {

            throw new BadCredentialsException("Credenciales inválidas");
        }
        return new UsernamePasswordAuthenticationToken(email, null, userDetails.getAuthorities());
    }

    @Transactional
    @Override
    public AuthResponse signup(RegisterRequest request) {

        userRepository.findByEmail(request.email())
                .ifPresent(usr -> {
            throw new ConflictException("El correo ya está registrado: "+ request.email());
        });

        User user = UserMapper.toRegister(request, passwordEncoder.encode(request.password()));

        user = userRepository.save(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), user.getPassword());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateToken(authentication);

        return new AuthResponse(
                jwt,
                "registro exitoso",
                "Bienvenid@ " + user.getFullName(),
                UserMapper.toResponse(user)
        );
    }

    @Override
    public AuthResponse logout() {
        return null;
    }

    @Transactional
    public void createPasswordResetToken(String email) {

        User user = userRepository.findByEmail(email).orElseThrow( () ->
                new NotFoundException("No se ha encontrado el usuario con el correo electrónico proporcionado.")
        );

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();
        passwordResetTokenRepository.save(resetToken);
        String reserLink = frontendUrl+token;
        String subject = "Solicitud de restablecimiento de contraseña";
        String body = "Solicitaste restablecer tu contraseña. Utiliza este enlace (válido durante 5 minutos): " + reserLink;

        emailService.sendEmail(user.getEmail(), subject, body);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(()-> new InvalidTokenException("Token no valido!"));

        if(resetToken.isExpired()){
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidTokenException("Token caducado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }
}
