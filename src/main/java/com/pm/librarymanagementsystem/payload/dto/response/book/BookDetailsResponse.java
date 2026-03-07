package com.pm.librarymanagementsystem.payload.dto.response.book;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BookDetailsResponse(
        UUID id,
        String isbn,
        String title,
        String author,

        String genreName,

        String publisher,
        LocalDate publishedDate,
        String language,
        Integer pages,
        String description,

        Integer totalCopies,
        Integer availableCopies,
        Boolean isWishList,

        BigDecimal price,
        String coverImageUrl,
        Boolean active,
        Double averageRating,
        Long totalReviews
) {
}
