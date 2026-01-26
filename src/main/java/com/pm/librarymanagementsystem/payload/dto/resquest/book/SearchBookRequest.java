package com.pm.librarymanagementsystem.payload.dto.resquest.book;

public record SearchBookRequest(
      String searchTerm,
      Long genreId,
      Boolean availableOnly
) {
}
