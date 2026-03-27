package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
import com.pm.librarymanagementsystem.modal.BookLoan;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.bookLoan.BookLoansSearchRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.bookLoan.BookLoanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookLoanRepository extends JpaRepository<BookLoan, UUID> {

    List<BookLoan> findByBookId(UUID bookId);

    @Query("""
    select case when count(bl) > 0 then true else false end from BookLoan bl
    where bl.user.id =:userId and bl.book.id =:bookId
    and (bl.status = 'CHECKED_OUT' OR bl.status = 'OVERDUE')
""")
    boolean hasActiveCheckout(
            @Param("userId") UUID userId,
            @Param("bookId") UUID bookId
    );

    @Query("""
    select count(bl) from BookLoan bl where bl.user.id =:userId
    and (bl.status = 'CHECKED_OUT' OR bl.status = 'OVERDUE')
""")
    Long countActiveBookLoansByUser(
            @Param("userId") UUID userId);

    @Query("""
    select count(bl) from BookLoan bl where bl.user.id =:userId
    and bl.status = 'OVERDUE'
""")
    Long countOverdueBookLoansByUser(
            @Param("userId") UUID userId);

    @Query("""
    select bl from BookLoan bl where bl.dueDate < :currentDate
    and (bl.status = 'CHECKED_OUT' OR bl.status = 'OVERDUE')
""")
    Page<BookLoan> findOverdueBookLoans(
            @Param("currentDate")LocalDateTime currentDate,
            Pageable pageable
            );

    boolean existsByUserIdAndBookIdAndStatus(UUID userId, UUID bookId, BookLoanStatus status);

    @Query("""
        SELECT new com.pm.librarymanagementsystem.payload.dto.response.bookLoan.BookLoanResponse(
            bl.id,
            b.id,
            b.title,
            b.author,
            b.coverImageUrl,
            u.id,
            u.fullName,
            bl.type,
            bl.status,
            bl.checkoutDate,
            bl.dueDate,
            0L,
            bl.returnDate,
            bl.renewalCount,
            bl.maxRenewals,
            bl.notes,
            bl.overdue,
            bl.overdueDays,
            COALESCE(f.amount,0)
        )
        FROM BookLoan bl
        JOIN bl.book b
        JOIN bl.user u
        LEFT JOIN Fine f ON f.bookLoan.id = bl.id
        WHERE (:userId IS NULL OR bl.user.id = :userId)
        AND (:bookId IS NULL OR b.id = :bookId)
        AND (:status IS NULL OR bl.status = :status)
        AND (:overdueOnly = false OR bl.overdue = true)
        AND bl.checkoutDate >= COALESCE(:startDate, bl.checkoutDate)
        AND bl.checkoutDate <= COALESCE(:endDate, bl.checkoutDate)
""")
    Page<BookLoanResponse> getBookLoans(
            @Param("userId") UUID userId,
            @Param("overdueOnly") boolean overdueOnly,
            @Param("bookId") UUID bookId,
            @Param("status") BookLoanStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
