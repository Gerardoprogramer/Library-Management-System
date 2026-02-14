package com.pm.librarymanagementsystem.payload.dto.request.bookReview;

import jakarta.validation.constraints.*;

public record CreateReviewRequest(

        @NotNull(message = "El id del libro es obligatorio")
        Long bookId,

        @NotNull(message = "El rating es obligatorio")
        @Min(value = 1, message = "El rating mínimo es 1")
        @Max(value = 5, message = "El rating máximo es 5")
        Integer rating,

        @NotBlank(message = "El texto de la reseña es obligatorio")
        @Size(min = 10, max = 2000, message = "La reseña debe tener entre 10 y 2000 caracteres")
        String reviewText,

        @Size(max = 255, message = "El título no puede superar 255 caracteres")
        String title
) {
}
