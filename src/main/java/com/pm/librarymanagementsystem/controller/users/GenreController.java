package com.pm.librarymanagementsystem.controller.users;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.response.genre.GenreResponse;
import com.pm.librarymanagementsystem.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/genres")
public class GenreController {

    private final GenreService genreService;

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
