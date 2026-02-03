package com.pm.librarymanagementsystem.payload.dto.response.payment;

public record GatewayPaymentResponse(
        String checkoutUrl,
        String checkoutSessionId,
        String paymentIntentId
) {
}
