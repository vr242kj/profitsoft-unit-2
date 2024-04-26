package com.example.jsontoxml2.controller.exceptionHandler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidArgument(MethodArgumentNotValidException exception) {
        List<ErrorDetail> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ErrorDetail(fieldError.getField(), fieldError.getRejectedValue(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorResponse response = new ErrorResponse(LocalDateTime.now(), errors);
        return ResponseEntity.badRequest().body(response);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        String errorMessage = "An error occurred: " + ex.getMessage();
        ErrorResponse response = new ErrorResponse(LocalDateTime.now(), errorMessage);
        return ResponseEntity.badRequest().body(response);
    }
}
