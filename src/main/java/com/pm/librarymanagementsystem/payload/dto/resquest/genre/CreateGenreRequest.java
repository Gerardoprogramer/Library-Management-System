package com.pm.librarymanagementsystem.payload.dto.resquest.genre;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGenreRequest(

        @NotBlank(message = "El código de género es obligatorio")
        String code,

        @NotBlank(message = "El nombre del género es obligatorio")
        String name,

        @Size(max = 500, message = "La descripción no debe superar los 500 caracteres")
        String description,

        @Min(value = 0, message = "El orden de visualización no puede ser negativo")
        Integer displayOrder,

        Long parentGenreId
) {}
