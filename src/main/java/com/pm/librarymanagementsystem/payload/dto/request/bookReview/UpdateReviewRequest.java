package com.pm.librarymanagementsystem.payload.dto.request.bookReview;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateReviewRequest(

        @Min(value = 1, message = "El rating mínimo es 1")
        @Max(value = 5, message = "El rating máximo es 5")
        Integer rating,

        @Size(min = 10, max = 2000, message = "La reseña debe tener entre 10 y 2000 caracteres")
        String reviewText,

        @Size(max = 255, message = "El título no puede superar 255 caracteres")
        String title
) {
}
