package com.pm.librarymanagementsystem.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /* ===============================
       VALIDACIONES (@Valid)
       =============================== */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity.badRequest().body(
                new ApiResponse<>(
                        false,
                        "Error de validación",
                        errors
                )
        );
    }

    /* ===============================
       GENRE YA EXISTE
       =============================== */
    @ExceptionHandler(GenreAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleGenreAlreadyExistsException(
            GenreAlreadyExistsException ex) {

        log.warn("Género duplicado: {}", ex.getMessage());

        return ResponseEntity.status(409).body(
                ApiResponse.error(ex.getMessage())
        );
    }

    /* ===============================
       PARENT GENRE NO ENCONTRADO
       =============================== */
    @ExceptionHandler(ParentGenreNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleParentGenreNotFoundException(
            ParentGenreNotFoundException ex) {

        log.warn("Parent genre no encontrado: {}", ex.getMessage());

        return ResponseEntity.status(404).body(
                ApiResponse.error(ex.getMessage())
        );
    }

    /* ===============================
       GENRE NO ENCONTRADO
       =============================== */
    @ExceptionHandler(GenreNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleGenreNotFoundException(
            GenreNotFoundException ex) {

        log.warn("Género no encontrado: {}", ex.getMessage());

        return ResponseEntity.status(404).body(
                ApiResponse.error(ex.getMessage())
        );
    }

    /* ===============================
       BOOK YA EXISTE
       =============================== */
    @ExceptionHandler(BookAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBookAlreadyExistsException(
            BookAlreadyExistsException ex) {

        log.warn("Ya existe un Libro con ese ISBN: {}", ex.getMessage());

        return ResponseEntity.status(409).body(
                ApiResponse.error(ex.getMessage())
        );
    }

    /* ===============================
       LIBRO NO ENCONTRADO
       =============================== */
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleBookNotFoundException(
            BookNotFoundException ex) {

        log.warn("Libro no encontrado: {}", ex.getMessage());

        return ResponseEntity.status(404).body(
                ApiResponse.error(ex.getMessage())
        );
    }

    /* ===============================
   JSON MAL FORMADO / TIPO INVALIDO
   =============================== */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {

        if (ex.getCause() instanceof InvalidFormatException invalidFormat) {

            String fieldName = invalidFormat.getPath().isEmpty()
                    ? "campo"
                    : invalidFormat.getPath().get(0).getFieldName();

            return ResponseEntity.badRequest().body(
                    ApiResponse.error(fieldName + " no válido")
            );
        }

        return ResponseEntity.badRequest().body(
                ApiResponse.error("Solicitud mal formada")
        );
    }

    /* ===============================
       FALLBACK (errores no controlados)
       =============================== */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {

        log.error("Error interno no controlado", ex);

        return ResponseEntity.status(500).body(
                ApiResponse.error("Error interno del servidor")
        );
    }
}

