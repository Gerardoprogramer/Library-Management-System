package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.exception.BookAlreadyExistsException;
import com.pm.librarymanagementsystem.exception.GenreNotFoundException;
import com.pm.librarymanagementsystem.mapper.BookMapper;
import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.Genre;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookResponse;
import com.pm.librarymanagementsystem.payload.dto.response.book.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.book.CreateBookRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.book.SearchBookRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.book.UpdateBookRequest;
import com.pm.librarymanagementsystem.repository.BookRepository;
import com.pm.librarymanagementsystem.repository.GenreRepository;
import com.pm.librarymanagementsystem.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;

    @Override
    public BookResponse createBook(CreateBookRequest request) {

        if(bookRepository.existsByIsbn(request.isbn())){
            throw new BookAlreadyExistsException("El libro con el isbn " + request.isbn() + " ya existe");
        }

        Genre   genre = genreRepository.findById(request.genreId())
                    .orElseThrow(() ->
                            new GenreNotFoundException("Género no encontrado"));

        Book book = BookMapper.toEntity(request, genre);

        return BookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    public BookResponse getBookById(Long id) {
        return null;
    }

    @Override
    public List<BookResponse> getAllBooks() {
        return List.of();
    }

    @Override
    public List<BookResponse> createBooksBulk(List<BookResponse> bookResponses) {
        return List.of();
    }

    @Override
    public BookResponse updateBook(Long id, UpdateBookRequest book) {
        return null;
    }

    @Override
    public void deleteBook(Long id) {

    }

    @Override
    public void hardDeleteBook(Long id) {

    }

    @Override
    public PageResponse<BookResponse> searchBooksWithFilters(SearchBookRequest searchBookRequest) {
        return null;
    }

    @Override
    public long getTotalActiveBooks() {
        return 0;
    }

    @Override
    public long getTotalAvailableBooks() {
        return 0;
    }
}
