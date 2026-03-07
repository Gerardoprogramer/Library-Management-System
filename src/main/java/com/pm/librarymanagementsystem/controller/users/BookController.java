package com.pm.librarymanagementsystem.controller.users;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.request.book.SearchBookRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookDetailsResponse;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookResponse;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookStatsResponse;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookSummaryResponse;
import com.pm.librarymanagementsystem.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDetailsResponse>> getBookById(
            @PathVariable UUID id){

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Libro encontrado",
                        bookService.getBookById(id))
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookSummaryResponse>>> searchBooks(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) UUID genreId,
            @RequestParam(required = false, defaultValue = "false") Boolean availableOnly,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable){

        SearchBookRequest bookRequest = new SearchBookRequest(
                searchTerm, genreId, availableOnly
        );

        PageResponse<BookSummaryResponse> books = bookService.searchBooksWithFilters(bookRequest, pageable);

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

        return ResponseEntity.ok(ApiResponse.success("Estadísticas obtenidas correctamente", stats));
    }
}
