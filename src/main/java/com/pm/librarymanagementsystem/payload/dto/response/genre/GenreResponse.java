package com.pm.librarymanagementsystem.payload.dto.response.genre;

import java.util.UUID;

public record GenreResponse(
        UUID id,
        String code,
        String name,
        String description,
        Integer displayOrder,
        Boolean active,
        UUID parentGenreId
) {}
