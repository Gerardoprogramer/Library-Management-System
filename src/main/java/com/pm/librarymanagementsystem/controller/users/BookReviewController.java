package com.pm.librarymanagementsystem.controller.users;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.request.bookReview.CreateReviewRequest;
import com.pm.librarymanagementsystem.payload.dto.request.bookReview.UpdateReviewRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.bookReview.BookReviewResponse;
import com.pm.librarymanagementsystem.service.BookReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class BookReviewController {

    private final BookReviewService bookReviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createReview(
            @Valid @RequestBody CreateReviewRequest request
            ){

        bookReviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Reseña creada correctamente"
                ));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest request
    ){
        bookReviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(ApiResponse.success(
                        "Reseña actualizada"
                ));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable UUID reviewId
    ){
        bookReviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success(
                "Reseña eliminada correctamente"
        ));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<ApiResponse<PageResponse<BookReviewResponse>>> getReviewsByBook(
            @PathVariable UUID bookId,
            @PageableDefault(
                    size = 5,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ){
        return ResponseEntity.ok(ApiResponse.success(
                "se listo todas las reseñas del libro",
                bookReviewService.getReviewsByBookId(bookId, pageable)
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookReviewResponse>>> getMeReviews(
            @PageableDefault(
                    size = 5,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ){
        return ResponseEntity.ok(ApiResponse.success(
                "se listo todas las reseñas del usuario",
                bookReviewService.getMeReviews(pageable)
        ));
    }
}
