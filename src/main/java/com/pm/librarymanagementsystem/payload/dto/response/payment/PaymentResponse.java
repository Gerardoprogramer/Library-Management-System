package com.pm.librarymanagementsystem.payload.dto.response.payment;

import com.pm.librarymanagementsystem.domain.Currency;
import com.pm.librarymanagementsystem.domain.PaymentGateway;
import com.pm.librarymanagementsystem.domain.PaymentStatus;
import com.pm.librarymanagementsystem.domain.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,

        PaymentType paymentType,
        PaymentStatus paymentStatus,
        PaymentGateway paymentGateway,

        BigDecimal amount,
        Currency currency,

        String transactionId,
        String checkoutSessionId,
        String paymentIntentId,
        String chargeId,

        String description,
        String failureReason,

        LocalDateTime initiatedAt,
        LocalDateTime completedAt,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
