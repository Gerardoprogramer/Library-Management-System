package com.pm.librarymanagementsystem.mapper;

import com.pm.librarymanagementsystem.modal.BookReview;
import com.pm.librarymanagementsystem.payload.dto.response.bookReview.BookReviewResponse;

public class BookReviewMapper {

    private BookReviewMapper() {
    }

    /* =======================
       ENTITY → DTO
       ======================= */
    public static BookReviewResponse toResponse(BookReview review) {
        return new BookReviewResponse(
                review.getId(),
                review.getBook().getId(),
                review.getUser().getId(),
                review.getRating(),
                review.getReviewText(),
                review.getTitle(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
