package com.pm.librarymanagementsystem.payload.dto.request.fine;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record WaiveFineRequest(

        @NotNull(message = "la multa es obligatoria")
        UUID fineId,

        @Size(max = 500, message = "La razón no puede superar 500 caracteres")
        String reason
) {
}
