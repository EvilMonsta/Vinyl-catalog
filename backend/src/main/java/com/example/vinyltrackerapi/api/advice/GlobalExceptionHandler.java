package com.example.vinyltrackerapi.api.advice;

import com.example.vinyltrackerapi.api.exceptions.ValidationException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String ERROR_MSG = "error";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(field ->
                errors.put(field.getField(), field.getDefaultMessage())
        );
        log.warn("[VALIDATION] Ошибка валидации: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleCustomValidation(ValidationException ex) {
        return ResponseEntity.badRequest().body(Map.of(ERROR_MSG, ex.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatusException(ResponseStatusException ex) {
        Map<String, String> error = Map.of(ERROR_MSG, Objects.requireNonNull(ex.getReason()));
        return new ResponseEntity<>(error, ex.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        return ResponseEntity.internalServerError().body(Map.of(ERROR_MSG,
                "Непредвиденная ошибка: " + ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleEnumParseError(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException ife) {
            Class<?> targetType = ife.getTargetType();

            String fieldName = ife.getPath().get(0).getFieldName();
            String invalidValue = String.valueOf(ife.getValue());

            if (targetType.isEnum()) {
                Object[] allowed = targetType.getEnumConstants();

                String allowedValues = String.join(", ",
                        java.util.Arrays.stream(allowed).map(Object::toString).toList());

                log.warn("[VALIDATION] Некорректное значение '{}' для поля '{}'. Разрешено: {}",
                        invalidValue, fieldName, allowedValues);

                Map<String, String> error = new HashMap<>();
                error.put(fieldName, "Некорректное значение: '" + invalidValue +
                        "'. Разрешённые значения: [" + allowedValues + "]");
                return error;
            }
        }

        log.warn("[VALIDATION] Невалидный JSON: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_MSG, "Невалидный JSON или значение поля");
        return error;
    }
}