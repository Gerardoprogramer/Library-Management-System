package com.pm.librarymanagementsystem.payload.dto.response.reservation;

import com.pm.librarymanagementsystem.domain.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,

        Long bookId,
        Long userId,

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
