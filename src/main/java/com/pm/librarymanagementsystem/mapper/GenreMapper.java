package com.pm.librarymanagementsystem.mapper;

import com.pm.librarymanagementsystem.payload.dto.response.genre.GenreResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.genre.CreateGenreRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.genre.UpdateGenreRequest;
import com.pm.librarymanagementsystem.modal.Genre;

public class GenreMapper {

    private GenreMapper() {
    }

    /* =======================
       DTO → ENTITY
       ======================= */

    public static Genre toEntity(CreateGenreRequest request, Genre parentGenre) {
        Genre genre = new Genre();
        genre.setCode(request.code());
        genre.setName(request.name());
        genre.setDescription(request.description());
        genre.setDisplayOrder(
                request.displayOrder() != null ? request.displayOrder() : 0
        );
        genre.setParentGenre(parentGenre);
        return genre;
    }

    public static void updateEntity(Genre genre, UpdateGenreRequest request, Genre parentGenre) {
        genre.setCode(request.code());
        genre.setName(request.name());
        genre.setDescription(request.description());
        genre.setDisplayOrder(request.displayOrder());
        genre.setActive(request.active());
        genre.setParentGenre(parentGenre);
    }

    /* =======================
       ENTITY → DTO
       ======================= */

    public static GenreResponse toResponse(Genre genre) {
        return new GenreResponse(
                genre.getId(),
                genre.getCode(),
                genre.getName(),
                genre.getDescription(),
                genre.getDisplayOrder(),
                genre.getActive(),
                genre.getParentGenre() != null
                        ? genre.getParentGenre().getId()
                        : null
        );
    }
}