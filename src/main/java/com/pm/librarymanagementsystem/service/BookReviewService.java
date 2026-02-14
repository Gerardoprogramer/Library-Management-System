package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.request.bookReview.CreateReviewRequest;
import com.pm.librarymanagementsystem.payload.dto.request.bookReview.UpdateReviewRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.bookReview.BookReviewResponse;
import org.springframework.data.domain.Pageable;

public interface BookReviewService {

    BookReviewResponse createReview(CreateReviewRequest request);

    BookReviewResponse updateReview(Long reviewId, UpdateReviewRequest updateReviewRequest);

    void deleteReview(Long reviewId);

    PageResponse<BookReviewResponse> getReviewsByBookId(Long bookId, Pageable pageable);
}
