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
       Recurso NO ENCONTRADO
       =============================== */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {

        log.warn("Recurso no encontrado: {}", ex.getMessage());

        return ResponseEntity.status(404).body(
                ApiResponse.error(ex.getMessage())
        );
    }

    /* ===============================
       RECURSO YA EXISTE
       =============================== */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException ex) {

        log.warn("Conflicto de negocio: {}", ex.getMessage());

        return ResponseEntity.status(409).body(
                ApiResponse.error(ex.getMessage())
        );
    }

    /* ===============================
       TOKEN NO VALIDO
       =============================== */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(InvalidTokenException ex) {

        log.warn("Token inválido: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(
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

