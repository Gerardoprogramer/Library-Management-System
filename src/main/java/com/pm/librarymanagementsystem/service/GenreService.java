package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.dto.response.genre.GenreResponse;
import com.pm.librarymanagementsystem.dto.resquest.genre.CreateGenreRequest;
import com.pm.librarymanagementsystem.dto.resquest.genre.UpdateGenreRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GenreService {

    GenreResponse createGenre(CreateGenreRequest request);

    GenreResponse updateGenre(Long id, UpdateGenreRequest request);

    List<GenreResponse> getAllGenres();

    GenreResponse getGenreById(Long id);

    void deleteGenre(Long id);

    void hardDeleGenre(Long id);

    List<GenreResponse> getAllActiveGenresWithSubGenres();

    List<GenreResponse> getTopLevelGenres();

    Page<GenreResponse> searchGenres(String searchTerm, Pageable pageable);

    long getTotalActiveGenres();

    long getBookCountByGenreId(Long id);
}
