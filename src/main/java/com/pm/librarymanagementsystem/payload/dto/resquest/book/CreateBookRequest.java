package com.pm.librarymanagementsystem.payload.dto.resquest.book;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateBookRequest(

        @NotBlank(message = "El ISBN es obligatorio")
        @Pattern(
                regexp = "^(97(8|9))?[\\d\\-]{9,17}[\\dX]$",
                message = "El ISBN no tiene un formato válido"
        )
        @Size(min = 10, max = 17, message = "El ISBN debe tener entre 10 y 17 caracteres")
        String isbn,

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 255, message = "El titulo no puede superar los 255 caracteres")
        String title,

        @NotBlank(message = "El autor es obligatorio")
        String author,

        @NotNull(message = "El género es obligatorio")
        Long genreId,

        @Size(max = 255, message = "El editor no puede superar los 255 caracteres")
        String publisher,

        @PastOrPresent(message = "La fecha de publicación no puede ser futura")
        LocalDate publishedDate,

        @Size(max = 50, message = "El idioma no puede superar los 50 caracteres")
        String language,

        @Min(value = 1, message = "El número de páginas debe ser mayor a 0")
        Integer pages,

        @Size(max = 500, message = "La descripción no debe superar los 500 caracteres")
        String description,

        @NotNull(message = "El total de copias es obligatorio")
        @Min(value = 1, message = "El total de copias debe ser al menos 1")
        Integer totalCopies,

        @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
        BigDecimal price,

        @Size(max = 255, message = "La URL de la portada no puede superar los 255 caracteres")
        String coverImageUrl
) { }
