package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
import com.pm.librarymanagementsystem.exception.ConflictException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.BookMapper;
import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.Genre;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookDetailsResponse;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookResponse;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.request.book.CreateBookRequest;
import com.pm.librarymanagementsystem.payload.dto.request.book.SearchBookRequest;
import com.pm.librarymanagementsystem.payload.dto.request.book.UpdateBookRequest;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookSummaryResponse;
import com.pm.librarymanagementsystem.repository.*;
import com.pm.librarymanagementsystem.service.BookService;
import com.pm.librarymanagementsystem.service.RatingStats;
import com.pm.librarymanagementsystem.service.UserService;
import com.pm.librarymanagementsystem.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final BookReviewRepository bookReviewRepository;
    private final WishlistRepository wishlistRepository;
    private final BookLoanRepository loanRepository;

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
    public BookDetailsResponse getBookById(UUID id) {
        BookDetailsResponse base = bookRepository.findBookBase(id)
                .orElseThrow(() -> new NotFoundException("Libro no encontrado"));

        RatingStats stats = bookReviewRepository.getRatingStats(id);
        UUID userId = getCurrentUserId();

        boolean hasReturned = false;
        boolean alreadyReviewed = false;
        boolean inWishlist = false;

        if (userId != null) {
            hasReturned = loanRepository.existsByUserIdAndBookIdAndStatus(userId, id, BookLoanStatus.RETURNED);

            alreadyReviewed = bookReviewRepository.existsByUserIdAndBookId(userId, id);

            inWishlist = wishlistRepository.isInWishlist(id, userId);
        }

        boolean canReview = hasReturned && !alreadyReviewed;

        return new BookDetailsResponse(
                base.id(),
                base.isbn(),
                base.title(),
                base.author(),
                base.genreName(),
                base.publisher(),
                base.publishedDate(),
                base.language(),
                base.pages(),
                base.description(),
                base.totalCopies(),
                base.availableCopies(),
                inWishlist,
                base.price(),
                base.coverImageUrl(),
                base.active(),
                stats.getAverage(),
                stats.getTotal(),
                canReview,
                hasReturned,
                alreadyReviewed
        );
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
    public BookResponse updateBook(UUID id, UpdateBookRequest request) {

        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Libro no encontrado"));

        Genre genre = genreRepository.findById(request.genreId())
                .orElseThrow(() ->
                        new NotFoundException("Género no encontrado"));

        BookMapper.updateEntity(book, request, genre);

        return BookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    public void deleteBook(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Libro no encontrado"));

        book.setActive(false);
        bookRepository.save(book);
    }

    @Override
    public void hardDeleteBook(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Libro no encontrado"));

        bookRepository.delete(book);
    }

    @Override
    public PageResponse<BookSummaryResponse> searchBooksWithFilters(SearchBookRequest searchBookRequest, Pageable pageable) {

        Page<BookSummaryResponse> bookPage =  bookRepository.searchBooksWithSummary(
                searchBookRequest.searchTerm(),
                searchBookRequest.genreId(),
                searchBookRequest.availableOnly(),
                getCurrentUserId(),
                pageable

        );
        return new PageResponse<>(bookPage.getContent(),
                bookPage.getNumber(),
                bookPage.getSize(),
                bookPage.getTotalElements(),
                bookPage.getTotalPages(),
                bookPage.isLast(),
                bookPage.isFirst(),
                bookPage.isEmpty());
    }


    @Override
    public long getTotalActiveBooks() {
        return bookRepository.countByActiveTrue();
    }

    @Override
    public long getTotalAvailableBooks() {
        return bookRepository.countAvailableBooks();
    }

    private UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
