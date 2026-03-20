package com.pm.librarymanagementsystem.payload.dto.response.reservation;

import com.pm.librarymanagementsystem.domain.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,

        UUID bookId,
        String bookTitle,
        String author,
        String bookCoverImageUrl,
        UUID userId,

        ReservationStatus status,

        Integer queuePosition,

        Boolean notificationSent,

        String notes,

        LocalDateTime reservedAt,
        LocalDateTime availableAt,
        LocalDateTime availableUntil,
        LocalDateTime cancelledAt,
        LocalDateTime fulfilledAt,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
