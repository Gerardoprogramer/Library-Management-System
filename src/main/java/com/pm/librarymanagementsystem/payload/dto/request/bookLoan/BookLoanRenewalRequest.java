package com.pm.librarymanagementsystem.payload.dto.request.bookLoan;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookLoanRenewalRequest(
        @NotNull(message = "El id del préstamo es obligatorio")
        Long loanId,

        @NotNull(message = "Los días de extensión son obligatorios")
        @Min(value = 1, message = "La extensión de días deben ser al menos 1")
        Integer extensionDays,

        @Size(max = 500, message = "Las notas no pueden superar los 500 caracteres")
        String notes
) {
}
