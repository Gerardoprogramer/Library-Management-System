package com.pm.librarymanagementsystem.payload.dto.response.payment;

import com.pm.librarymanagementsystem.domain.PaymentStatus;

public record PaymentStatusResponse(
        Long paymentId,
        PaymentStatus status,
        boolean successful
) {
}
