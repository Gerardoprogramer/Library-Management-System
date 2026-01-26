package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.exception.ConflictException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;

    @Override
    public BookResponse createBook(CreateBookRequest request) {

        if(bookRepository.existsByIsbn(request.isbn())){
            throw new ConflictException("El libro con el isbn " + request.isbn() + " ya existe");
        }

        Genre genre = genreRepository.findById(request.genreId())
                    .orElseThrow(() ->
                            new NotFoundException("Género no encontrado"));

        Book book = BookMapper.toEntity(request, genre);

        return BookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    public BookResponse getBookById(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Libro no encontrado"));

        return BookMapper.toResponse(book);
    }

    @Override
    public List<BookResponse> createBooksBulk(List<CreateBookRequest> requests) {

        List<BookResponse> createdBooks = new ArrayList<>();
        for(CreateBookRequest request:requests){
            BookResponse book = createBook(request);
            createdBooks.add(book);
        }
        return createdBooks;
    }

    @Override
    public BookResponse updateBook(Long id, UpdateBookRequest request) {

        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Libro no encontrado"));

        Genre genre = genreRepository.findById(request.genreId())
                .orElseThrow(() ->
                        new NotFoundException("Género no encontrado"));

        BookMapper.updateEntity(book, request, genre);

        return BookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Libro no encontrado"));

        book.setActive(false);
        bookRepository.save(book);
    }

    @Override
    public void hardDeleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Libro no encontrado"));

        bookRepository.delete(book);
    }

    @Override
    public PageResponse<BookResponse> searchBooksWithFilters(SearchBookRequest searchBookRequest, Pageable pageable) {

        Page<Book> bookPage =  bookRepository.searchBookswithFilters(
                searchBookRequest.searchTerm(),
                searchBookRequest.genreId(),
                searchBookRequest.availableOnly(),
                pageable

        );
        return toPageResponse(bookPage);
    }

    @Override
    public long getTotalActiveBooks() {
        return bookRepository.countByActiveTrue();
    }

    @Override
    public long getTotalAvailableBooks() {
        return bookRepository.countAvailableBooks();
    }

    @Override
    public BookResponse getBookByISBN(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(()-> new NotFoundException("Libro no encontrado"));

        return BookMapper.toResponse(book);
    }

    public PageResponse<BookResponse> toPageResponse(Page<Book> books) {
        List<BookResponse> bookDtos = books.getContent()
                .stream()
                .map(BookMapper::toResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(bookDtos,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isLast(),
                books.isFirst(),
                books.isEmpty());
    }
}
