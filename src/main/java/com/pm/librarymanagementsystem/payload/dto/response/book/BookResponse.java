package com.pm.librarymanagementsystem.payload.dto.response.book;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookResponse(
        UUID id,
        String isbn,
        String title,
        String author,

        UUID genreId,
        String genreName,

        String publisher,
        LocalDate publishedDate,
        String language,
        Integer pages,
        String description,

        Integer totalCopies,
        Integer availableCopies,

        BigDecimal price,
        String coverImageUrl,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
