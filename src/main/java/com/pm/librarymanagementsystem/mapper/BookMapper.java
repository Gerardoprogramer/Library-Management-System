package com.pm.librarymanagementsystem.mapper;

import com.pm.librarymanagementsystem.payload.dto.response.book.BookResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.book.CreateBookRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.book.UpdateBookRequest;
import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.Genre;

import java.util.function.Consumer;

public class BookMapper {

    private BookMapper(){
    }

    /* =======================
       DTO → ENTITY
       ======================= */

    public static Book toEntity(CreateBookRequest request, Genre genre) {
        Book book = new Book();

        book.setIsbn(request.isbn());
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setGenre(genre);
        book.setPublisher(request.publisher());
        book.setPublishedDate(request.publishedDate());
        book.setLanguage(request.language());
        book.setPages(request.pages());
        book.setDescription(request.description());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.totalCopies());
        book.setPrice(request.price());
        book.setCoverImageUrl(request.coverImageUrl());
        book.setActive(true);

        return book;
    }

    public static void updateEntity(Book book, UpdateBookRequest request, Genre genre) {
        setIfNotNull(request.title(), book::setTitle);
        setIfNotNull(request.author(), book::setAuthor);
        setIfNotNull(genre, book::setGenre);
        setIfNotNull(request.publisher(), book::setPublisher);
        setIfNotNull(request.publishedDate(), book::setPublishedDate);
        setIfNotNull(request.language(), book::setLanguage);
        setIfNotNull(request.pages(), book::setPages);
        setIfNotNull(request.description(), book::setDescription);
        setIfNotNull(request.totalCopies(), book::setTotalCopies);
        setIfNotNull(request.availableCopies(), book::setAvailableCopies);
        setIfNotNull(request.price(), book::setPrice);
        setIfNotNull(request.coverImageUrl(), book::setCoverImageUrl);
        setIfNotNull(request.active(), book::setActive);
    }

    /* =======================
       ENTITY → DTO
       ======================= */

    public static BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre().getId(),
                book.getGenre().getName(),
                book.getPublisher(),
                book.getPublishedDate(),
                book.getLanguage(),
                book.getPages(),
                book.getDescription(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getPrice(),
                book.getCoverImageUrl(),
                book.getActive(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }

    private static <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
