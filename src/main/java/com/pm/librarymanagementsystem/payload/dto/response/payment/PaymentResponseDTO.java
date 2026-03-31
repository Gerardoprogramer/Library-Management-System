package com.pm.librarymanagementsystem.payload.dto.response.payment;

import com.pm.librarymanagementsystem.domain.PaymentType;

public record PaymentResponseDTO(
        Double amount,
        String currency,
        String status,
        String description,
        String customerEmail,
        String date,
        PaymentType type,
        String plan
) {}

