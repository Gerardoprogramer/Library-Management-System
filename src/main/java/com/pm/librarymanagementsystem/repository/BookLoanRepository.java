package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.BookLoan;
import com.pm.librarymanagementsystem.modal.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BookLoanRepository extends JpaRepository<BookLoan, Long> {

    Page<BookLoan> findByUserId(Long userId, Pageable pageable);

    Page<BookLoan> findByStatusAndUser(User user, BookLoanStatus status, Pageable pageable);

    Page<BookLoan> findByStatus(BookLoanStatus status, Pageable pageable);

    Page<BookLoan> findByBookId(Long bookId, Pageable pageable);

    @Query("""
    select case when count(bl) > 0 then true else false end from BookLoan bl
    where bl.user.id =:userId and bl.book.id =:bookId
    and (bl.status = 'CHECKED_OUT' OR bl.status = 'OVERDUE')
""")
    boolean hasActiveCheckout(
            @Param("userId") Long userId,
            @Param("bookId") Long bookId
    );

    @Query("""
    select count(bl) from BookLoan bl where bl.user.id =:userId
    and (bl.status = 'CHECKED_OUT' OR bl.status = 'OVERDUE')
""")
    Long countActiveBookLoansByUser(
            @Param("userId") Long userId);

    @Query("""
    select count(bl) from BookLoan bl where bl.user.id =:userId
    and bl.status = 'OVERDUE'
""")
    Long countOverdueBookLoansByUser(
            @Param("userId") Long userId);

    @Query("""
    select bl from BookLoan bl where bl.dueDate < :currentDate
    and (bl.status = 'CHECKED_OUT' OR bl.status = 'OVERDUE')
""")
    Page<BookLoan> findOverdueBookLoans(
            @Param("currentDate")LocalDateTime currentDate,
            Pageable pageable
            );

    @Query("""
    select bl from BookLoan bl where bl.checkoutDate between :startDate and :endDate
""")
    Page<BookLoan> findBookLoansByDateRange(
           @Param("startDate") LocalDateTime startDate,
           @Param("endDate") LocalDateTime endDate,
           Pageable pageable);

    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, BookLoanStatus status);
}
