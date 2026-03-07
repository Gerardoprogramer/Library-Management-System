package com.pm.librarymanagementsystem.payload.dto.response.bookReview;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookReviewResponse(
        UUID id,
        String userName,
        Integer rating,
        String reviewText,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
        ) {
}
