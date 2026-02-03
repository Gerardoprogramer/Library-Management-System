package com.pm.librarymanagementsystem.payload.dto.response.payment;

import com.pm.librarymanagementsystem.domain.Currency;
import com.pm.librarymanagementsystem.domain.PaymentStatus;
import com.pm.librarymanagementsystem.domain.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentHistoryItemResponse(

        Long id,
        PaymentType paymentType,
        PaymentStatus paymentStatus,

        BigDecimal amount,
        Currency currency,

        LocalDateTime createdAt
) {
}
