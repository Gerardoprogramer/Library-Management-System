package com.pm.librarymanagementsystem.payload.dto.resquest.SubscriptionPlan;

import com.pm.librarymanagementsystem.domain.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateSubscriptionPlanRequest (
        @NotBlank(message = "El código del plan es obligatorio.")
        @Size(max = 50, message = "El código del plan no puede superar los 50 caracteres.")
        String planCode,

        @NotBlank(message = "El nombre del plan es obligatorio.")
        @Size(max = 100, message = "El nombre del plan no puede superar los 100 caracteres.")
        String name,

        @Size(max = 255, message = "La descripción no puede superar los 255 caracteres.")
        String description,

        @NotNull(message = "La duración del plan en días es obligatoria.")
        @Positive(message = "La duración del plan debe ser un número positivo.")
        Integer durationDays,

        @NotNull(message = "El precio del plan es obligatorio.")
        @Positive(message = "El precio del plan debe ser mayor que cero.")
        Long price,

        @NotNull(message = "La moneda del plan es obligatoria.")
        Currency currency,

        @NotNull(message = "El número máximo de libros permitidos es obligatorio.")
        @Positive(message = "El número máximo de libros permitidos debe ser positivo.")
        Integer maxBooksAllowed,

        @NotNull(message = "El número máximo de días por libro es obligatorio.")
        @Positive(message = "El número máximo de días por libro debe ser positivo.")
        Integer maxDaysPerBook,

        Integer displayOrder,

        Boolean active,

        Boolean featured,

        @Size(max = 50, message = "El texto del badge no puede superar los 50 caracteres.")
        String badgeText,

        @Size(max = 500, message = "Las notas administrativas no pueden superar los 500 caracteres.")
        String adminNotes
){

}
