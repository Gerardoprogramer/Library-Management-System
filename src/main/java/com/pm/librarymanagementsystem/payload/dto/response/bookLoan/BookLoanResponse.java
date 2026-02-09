package com.pm.librarymanagementsystem.payload.dto.response.bookLoan;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
import com.pm.librarymanagementsystem.domain.BookLoanType;

import java.time.LocalDateTime;

public record BookLoanResponse(
        Long id,

        Long bookId,
        String bookTitle,

        Long userId,
        String userName,

        BookLoanType type,
        BookLoanStatus status,

        LocalDateTime checkoutDate,
        LocalDateTime dueDate,
        Long remainingDays,
        LocalDateTime returnDate,

        Integer renewalCount,
        Integer maxRenewals,

        String notes,

        boolean overdue,
        Integer overdueDays,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
