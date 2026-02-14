package com.pm.librarymanagementsystem.payload.dto.request.reservation;

import com.pm.librarymanagementsystem.domain.ReservationStatus;

public record SearchReservationRequest(

        Long bookId,

        ReservationStatus status,

        Boolean activityOnly
) {
}
