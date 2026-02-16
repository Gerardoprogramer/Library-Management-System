package com.pm.librarymanagementsystem.payload.dto.response.fine;

import com.pm.librarymanagementsystem.domain.Currency;
import com.pm.librarymanagementsystem.domain.FineStatus;
import com.pm.librarymanagementsystem.domain.FineType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FineResponse(
        UUID id,
        UUID userId,
        UUID bookLoanId,
        FineType type,
        BigDecimal amount,
        Currency currency,
        FineStatus status,
        String reason,
        String notes,
        UUID waivedByUserId,
        String waiverReason,
        LocalDateTime paidAt,
        UUID processedByUserId,
        String transactionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
