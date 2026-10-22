package com.example.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.example.calculator.exception.DivisionByZeroException;
import com.example.calculator.model.CalculationResponse;
import com.example.calculator.service.CalculatorService;

class CalculatorServiceTest {
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
    void add_shouldHandleNegativeAndDecimalValues() {
        CalculationResponse response = service.add(-2.5, 1.25);

        assertEquals(-1.25, response.getResult());
    }

    @Test
    void add_shouldFollowDoublePositiveOverflowBehavior() {
        CalculationResponse response = service.add(Double.MAX_VALUE, Double.MAX_VALUE);

        assertEquals(Double.POSITIVE_INFINITY, response.getResult());
    }

    @Test 
    void add_shouldFollowDoubleNegativeOverflowBehavior() {
        CalculationResponse response = service.add(-Double.MAX_VALUE, -Double.MAX_VALUE);

        assertEquals(Double.NEGATIVE_INFINITY, response.getResult());
    }

    @Test 
    void add_shouldHandleZeroValues() {
        CalculationResponse response = service.add(0, 0);

        assertEquals(0, response.getResult());
    }

    @Test
    void subtract_shouldReturnCorrectResult() {
		CalculationResponse response = this.service.subtract(100, 110);
        assertEquals("subtract", response.getOperation());
        assertEquals(10, response.getA());
        assertEquals(110, response.getB());
        assertEquals(-10, response.getResult());
	}

    @Test
    void subtract_shouldHandleZeroValues() {
        CalculationResponse response = service.subtract(0, 0);

        assertEquals(0, response.getResult());
    }

    @Test 
    void substract_shouldHandleNegativeValues() {
        CalculationResponse response = service.subtract(-5, -10);

        assertEquals(5, response.getResult());
    }

    @Test 
    void subtract_shouldFollowDoublePositiveOverflowBehavior() {
        CalculationResponse response = service.subtract(Double.MAX_VALUE, -Double.MAX_VALUE);

        assertEquals(Double.POSITIVE_INFINITY, response.getResult());
    }

    @Test 
    void subtract_shouldFollowDoubleNegativeOverflowBehavior() {
        CalculationResponse response = service.subtract(-Double.MAX_VALUE, Double.MAX_VALUE);

        assertEquals(Double.NEGATIVE_INFINITY, response.getResult());
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
    void multiply_shouldHandleNegativeValues() {
        CalculationResponse response = service.multiply(-2.5, 4);

        assertEquals(-10, response.getResult());
    }

    @Test
    void multiply_shouldFollowDoublePositiveOverflowBehavior() {
        CalculationResponse response = service.multiply(Double.MAX_VALUE, 2);

        assertEquals(Double.POSITIVE_INFINITY, response.getResult());
    }

    @Test 
    void multiply_shouldFollowDoubleNegativeOverflowBehavior() {
        CalculationResponse response = service.multiply(-Double.MAX_VALUE, 2);

        assertEquals(Double.NEGATIVE_INFINITY, response.getResult());
    }

    @Test 
    void multiply_shouldHandleZeroValues() {
        CalculationResponse response = service.multiply(0, 100);

        assertEquals(0, response.getResult());
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
    void divide_shouldReturnDecimalResult() {
        CalculationResponse response = service.divide(5, 2);

        assertEquals(2.5, response.getResult());
    }

    @Test 
    void divide_shouldHandleNegativeValues() {
        CalculationResponse response = service.divide(-10, 2);

        assertEquals(-5, response.getResult());
    }

    @Test 
    void divide_shouldFollowDoublePositiveOverflowBehavior() {
        CalculationResponse response = service.divide(Double.MAX_VALUE, 0.5);

        assertEquals(Double.POSITIVE_INFINITY, response.getResult());
    }

    @Test 
    void divide_shouldFollowDoubleNegativeOverflowBehavior() {
        CalculationResponse response = service.divide(-Double.MAX_VALUE, 0.5);

        assertEquals(Double.NEGATIVE_INFINITY, response.getResult());
    }

    @Test
    void divide_throwsDivideByZeroException() {
        DivisionByZeroException exception = assertThrows(
            DivisionByZeroException.class,
            () -> service.divide(10, 0)
        );
        assertEquals("Cannot divide by zero", exception.getMessage());
	}

    @Test
    void divide_throwsDivideByZeroException_NegativeZero() {
        DivisionByZeroException exception = assertThrows(
            DivisionByZeroException.class,
            () -> service.divide(10, -0)
        );
        assertEquals("Cannot divide by zero", exception.getMessage());
	}

}
