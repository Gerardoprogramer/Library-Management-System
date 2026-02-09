package com.pm.librarymanagementsystem.payload.dto.request.bookLoan;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookLoanCheckoutRequest(
        @NotNull(message = "El id del libro es obligatorio")
        Long bookId,

        @NotNull(message = "Los días de préstamo son obligatorios")
        @Min(value = 1, message = "Los días de salida deben ser al menos 1")
        Integer checkoutDays,

        @Size(max = 500, message = "Las notas no pueden superar los 500 caracteres")
        String notes

) {
}
