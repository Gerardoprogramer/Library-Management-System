package com.pm.librarymanagementsystem.controller;


import com.pm.librarymanagementsystem.dto.response.genre.GenreResponse;
import com.pm.librarymanagementsystem.dto.resquest.genre.CreateGenreRequest;
import com.pm.librarymanagementsystem.dto.resquest.genre.UpdateGenreRequest;
import com.pm.librarymanagementsystem.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/genres")
public class GenreController {
    private final GenreService genreService;

    @PostMapping("/create")
    public ResponseEntity<GenreResponse> createGenre(
            @Valid @RequestBody CreateGenreRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(genreService.createGenre(request));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<GenreResponse> updateGenre(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGenreRequest request) {

        return ResponseEntity.ok(
                genreService.updateGenre(id, request)
        );
    }
}
