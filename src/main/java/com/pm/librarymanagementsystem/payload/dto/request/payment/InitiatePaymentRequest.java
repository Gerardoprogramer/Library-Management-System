package com.pm.librarymanagementsystem.payload.dto.request.payment;

import com.pm.librarymanagementsystem.domain.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record InitiatePaymentRequest(

        @NotNull(message = "El recurso a pagar es obligatorio")
        UUID payableId,

        @NotNull(message = "El tipo de pago es obligatorio")
        PaymentType paymentType
) {
}