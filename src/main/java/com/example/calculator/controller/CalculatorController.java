package com.example.calculator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.calculator.model.CalculationResponse;
import com.example.calculator.service.CalculatorService;


@RestController
@RequestMapping("/api/v1")
public class CalculatorController {
    private final CalculatorService calculatorService;
    
    public CalculatorController(CalculatorService calculatorService){
        this.calculatorService = calculatorService;
    } 

    @GetMapping("/add")
    public CalculationResponse add (@RequestParam double a, @RequestParam double b){
        return calculatorService.add(a, b);
    }

    @GetMapping("/subtract")
    public CalculationResponse subtract (@RequestParam double a, @RequestParam double b){
        return calculatorService.subtract(a, b);
    }

    @GetMapping("/multiply")
    public CalculationResponse multiply (@RequestParam double a, @RequestParam double b){
        return calculatorService.multiply(a, b);
    }

    @GetMapping("/divide")
    public CalculationResponse divide (@RequestParam double a, @RequestParam double b){
        return calculatorService.divide(a, b);
    }
}
