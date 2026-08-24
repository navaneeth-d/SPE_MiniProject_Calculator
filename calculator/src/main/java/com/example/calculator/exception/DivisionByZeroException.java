package com.example.calculator.exception;

public class DivisionByZeroException extends RuntimeException {

    public DivisionByZeroException() {
        super("Cannot divide by zero");
    }
}