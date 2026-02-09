package com.pm.librarymanagementsystem.mapper;

import com.pm.librarymanagementsystem.modal.BookLoan;
import com.pm.librarymanagementsystem.payload.dto.response.bookLoan.BookLoanResponse;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class BookLoanMapper {

    private BookLoanMapper(){}

    /* =======================
       ENTITY → DTO
       ======================= */
    public static BookLoanResponse toResponse(BookLoan entity) {

        return new BookLoanResponse(
                entity.getId(),
                entity.getBook().getId(),
                entity.getBook().getTitle(),
                entity.getUser().getId(),
                entity.getUser().getFullName(),
                entity.getType(),
                entity.getStatus(),
                entity.getCheckoutDate(),
                entity.getDueDate(),
                ChronoUnit.DAYS.between(LocalDateTime.now(), entity.getDueDate()),
                entity.getReturnDate(),
                entity.getRenewalCount(),
                entity.getMaxRenewals(),
                entity.getNotes(),
                entity.isOverdue(),
                entity.getOverdueDays(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
