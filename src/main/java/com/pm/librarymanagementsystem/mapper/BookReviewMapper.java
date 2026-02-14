package com.pm.librarymanagementsystem.mapper;

import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.BookReview;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.bookReview.CreateReviewRequest;
import com.pm.librarymanagementsystem.payload.dto.request.bookReview.UpdateReviewRequest;
import com.pm.librarymanagementsystem.payload.dto.response.bookReview.BookReviewResponse;

public class BookReviewMapper {

    private BookReviewMapper() {
    }

    /* =======================
       DTO → ENTITY
       ======================= */
    public static BookReview toEntity(CreateReviewRequest request, Book book, User user) {
        BookReview review = new BookReview();

        review.setBook(book);
        review.setUser(user);
        review.setRating(request.rating());
        review.setReviewText(request.reviewText());
        review.setTitle(request.title());

        return review;
    }

    public static void updateEntity(BookReview review, UpdateReviewRequest request) {

        if (request.rating() != null) {
            review.setRating(request.rating());
        }

        if (request.reviewText() != null) {
            review.setReviewText(request.reviewText());
        }

        if (request.title() != null) {
            review.setTitle(request.title());
        }
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
