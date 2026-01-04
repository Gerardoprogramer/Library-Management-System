package com.pm.librarymanagementsystem.controller;


import com.pm.librarymanagementsystem.modal.Genre;
import com.pm.librarymanagementsystem.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @PostMapping
    public ResponseEntity<Genre> addGenre(@RequestBody Genre genre) {
        Genre createdGenre = genreService.createGenre(genre);

        return ResponseEntity.ok(createdGenre);
    }
}
