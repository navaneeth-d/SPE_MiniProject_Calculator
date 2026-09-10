package com.example.calculator.service;
import org.springframework.stereotype.Service;

import com.example.calculator.exception.DivisionByZeroException;
import com.example.calculator.model.CalculationResponse;


@Service
public class CalculatorService {
    public CalculationResponse add(double a, double b) {
        return new CalculationResponse("add", a, b, a+b);
    }

    public CalculationResponse subtract(double a, double b) {
        return new CalculationResponse("subtract", a, b, a-b);
    }

    public CalculationResponse multiply(double a, double b) {
        return new CalculationResponse("multiply", a, b, a*b);
    }

    public CalculationResponse divide(double a, double b) {
        if(b == 0){
            throw new DivisionByZeroException();
        }
        return new CalculationResponse("divide", a, b, a/b);
    }
}
