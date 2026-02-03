package com.pm.librarymanagementsystem.payload.dto.response.payment;

import com.pm.librarymanagementsystem.domain.PaymentStatus;

public record InitiatePaymentResponse(
        Long paymentId,

        PaymentStatus paymentStatus,

        String checkoutUrl,

        String checkoutSessionId
) {
}
