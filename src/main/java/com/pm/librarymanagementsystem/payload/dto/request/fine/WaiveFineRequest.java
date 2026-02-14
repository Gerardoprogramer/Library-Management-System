package com.pm.librarymanagementsystem.payload.dto.request.fine;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WaiveFineRequest(

        @NotNull(message = "la multa es obligatoria")
        Long fineId,

        @Size(max = 500, message = "La razón no puede superar 500 caracteres")
        String reason
) {
}
