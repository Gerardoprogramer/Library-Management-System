package com.pm.librarymanagementsystem.payload.dto.request.payment;

import com.pm.librarymanagementsystem.domain.Currency;
import com.pm.librarymanagementsystem.domain.PaymentType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record InitiatePaymentRequest(

        Long bookLoanId,

        Long subscriptionId,

        @NotNull(message = "El tipo de pago es obligatorio")
        PaymentType paymentType,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a 0")
        BigDecimal amount,

        @NotNull(message = "La moneda es obligatoria")
        Currency currency,

        @Size(max = 255)
        String description,

        @NotBlank(message = "La successUrl es obligatoria")
        String successUrl,

        @NotBlank(message = "La cancelUrl es obligatoria")
        String cancelUrl
) {
}
