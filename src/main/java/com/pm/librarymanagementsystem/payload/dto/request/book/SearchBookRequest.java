package com.pm.librarymanagementsystem.payload.dto.request.book;

import java.util.UUID;

public record SearchBookRequest(
      String searchTerm,
      UUID genreId,
      Boolean availableOnly
) {
}
