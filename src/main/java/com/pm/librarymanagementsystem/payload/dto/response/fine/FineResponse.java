package com.pm.librarymanagementsystem.payload.dto.response.fine;

import com.pm.librarymanagementsystem.domain.Currency;
import com.pm.librarymanagementsystem.domain.FineStatus;
import com.pm.librarymanagementsystem.domain.FineType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FineResponse(
        Long id,
        Long userId,
        Long bookLoanId,
        FineType type,
        BigDecimal amount,
        Currency currency,
        FineStatus status,
        String reason,
        String notes,
        Long waivedByUserId,
        String waiverReason,
        LocalDateTime paidAt,
        Long processedByUserId,
        String transactionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
