package com.pm.librarymanagementsystem.payload.dto.resquest.book;

public record SearchBookRequest(
      String searchTerm,
      Long genreId,
      Boolean availableOnly,

      Integer page,
      Integer size,
      String sortBy,
      String sortDirection

) {
    public SearchBookRequest {
        page = (page == null) ? 0 : page;
        size = (size == null) ? 20 : size;
        sortBy = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        sortDirection = (sortDirection == null || sortDirection.isBlank())
                ? "DESC"
                : sortDirection.toUpperCase();
    }
}
