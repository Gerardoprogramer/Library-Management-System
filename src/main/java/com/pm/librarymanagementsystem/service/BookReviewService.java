package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.request.bookReview.CreateReviewRequest;
import com.pm.librarymanagementsystem.payload.dto.request.bookReview.UpdateReviewRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.bookReview.BookReviewResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookReviewService {

    BookReviewResponse createReview(CreateReviewRequest request);

    BookReviewResponse updateReview(UUID reviewId, UpdateReviewRequest updateReviewRequest);

    void deleteReview(UUID reviewId);

    PageResponse<BookReviewResponse> getReviewsByBookId(UUID bookId, Pageable pageable);
}
