package com.example.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.calculator.exception.DivisionByZeroException;
import com.example.calculator.model.CalculationResponse;
import com.example.calculator.service.CalculatorService;

@SpringBootTest
public class CalculatorServiceTest {
    private final CalculatorService service = new CalculatorService();

    @Test
    void add_shouldReturnCorrectResult() {
		CalculationResponse response = this.service.add(110, 290);
        assertEquals("add", response.getOperation());
        assertEquals(110, response.getA());
        assertEquals(290, response.getB());
        assertEquals(400, response.getResult());
	}

    @Test
    void subtract_shouldReturnCorrectResult() {
		CalculationResponse response = this.service.subtract(100, 110);
        assertEquals("subtract", response.getOperation());
        assertEquals(100, response.getA());
        assertEquals(110, response.getB());
        assertEquals(-10, response.getResult());
	}

    @Test
    void multiply_shouldReturnCorrectResult() {
		CalculationResponse response = this.service.multiply(20, 30);
        assertEquals("multiply", response.getOperation());
        assertEquals(20, response.getA());
        assertEquals(30, response.getB());
        assertEquals(600, response.getResult());
	}

    @Test
    void divide_shouldReturnCorrectResult() {
		CalculationResponse response = this.service.divide(100, 25);
        assertEquals("divide", response.getOperation());
        assertEquals(100, response.getA());
        assertEquals(25, response.getB());
        assertEquals(4, response.getResult());
	}

    @Test
    void divide_throwsDivideByZeroException() {
        assertThrows(
            DivisionByZeroException.class,
            () -> service.divide(10, 0)
        );
	}

}
