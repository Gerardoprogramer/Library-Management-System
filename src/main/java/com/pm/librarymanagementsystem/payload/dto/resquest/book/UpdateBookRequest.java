package com.pm.librarymanagementsystem.payload.dto.resquest.book;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateBookRequest(

        @Size(max = 255, message = "El título no puede superar los 255 caracteres")
        String title,

        @Size(max = 255, message = "El autor no puede superar los 255 caracteres")
        String author,

        Long genreId,

        @Size(max = 255, message = "El editor no puede superar los 255 caracteres")
        String publisher,

        @PastOrPresent(message = "La fecha de publicación no puede ser futura")
        LocalDate publishedDate,

        @Size(max = 50, message = "El idioma no puede superar los 50 caracteres")
        String language,

        @Min(value = 1, message = "Las páginas deben ser mayores a 0")
        Integer pages,

        @Size(max = 500, message = "La descripción no debe superar los 500 caracteres")
        String description,

        @Min(value = 1, message = "El total de copias debe ser mayor a 0")
        Integer totalCopies,

        @Min(value = 0, message = "Las copias disponibles no pueden ser negativas")
        Integer availableCopies,

        @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
        BigDecimal price,

        @Size(max = 255, message = "La URL de la portada no puede superar los 255 caracteres")
        String coverImageUrl,

        Boolean active
) { }
