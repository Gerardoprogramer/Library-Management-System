package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.response.book.BookResponse;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.request.book.CreateBookRequest;
import com.pm.librarymanagementsystem.payload.dto.request.book.SearchBookRequest;
import com.pm.librarymanagementsystem.payload.dto.request.book.UpdateBookRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BookService {

    BookResponse createBook(CreateBookRequest request);

    BookResponse getBookById(UUID id);

    List<BookResponse> createBooksBulk(List<CreateBookRequest> requests);

    BookResponse updateBook(UUID id, UpdateBookRequest request);

    void deleteBook(UUID id);

    void hardDeleteBook(UUID id);

    PageResponse<BookResponse> searchBooksWithFilters(
            SearchBookRequest searchBookRequest, Pageable pageable
    );

    long getTotalActiveBooks();

    long getTotalAvailableBooks();
}
