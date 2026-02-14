package com.pm.librarymanagementsystem.payload.dto.request.reservation;

import jakarta.validation.constraints.NotNull;

public record ReservationRequest(

        @NotNull(message = "El libro es obligatorio")
        Long bookId,

        String notes
) {
}
