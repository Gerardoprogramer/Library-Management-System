package com.pm.librarymanagementsystem.payload.dto.response.bookReview;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookReviewResponse(
        UUID id,
        UUID bookId,
        UUID userId,
        Integer rating,
        String reviewText,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
        ) {
}
