package com.pm.librarymanagementsystem.payload.dto.request.bookLoan;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookLoansSearchRequest(

        UUID userId,
        UUID bookId,
        BookLoanStatus status,
        Boolean overdueOnly,
        Boolean unpaidFinesOnly,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
