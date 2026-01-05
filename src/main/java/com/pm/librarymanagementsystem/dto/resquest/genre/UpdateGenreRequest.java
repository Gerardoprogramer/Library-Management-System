package com.pm.librarymanagementsystem.dto.resquest.genre;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateGenreRequest(

        @NotBlank(message = "El nombre del género es obligatorio")
        String name,

        @Size(max = 500, message = "La descripción no debe superar los 500 caracteres")
        String description,

        @Min(value = 0, message = "El orden de visualización no puede ser negativo")
        Integer displayOrder,

        Boolean active,

        Long parentGenreId
) {}