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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class BookReviewController {

    private final BookReviewService bookReviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookReviewResponse>> createReview(
            @Valid @RequestBody CreateReviewRequest request
            ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Reseña creada",
                        bookReviewService.createReview(request)
                ));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<BookReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request
    ){
        return ResponseEntity.ok(ApiResponse.success(
                        "Reseña actualizada",
                        bookReviewService.updateReview(reviewId, request)
                ));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId
    ){
        bookReviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success(
                "Reseña eliminada correctamente"
        ));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<ApiResponse<PageResponse<BookReviewResponse>>> getReviewsByBook(
            @PathVariable Long bookId,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ){
        return ResponseEntity.ok(ApiResponse.success(
                "se listo todas las reseñas del libro",
                bookReviewService.getReviewsByBookId(bookId, pageable)
        ));
    }
}
