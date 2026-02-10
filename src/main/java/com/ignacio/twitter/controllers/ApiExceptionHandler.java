package com.ignacio.twitter.controllers;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String constraint = resolveConstraintName(ex);
        String message = switch (constraint) {
            case "users_handle_key" -> "Handle already exists";
            case "users_email_key" -> "Email already exists";
            case "user_credentials_username_key" -> "Username already exists";
            default -> "Data integrity violation";
        };
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", message));
    }

    private String resolveConstraintName(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                return violation.getConstraintName();
            }
            cause = cause.getCause();
        }
        return null;
    }
}
