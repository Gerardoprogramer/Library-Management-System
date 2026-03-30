package com.pm.librarymanagementsystem.payload.dto.request.payment;

import com.pm.librarymanagementsystem.domain.Currency;
import com.pm.librarymanagementsystem.domain.PaymentType;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record InitiatePaymentRequest(

        @NotNull
        UUID payableId,

        @NotNull(message = "El tipo de pago es obligatorio")
        PaymentType paymentType,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a 0")
        BigDecimal amount,

        @NotNull(message = "La moneda es obligatoria")
        Currency currency,

        @Size(max = 255)
        String description
) {
}
