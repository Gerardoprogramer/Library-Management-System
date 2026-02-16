package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.response.genre.GenreResponse;
import com.pm.librarymanagementsystem.payload.dto.request.genre.CreateGenreRequest;
import com.pm.librarymanagementsystem.payload.dto.request.genre.UpdateGenreRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface GenreService {

    GenreResponse createGenre(CreateGenreRequest request);

    GenreResponse updateGenre(UUID id, UpdateGenreRequest request);

    List<GenreResponse> getAllGenres();

    GenreResponse getGenreById(UUID id);

    void deleteGenre(UUID id);

    void hardDeleteGenre(UUID id);

    List<GenreResponse> getAllActiveGenresWithSubGenres();

    List<GenreResponse> getTopLevelGenres();

    Page<GenreResponse> searchGenres(String searchTerm, Pageable pageable);

    long getTotalActiveGenres();

    long getBookCountByGenreId(UUID id);
}
