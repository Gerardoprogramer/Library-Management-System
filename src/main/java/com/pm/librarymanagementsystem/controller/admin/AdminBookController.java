package com.pm.librarymanagementsystem.controller.admin;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.request.book.CreateBookRequest;
import com.pm.librarymanagementsystem.payload.dto.request.book.UpdateBookRequest;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookResponse;
import com.pm.librarymanagementsystem.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/books")
public class AdminBookController {

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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest request) {

        BookResponse book = bookService.updateBook(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Libro actualizado correctamente", book)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);

        return ResponseEntity.ok(
                ApiResponse.success("Libro desactivado correctamente")
        );
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ApiResponse<Void>> hardDeleteBook(@PathVariable Long id) {
        bookService.hardDeleteBook(id);

        return ResponseEntity.ok(
                ApiResponse.success("Libro eliminado permanentemente")
        );
    }
}
