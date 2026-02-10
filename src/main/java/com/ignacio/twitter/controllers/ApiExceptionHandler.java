package com.ignacio.twitter.controllers;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import com.ignacio.twitter.dto.ErrorResponse;
import com.ignacio.twitter.dto.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String constraint = resolveConstraintName(ex);
        ErrorResponse response = switch (constraint) {
            case "users_handle_key" -> new ErrorResponse(ErrorType.HANDLE_ALREADY_EXISTS, "Handle already exists");
            case "users_email_key" -> new ErrorResponse(ErrorType.EMAIL_ALREADY_EXISTS, "Email already exists");
            case "user_credentials_username_key" -> new ErrorResponse(ErrorType.USERNAME_ALREADY_EXISTS, "Username already exists");
            default -> new ErrorResponse(ErrorType.DATA_INTEGRITY_VIOLATION, "Data integrity violation");
        };
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
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
