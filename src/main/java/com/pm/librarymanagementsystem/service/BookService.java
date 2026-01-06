package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.response.book.BookResponse;
import com.pm.librarymanagementsystem.payload.dto.response.book.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.book.CreateBookRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.book.SearchBookRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.book.UpdateBookRequest;

import java.util.List;

public interface BookService {

    BookResponse createBook(CreateBookRequest book);

    BookResponse getBookById(Long id);

    List<BookResponse> getAllBooks();

    List<BookResponse> createBooksBulk();

    BookResponse updateBook(Long id, UpdateBookRequest book);

    void deleteBook(Long id);

    void hardDeleteBook(Long id);

    PageResponse<BookResponse> searchBooksWithFilters(
            SearchBookRequest searchBookRequest
    );

    long getTotalActiveBooks();

    long getTotalAvailableBooks();
}
