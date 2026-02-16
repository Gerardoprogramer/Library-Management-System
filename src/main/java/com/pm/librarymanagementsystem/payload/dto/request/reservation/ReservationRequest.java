package com.pm.librarymanagementsystem.payload.dto.request.reservation;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReservationRequest(

        @NotNull(message = "El libro es obligatorio")
        UUID bookId,

        String notes
) {
}
