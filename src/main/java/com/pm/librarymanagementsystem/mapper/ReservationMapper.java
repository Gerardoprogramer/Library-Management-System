package com.pm.librarymanagementsystem.mapper;

import com.pm.librarymanagementsystem.modal.Reservation;
import com.pm.librarymanagementsystem.payload.dto.response.reservation.ReservationResponse;

public class ReservationMapper {

    private ReservationMapper() {
    }

    /* =======================
       ENTITY → DTO
       ======================= */
    public static ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),

                reservation.getBook().getId(),
                reservation.getUser().getId(),

                reservation.getStatus(),

                reservation.getQueuePosition(),

                reservation.getNotificationSent(),

                reservation.getNotes(),

                reservation.getReservedAt(),
                reservation.getAvailableAt(),
                reservation.getAvailableUntil(),
                reservation.getCancelledAt(),
                reservation.getFulfilledAt(),

                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}
