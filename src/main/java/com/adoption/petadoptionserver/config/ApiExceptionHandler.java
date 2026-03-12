package com.adoption.petadoptionserver.config;

import com.adoption.petadoptionserver.exception.AuthenticationException;
import com.adoption.petadoptionserver.service.AuthServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API requests.
 * Converts common exceptions into consistent JSON responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles validation errors triggered by @Valid annotated request bodies.
     *
     * @param ex the validation exception
     * @return a response containing validation error details per field
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Validation failed");
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles bad request exceptions caused by invalid method arguments.
     *
     * @param ex the exception
     * @return a 400 Bad Request response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, Object> body = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", ex.getMessage()
        );

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles illegal state exceptions, usually representing a conflict
     * in the current application state.
     *
     * @param ex the exception
     * @return a 409 Conflict response
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException ex) {
        Map<String, Object> body = Map.of(
                "status", HttpStatus.CONFLICT.value(),
                "message", ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Handles authentication failures.
     *
     * @param ex the authentication exception
     * @return a 401 Unauthorized response
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthFailed(RuntimeException ex) {
        Map<String, Object> body = Map.of(
                "status", HttpStatus.UNAUTHORIZED.value(),
                "message", ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Handles malformed or unreadable request bodies.
     *
     * @param ex the exception
     * @return a 400 Bad Request response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        Map<String, Object> body = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", "Invalid request body"
        );

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles all unexpected exceptions.
     *
     * @param ex the exception
     * @return a 500 Internal Server Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Map<String, Object> body = Map.of(
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "message", "Internal server error"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}