package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.modal.Genre;
import com.pm.librarymanagementsystem.repository.GenreRepository;
import com.pm.librarymanagementsystem.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    @Override
    public Genre createGenre(Genre genre) {
        return genreRepository.save(genre);
    }

}
