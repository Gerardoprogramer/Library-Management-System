package com.pm.librarymanagementsystem.payload.dto.request.fine;

import com.pm.librarymanagementsystem.domain.Currency;
import com.pm.librarymanagementsystem.domain.FineType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FineRequest(
        @NotNull(message = "El usuario es obligatorio")
        Long userId,

        @NotNull(message = "El préstamo es obligatorio")
        Long bookLoanId,

        @NotNull(message = "El tipo de multa es obligatorio")
        FineType type,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "Formato de monto inválido")
        BigDecimal amount,

        @NotNull(message = "La moneda es obligatoria")
        Currency currency,

        @Size(max = 500, message = "La razón no puede superar 500 caracteres")
        String reason,

        @Size(max = 1000, message = "Las notas no pueden superar 1000 caracteres")
        String notes
) {
}
