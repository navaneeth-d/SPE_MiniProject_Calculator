package com.example.calculator.model;

public class CalculationResponse {
    private String operation;
    private double a, b, result;

    public CalculationResponse(String operation, double a, double b, double result) {
        this.operation = operation;
        this.a = a;
        this.b = b;
        this.result = result;
    }

    public String getOperation() {
        return this.operation;
    }

    public double getA() {
        return this.a;
    }

    public double getB() {
        return this.b;
    }

    public double getResult() {
        return this.result;
    }
}
