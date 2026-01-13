package com.pm.librarymanagementsystem.controller;

import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookResponse;
import com.pm.librarymanagementsystem.payload.dto.response.genre.GenreResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.book.CreateBookRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.book.UpdateBookRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.genre.UpdateGenreRequest;
import com.pm.librarymanagementsystem.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @Valid @RequestBody CreateBookRequest request) {

        BookResponse book = bookService.createBook(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Libro creado correctamente", book));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<BookResponse>>> createBooksBulk(
            @Valid @RequestBody List<CreateBookRequest> requests) {

       List<BookResponse> books = bookService.createBooksBulk(requests);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Libros creados correctamente en lote",
                        books));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(
            @PathVariable Long id){

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Libro encontrado",
                        bookService.getBookById(id))

        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest request) {

        BookResponse book = bookService.updateBook(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Libro actualizado correctamente", book)
        );
    }
}
