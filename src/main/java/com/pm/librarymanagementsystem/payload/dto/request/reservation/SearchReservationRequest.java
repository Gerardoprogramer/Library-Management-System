package com.pm.librarymanagementsystem.payload.dto.request.reservation;

import com.pm.librarymanagementsystem.domain.ReservationStatus;

import java.util.UUID;

public record SearchReservationRequest(

        UUID bookId,

        ReservationStatus status,

        Boolean activityOnly
) {
}
