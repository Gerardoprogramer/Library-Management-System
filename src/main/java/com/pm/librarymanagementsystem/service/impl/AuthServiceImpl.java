package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.configurations.JwtProvider;
import com.pm.librarymanagementsystem.exception.ConflictException;
import com.pm.librarymanagementsystem.exception.InvalidTokenException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.UserMapper;
import com.pm.librarymanagementsystem.modal.PasswordResetToken;
import com.pm.librarymanagementsystem.modal.RefreshToken;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.auth.LoginRequest;
import com.pm.librarymanagementsystem.payload.dto.request.auth.RegisterRequest;
import com.pm.librarymanagementsystem.payload.dto.response.jwt.JwtResponse;
import com.pm.librarymanagementsystem.repository.PasswordResetTokenRepository;
import com.pm.librarymanagementsystem.repository.RefreshTokenRepository;
import com.pm.librarymanagementsystem.repository.UserRepository;
import com.pm.librarymanagementsystem.service.AuthService;
import com.pm.librarymanagementsystem.service.EmailService;
import com.pm.librarymanagementsystem.service.RefreshTokenService;
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
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.frontend.reset-password-url}")
    private String frontendUrl;

    @Override
    @Transactional
    public JwtResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        String accessToken = jwtProvider.generateAccessToken(user);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return new JwtResponse(
                accessToken,
                refreshToken.getToken(),
                UserMapper.toResponse(user)
        );
    }

    @Override
    @Transactional
    public JwtResponse signup(RegisterRequest request) {

        userRepository.findByEmail(request.email())
                .ifPresent(usr -> {
                    throw new ConflictException("El correo ya está registrado");
                });

        User user = UserMapper.toRegister(request, passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        UserDetails userDetails = customUserServiceImpl.loadUserByUsername(user.getEmail());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(),
                null,
                userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtProvider.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new JwtResponse(
                accessToken,
                refreshToken.getToken(),
                UserMapper.toResponse(user)
        );
    }


    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.deleteByToken(refreshToken);
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
        String subject = "Restablecimiento de contraseña";
        String body = "Usa este enlace (válido por 15 minutos): " + reserLink;

        emailService.sendPasswordResetEmail(user.getEmail(), subject, body);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(()-> new InvalidTokenException("Token inválido o expirado"));

        if(resetToken.isExpired()){
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidTokenException("Token inválido o expirado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }

    @Override
    @Transactional
    public JwtResponse refresh(String refreshTokenRequest) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenRequest)
                .orElseThrow(() -> new RuntimeException("Refresh token no encontrado"));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtProvider.generateAccessToken(user);

        return new JwtResponse(
                newAccessToken,
                refreshToken.getToken(),
                UserMapper.toResponse(refreshToken.getUser())
        );
    }
}
