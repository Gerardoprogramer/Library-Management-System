package com.pm.librarymanagementsystem.payload.dto.response.bookReview;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record BookReviewResponse(
        Long id,
        Long bookId,
        Long userId,
        Integer rating,
        String reviewText,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
        ) {
}
