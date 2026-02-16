package com.pm.librarymanagementsystem.payload.dto.request.bookLoan;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BookLoanCheckinRequest(

        @NotNull(message = "El id del préstamo es obligatorio")
        UUID loanId,

        BookLoanStatus status,

        @Size(max = 500, message = "Las notas no pueden superar los 500 caracteres")
        String notes
){

}
