package com.pm.librarymanagementsystem.payload.dto.response.book;

import java.math.BigDecimal;
import java.util.UUID;

public record BookSummaryResponse(
        UUID id,
        String title,
        String author,
        String genreName,
        Integer pages,
        Integer availableCopies,
        String coverImageUrl,
        Boolean isWishList,
        Double averageRating,
        Long totalReviews
) {
}
