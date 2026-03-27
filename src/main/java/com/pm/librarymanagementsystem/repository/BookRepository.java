package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookDetailsResponse;
import com.pm.librarymanagementsystem.payload.dto.response.book.BookSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    boolean existsByIsbn(String isbn);

    @Query(
            value = """
                SELECT new com.pm.librarymanagementsystem.payload.dto.response.book.BookSummaryResponse(
                    b.id,
                    b.title,
                    b.author,
                    g.name,
                    b.pages,
                    b.availableCopies,
                    b.coverImageUrl,
                    CASE WHEN COUNT(DISTINCT w.id) > 0 THEN true ELSE false END,
                    COALESCE(AVG(r.rating), 0),
                    COUNT(DISTINCT r.id)
                )
                FROM Book b
                LEFT JOIN b.genre g
                LEFT JOIN BookReview r ON r.book.id = b.id
                LEFT JOIN Wishlist w 
                    ON w.book.id = b.id 
                    AND w.user.id = :userId
                WHERE
                (
                    (coalesce(:searchTerm, '') = '' OR
                     lower(b.title) like lower(concat('%', :searchTerm, '%')) OR
                     lower(b.author) like lower(concat('%', :searchTerm, '%')) OR
                     lower(b.isbn) like lower(concat('%', :searchTerm, '%')))
                )
                AND (:genreId is null or g.id = :genreId)
                AND (:availableOnly = false or b.availableCopies > 0)
                AND b.active = true
                GROUP BY
                    b.id,
                    b.title,
                    b.author,
                    g.name,
                    b.pages,
                    b.availableCopies,
                    b.coverImageUrl
                """,
            countQuery = """
                SELECT count(b.id)
                FROM Book b
                LEFT JOIN b.genre g
                WHERE
                (
                    (coalesce(:searchTerm, '') = '' OR
                     lower(b.title) like lower(concat('%', :searchTerm, '%')) OR
                     lower(b.author) like lower(concat('%', :searchTerm, '%')) OR
                     lower(b.isbn) like lower(concat('%', :searchTerm, '%')))
                )
                AND (:genreId is null or g.id = :genreId)
                AND (:availableOnly = false or b.availableCopies > 0)
                AND b.active = true
                """
    )
    Page<BookSummaryResponse> searchBooksWithSummary(
            @Param("searchTerm") String searchTerm,
            @Param("genreId") UUID genreId,
            @Param("availableOnly") boolean availableOnly,
            @Param("userId") UUID userId,
            Pageable pageable
    );

    long countByActiveTrue();

    @Query(
            "select count(b) from Book b where b.availableCopies > 0 and b.active = true"
    )
    long countAvailableBooks();


    @Query("""
    SELECT new com.pm.librarymanagementsystem.payload.dto.response.book.BookDetailsResponse(
        b.id,
        b.isbn,
        b.title,
        b.author,
        g.name,
        b.publisher,
        b.publishedDate,
        b.language,
        b.pages,
        b.description,
        b.totalCopies,
        b.availableCopies,
        false,
        b.price,
        b.coverImageUrl,
        b.active,
        0.0,
        0L,
        false,
        false,
        false
    )
    FROM Book b
    JOIN b.genre g
    WHERE b.id = :bookId
""")
    Optional<BookDetailsResponse> findBookBase(@Param("bookId") UUID bookId);
}
