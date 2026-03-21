package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.modal.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {

    @Query("SELECT w FROM Wishlist w JOIN FETCH w.book WHERE w.user.id = :userId")
    Page<Wishlist> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);

    Wishlist findByUserIdAndBookId(UUID userId, UUID bookId);

    @Query("""
    SELECT COUNT(w.id) > 0
    FROM Wishlist w
    WHERE w.book.id = :bookId
    AND w.user.id = :userId
""")
    boolean isInWishlist(UUID bookId, UUID userId);
}
