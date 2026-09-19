package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.modal.RefreshToken;
import com.pm.librarymanagementsystem.modal.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
