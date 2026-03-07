package com.pm.librarymanagementsystem.payload.dto.response.bookLoan;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
import com.pm.librarymanagementsystem.domain.BookLoanType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record BookLoanResponse(
        UUID id,

        UUID bookId,
        String bookTitle,
        String author,
        String bookCoverImageUrl,

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
        BigDecimal fineAmount

) {

    public BookLoanResponse {
        if (dueDate != null) {
            remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate);
        } else {
            remainingDays = null;
        }
    }
}
