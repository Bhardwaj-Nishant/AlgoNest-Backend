package com.algonest.AlgoNest_Backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * ==========================
     * Validation Errors
     * ==========================
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex
    ) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {

            errors.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    /*
     * ==========================
     * ResponseStatusException
     * ==========================
     */

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException ex
    ) {

        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", ex.getStatusCode().value());
        response.put("message", ex.getReason());

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(response);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String,Object>> handleEmailExists(
            EmailAlreadyExistsException ex
    ){

        Map<String,Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status",409);
        response.put("message",ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(response);

    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleUserNotFound(
            UserNotFoundException ex
    ){

        Map<String,Object> response = new HashMap<>();

        response.put("timestamp",LocalDateTime.now());
        response.put("status",404);
        response.put("message",ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(response);

    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<Map<String,Object>> handleOtp(
            InvalidOtpException ex
    ){

        Map<String,Object> response = new HashMap<>();

        response.put("timestamp",LocalDateTime.now());
        response.put("status",400);
        response.put("message",ex.getMessage());

        return ResponseEntity.badRequest()
                .body(response);

    }

    @ExceptionHandler(SupabaseException.class)
    public ResponseEntity<Map<String,Object>> handleSupabase(
            SupabaseException ex
    ){

        Map<String,Object> response = new HashMap<>();

        response.put("timestamp",LocalDateTime.now());
        response.put("status",500);
        response.put("message",ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);

    }

    /*
     * ==========================
     * Runtime Exception
     * ==========================
     */

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(
            RuntimeException ex
    ) {

        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", ex.getMessage());

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    /*
     * ==========================
     * Unknown Exception
     * ==========================
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(
            Exception ex
    ) {

        ex.printStackTrace();

        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("message", "Something went wrong.");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

}