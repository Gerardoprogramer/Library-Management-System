package com.pm.librarymanagementsystem.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(GenreAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleGenreAlreadyExistsException(GenreAlreadyExistsException ex){

        log.warn("Ya existe un género con ese código. {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        errors.put("message", "Ya existe un género con ese código.");

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ParentGenreNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleParentGenreNotFoundException(ParentGenreNotFoundException ex){

        log.warn("Género principal no encontrado {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        errors.put("message", "Género principal no encontrado.");

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(GenreNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleGenreNotFoundException(GenreNotFoundException ex){

        log.warn("Género no encontrado {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        errors.put("message", "Género no encontrado.");

        return ResponseEntity.badRequest().body(errors);
    }
}
