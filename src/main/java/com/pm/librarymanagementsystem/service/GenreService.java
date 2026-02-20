package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookResponse;
import com.pm.librarymanagementsystem.payload.dto.response.genre.GenreResponse;
import com.pm.librarymanagementsystem.payload.dto.request.genre.CreateGenreRequest;
import com.pm.librarymanagementsystem.payload.dto.request.genre.UpdateGenreRequest;

import java.util.List;
import java.util.UUID;

public interface GenreService {

    GenreResponse createGenre(CreateGenreRequest request);

    GenreResponse updateGenre(UUID id, UpdateGenreRequest request);

    List<GenreResponse> getAllGenres();

    GenreResponse getGenreById(UUID id);

    void deleteGenre(UUID id);

    void hardDeleteGenre(UUID id);

    List<GenreResponse> getTopLevelGenres();

    long getTotalActiveGenres();

    long getBookCountByGenreId(UUID id);
}
