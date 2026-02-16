package com.pm.librarymanagementsystem.payload.dto.response.payment;

import com.pm.librarymanagementsystem.domain.PaymentStatus;

import java.util.UUID;

public record PaymentStatusResponse(
        UUID paymentId,
        PaymentStatus status,
        boolean successful
) {
}
