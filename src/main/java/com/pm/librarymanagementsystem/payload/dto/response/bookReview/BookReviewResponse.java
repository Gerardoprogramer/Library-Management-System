package com.pm.librarymanagementsystem.payload.dto.response.bookReview;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookReviewResponse(
        UUID id,
        UUID userId,
        String userName,
        Integer rating,
        String reviewText,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        UUID bookId,
        String bookTitle,
        String coverImageUrl
        ) {
}
