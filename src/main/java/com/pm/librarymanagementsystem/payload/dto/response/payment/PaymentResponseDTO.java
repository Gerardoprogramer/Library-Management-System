package com.pm.librarymanagementsystem.payload.dto.response.payment;

import com.pm.librarymanagementsystem.domain.PaymentType;

import java.time.LocalDateTime;

public record PaymentResponseDTO(
        Double amount,
        String currency,
        String status,
        String description,
        String customerEmail,
        LocalDateTime date,
        PaymentType type,
        String plan
) {}

