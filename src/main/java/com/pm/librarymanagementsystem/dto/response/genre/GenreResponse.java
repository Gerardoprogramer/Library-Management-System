package com.pm.librarymanagementsystem.dto.response.genre;

public record GenreResponse(
        Long id,
        String code,
        String name,
        String description,
        Integer displayOrder,
        Boolean active,
        Long parentGenreId
) {}
