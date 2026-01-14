package com.pm.librarymanagementsystem.controller;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookResponse;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookStatsResponse;
import com.pm.librarymanagementsystem.payload.dto.response.book.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.book.CreateBookRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.book.SearchBookRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.book.UpdateBookRequest;
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

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookResponse>>> searchBooks(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false, defaultValue = "false") Boolean availableOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection){

            SearchBookRequest bookRequest = new SearchBookRequest(
                    searchTerm, genreId, availableOnly,
                    page, size, sortBy, sortDirection
            );

            PageResponse<BookResponse> books = bookService.searchBooksWithFilters(bookRequest);

        String message = books.empty()
                ? "No se encontraron libros"
                : "Libros encontrados correctamente";

            return ResponseEntity.ok(ApiResponse.success(message, books));

    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<BookStatsResponse>> getBookStats(){
        long totalActive = bookService.getTotalActiveBooks();
        long totalAvailable= bookService.getTotalAvailableBooks();

       BookStatsResponse stats = new BookStatsResponse(totalActive, totalAvailable);

       return ResponseEntity.ok(ApiResponse.success("Stats encontrados", stats));
    }

}
