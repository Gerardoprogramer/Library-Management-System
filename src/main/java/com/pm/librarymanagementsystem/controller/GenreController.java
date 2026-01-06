package com.pm.librarymanagementsystem.controller;


import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.response.genre.GenreResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.genre.CreateGenreRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.genre.UpdateGenreRequest;
import com.pm.librarymanagementsystem.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreService genreService;

    @PostMapping
    public ResponseEntity<ApiResponse<GenreResponse>> createGenre(
            @Valid @RequestBody CreateGenreRequest request) {

        GenreResponse genre = genreService.createGenre(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Género creado correctamente", genre));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GenreResponse>> updateGenre(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGenreRequest request) {

        GenreResponse genre = genreService.updateGenre(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Género actualizado correctamente", genre)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GenreResponse>>> getAllGenres() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Listado de géneros obtenido correctamente",
                        genreService.getAllGenres()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GenreResponse>> getGenreById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Género encontrado",
                        genreService.getGenreById(id)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGenre(@PathVariable Long id) {
        genreService.deleteGenre(id);

        return ResponseEntity.ok(
                ApiResponse.success("Género desactivado correctamente")
        );
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ApiResponse<Void>> hardDeleteGenre(@PathVariable Long id) {
        genreService.hardDeleGenre(id);

        return ResponseEntity.ok(
                ApiResponse.success("Género eliminado permanentemente")
        );
    }

    @GetMapping("/top-level")
    public ResponseEntity<ApiResponse<List<GenreResponse>>> getTopLevelGenres() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Géneros principales obtenidos",
                        genreService.getTopLevelGenres()
                )
        );
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getTotalActiveGenres() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Total de géneros activos",
                        genreService.getTotalActiveGenres()
                )
        );
    }

    @GetMapping("/{id}/book-count")
    public ResponseEntity<ApiResponse<Long>> getBookCountByGenreId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cantidad de libros del género",
                        genreService.getBookCountByGenreId(id)
                )
        );
    }
}
