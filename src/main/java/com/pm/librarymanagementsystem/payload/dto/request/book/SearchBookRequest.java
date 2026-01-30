package com.pm.librarymanagementsystem.payload.dto.request.book;

public record SearchBookRequest(
      String searchTerm,
      Long genreId,
      Boolean availableOnly
) {
}
