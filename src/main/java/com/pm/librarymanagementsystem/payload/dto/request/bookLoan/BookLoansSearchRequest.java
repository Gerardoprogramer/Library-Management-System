package com.pm.librarymanagementsystem.payload.dto.request.bookLoan;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;

import java.time.LocalDateTime;

public record BookLoansSearchRequest(

        Long userId,
        Long bookId,
        BookLoanStatus status,
        Boolean overdueOnly,
        Boolean unpaidFinesOnly,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
