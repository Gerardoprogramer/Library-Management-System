package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.modal.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {

    Page<Wishlist> findByUserId(UUID userId, Pageable pageable);

    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);

    Wishlist findByUserIdAndBookId(UUID userId, UUID bookId);
}
