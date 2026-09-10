package com.example.calculator;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.calculator.controller.CalculatorController;
import com.example.calculator.exception.DivisionByZeroException;
import com.example.calculator.model.CalculationResponse;
import com.example.calculator.service.CalculatorService;

@WebMvcTest (CalculatorController.class)
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculatorService service;

    @Test
    void  add_shouldReturn200AndCorrectResponse() throws Exception {

        when(service.add(2, 3))
                .thenReturn(new CalculationResponse("add", 2, 3, 5));

        mockMvc.perform(
                get("/api/v1/add")
                        .param("a", "2")
                        .param("b", "3")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operation").value("add"))
        .andExpect(jsonPath("$.a").value(2))
        .andExpect(jsonPath("$.b").value(3))
        .andExpect(jsonPath("$.result").value(5));
    }

    @Test
    void  subtract_shouldReturn200AndCorrectResponse() throws Exception {

        when(service.subtract(2, 3))
                .thenReturn(new CalculationResponse("subtract", 2, 3, -1));

        mockMvc.perform(
                get("/api/v1/subtract")
                        .param("a", "2")
                        .param("b", "3")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operation").value("subtract"))
        .andExpect(jsonPath("$.a").value(2))
        .andExpect(jsonPath("$.b").value(3))
        .andExpect(jsonPath("$.result").value(-1));
    }

    @Test
    void  multiply_shouldReturn200AndCorrectResponse() throws Exception {

        when(service.multiply(2, 3))
                .thenReturn(new CalculationResponse("multiply", 2, 3, 6));

        mockMvc.perform(
                get("/api/v1/multiply")
                        .param("a", "2")
                        .param("b", "3")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operation").value("multiply"))
        .andExpect(jsonPath("$.a").value(2))
        .andExpect(jsonPath("$.b").value(3))
        .andExpect(jsonPath("$.result").value(6));
    }

    @Test
    void  divide_shouldReturn200AndCorrectResponse() throws Exception {

        when(service.divide(21, 3))
                .thenReturn(new CalculationResponse("divide", 21, 3, 7));

        mockMvc.perform(
                get("/api/v1/divide")
                        .param("a", "21")
                        .param("b", "3")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operation").value("divide"))
        .andExpect(jsonPath("$.a").value(21))
        .andExpect(jsonPath("$.b").value(3))
        .andExpect(jsonPath("$.result").value(7));
    }

    @Test
    void divideByZero_shouldReturn400() throws Exception {

        when(service.divide(2, 0))
                .thenThrow(new DivisionByZeroException());

        mockMvc.perform(
                get("/api/v1/divide")
                        .param("a", "2")
                        .param("b", "0")
        )
        .andExpect(status().isBadRequest());
    }

        @Test
        void divideByZero_shouldReturnErrorResponse() throws Exception {
                when(service.divide(2, 0)).thenThrow(new DivisionByZeroException());

                mockMvc.perform(get("/api/v1/divide").param("a", "2").param("b", "0"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("DIVISION_BY_ZERO"))
                                .andExpect(jsonPath("$.message").value("Cannot divide by zero"));
        }

        @Test
        void add_shouldAcceptDecimalValues() throws Exception {
                when(service.add(2.5, 1.25))
                                .thenReturn(new CalculationResponse("add", 2.5, 1.25, 3.75));

                mockMvc.perform(get("/api/v1/add").param("a", "2.5").param("b", "1.25"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result").value(3.75));
        }

        @Test
        void add_shouldReturnBadRequestWhenParameterIsMissing() throws Exception {
                mockMvc.perform(get("/api/v1/add").param("a", "2"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void add_shouldReturnBadRequestWhenParameterIsNotNumeric() throws Exception {
                mockMvc.perform(get("/api/v1/add").param("a", "two").param("b", "3"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void response_shouldContainOnlyExpectedCalculationFields() throws Exception {
                when(service.add(2, 3)).thenReturn(new CalculationResponse("add", 2, 3, 5));

                mockMvc.perform(get("/api/v1/add").param("a", "2").param("b", "3"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.operation").value("add"))
                                .andExpect(jsonPath("$.a").value(2))
                                .andExpect(jsonPath("$.b").value(3))
                                .andExpect(jsonPath("$.result").value(5))
                                .andExpect(jsonPath("$.error").doesNotExist())
                                .andExpect(jsonPath("$.message").doesNotExist());
        }
    
}