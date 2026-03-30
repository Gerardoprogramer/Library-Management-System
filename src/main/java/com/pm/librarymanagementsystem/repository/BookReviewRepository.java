package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.BookReview;
import com.pm.librarymanagementsystem.payload.dto.response.bookReview.BookReviewResponse;
import com.pm.librarymanagementsystem.service.RatingStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BookReviewRepository extends JpaRepository<BookReview, UUID> {

    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);

    @Query("""
       SELECT new com.pm.librarymanagementsystem.payload.dto.response.bookReview.BookReviewResponse(
           r.id,
           u.id,
           u.fullName,
           r.rating,
           r.reviewText,
           r.title,
           r.createdAt,
           r.updatedAt,
           b.id,
           b.title,
           b.coverImageUrl
       )
       FROM BookReview r
       JOIN r.user u
       JOIN r.book b
       WHERE b.id = :bookId
       ORDER BY r.createdAt DESC
""")
    Page<BookReviewResponse> findReviewsByBookId(
            @Param("bookId") UUID bookId,
            Pageable pageable
    );


    @Query("""
    SELECT 
        COALESCE(AVG(r.rating), 0) AS average,
        COUNT(r.id) AS total
    FROM BookReview r
    WHERE r.book.id = :bookId
""")
    RatingStats getRatingStats(UUID bookId);

    @Query("""
       SELECT new com.pm.librarymanagementsystem.payload.dto.response.bookReview.BookReviewResponse(
           r.id,
           u.id,
           u.fullName,
           r.rating,
           r.reviewText,
           r.title,
           r.createdAt,
           r.updatedAt,
           b.id,
           b.title,
           b.coverImageUrl
       )
       FROM BookReview r
       JOIN r.user u
       JOIN r.book b
       WHERE u.id = :userId
       ORDER BY r.createdAt DESC
""")
    Page<BookReviewResponse> findReviewsByUserId(
            @Param("userId") UUID userId,
            Pageable pageable
    );
}
