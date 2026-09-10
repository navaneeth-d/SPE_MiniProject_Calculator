package com.example.calculator.model;

public record ErrorResponse(
        String error,
        String message
) {}