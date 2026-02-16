package com.pm.librarymanagementsystem.payload.dto.response.bookLoan;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
import com.pm.librarymanagementsystem.domain.BookLoanType;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookLoanResponse(
        UUID id,

        UUID bookId,
        String bookTitle,

        UUID userId,
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
