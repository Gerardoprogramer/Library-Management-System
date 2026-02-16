package com.pm.librarymanagementsystem.controller.admin;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.request.genre.CreateGenreRequest;
import com.pm.librarymanagementsystem.payload.dto.request.genre.UpdateGenreRequest;
import com.pm.librarymanagementsystem.payload.dto.response.genre.GenreResponse;
import com.pm.librarymanagementsystem.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/genres")
public class AdminGenreController {

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
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGenreRequest request) {

        GenreResponse genre = genreService.updateGenre(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Género actualizado correctamente", genre)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGenre(@PathVariable UUID id) {
        genreService.deleteGenre(id);

        return ResponseEntity.ok(
                ApiResponse.success("Género desactivado correctamente")
        );
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ApiResponse<Void>> hardDeleteGenre(@PathVariable UUID id) {
        genreService.hardDeleteGenre(id);

        return ResponseEntity.ok(
                ApiResponse.success("Género eliminado permanentemente")
        );
    }
}
