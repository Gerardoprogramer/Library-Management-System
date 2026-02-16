package com.pm.librarymanagementsystem.payload.dto.response.payment;

import com.pm.librarymanagementsystem.domain.PaymentStatus;

import java.util.UUID;

public record InitiatePaymentResponse(
        UUID paymentId,

        PaymentStatus paymentStatus,

        String checkoutUrl,

        String checkoutSessionId
) {
}
