package com.example.calculator.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.calculator.model.ErrorResponse;


@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DivisionByZeroException.class)
    public ResponseEntity<ErrorResponse> handleDivisionByZero(DivisionByZeroException e) {
        ErrorResponse error = new ErrorResponse("DIVISION_BY_ZERO", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

}
