package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.dto.response.genre.GenreResponse;
import com.pm.librarymanagementsystem.dto.resquest.genre.CreateGenreRequest;
import com.pm.librarymanagementsystem.dto.resquest.genre.UpdateGenreRequest;
import com.pm.librarymanagementsystem.exception.GenreAlreadyExistsException;
import com.pm.librarymanagementsystem.exception.GenreNotFoundException;
import com.pm.librarymanagementsystem.exception.ParentGenreNotFoundException;
import com.pm.librarymanagementsystem.mapper.GenreMapper;
import com.pm.librarymanagementsystem.modal.Genre;
import com.pm.librarymanagementsystem.repository.GenreRepository;
import com.pm.librarymanagementsystem.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    @Override
    public GenreResponse createGenre(CreateGenreRequest request) {

        Genre parentGenre = null;

        if (genreRepository.existsByCode(request.code())) {
            throw new GenreAlreadyExistsException("Ya existe un género con ese código " + request.code());
        }

        if (request.parentGenreId() != null) {
            parentGenre = genreRepository.findById(request.parentGenreId())
                    .orElseThrow(() ->
                            new ParentGenreNotFoundException("Género principal no encontrado"));
        }

        Genre genre = GenreMapper.toEntity(request, parentGenre);
        Genre savedGenre = genreRepository.save(genre);

        return GenreMapper.toResponse(savedGenre);
    }

    @Override
    public GenreResponse updateGenre(Long id, UpdateGenreRequest request) {

        Genre genre = genreRepository.findById(id)
                .orElseThrow(() ->
                        new GenreNotFoundException("Género no encontrado"));

        Genre parentGenre = null;
        if (request.parentGenreId() != null) {
            parentGenre = genreRepository.findById(request.parentGenreId())
                    .orElseThrow(() ->
                            new ParentGenreNotFoundException("Género principal no encontrado"));
        }

        GenreMapper.updateEntity(genre, request, parentGenre);

        return GenreMapper.toResponse(genreRepository.save(genre));
    }

    @Override
    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(GenreMapper::toResponse)
                .collect(Collectors.toList());
    }
}

