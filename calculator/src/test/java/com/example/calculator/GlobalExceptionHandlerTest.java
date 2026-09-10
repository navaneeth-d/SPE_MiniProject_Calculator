package com.example.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.calculator.exception.DivisionByZeroException;
import com.example.calculator.exception.GlobalExceptionHandler;
import com.example.calculator.model.ErrorResponse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDivisionByZero_shouldReturnBadRequestAndErrorDetails() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDivisionByZero(new DivisionByZeroException());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("DIVISION_BY_ZERO", response.getBody().error());
        assertEquals("Cannot divide by zero", response.getBody().message());
    }
}