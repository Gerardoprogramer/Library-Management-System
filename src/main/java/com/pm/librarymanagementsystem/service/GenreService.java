package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.dto.response.genre.GenreResponse;
import com.pm.librarymanagementsystem.dto.resquest.genre.CreateGenreRequest;
import com.pm.librarymanagementsystem.dto.resquest.genre.UpdateGenreRequest;

public interface GenreService {

    GenreResponse createGenre(CreateGenreRequest request);

    GenreResponse updateGenre(Long id, UpdateGenreRequest request);
}
