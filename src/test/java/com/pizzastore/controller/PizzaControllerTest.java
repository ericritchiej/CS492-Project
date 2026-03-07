package com.pizzastore.controller;

import com.pizzastore.model.CrustType;
import com.pizzastore.repository.CrustTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({PizzaController.class, GlobalExceptionHandler.class})
public class PizzaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CrustTypeRepository crustTypeRepository;

    // --- GET /api/crust-types ---

    @Test
    public void getCrustTypes_returnsOk() throws Exception {
        when(crustTypeRepository.findAll()).thenReturn(List.of(
                new CrustType(1L, "Thin Crust", new BigDecimal("1.00"))
        ));

        mockMvc.perform(get("/api/crust-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].crustId").value(1))
                .andExpect(jsonPath("$[0].crustName").value("Thin Crust"))
                .andExpect(jsonPath("$[0].price").value(1.00));
    }

    @Test
    public void getCrustTypes_returnsEmptyList() throws Exception {
        when(crustTypeRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/crust-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void getCrustTypes_returnsMultipleCrustTypes() throws Exception {
        when(crustTypeRepository.findAll()).thenReturn(List.of(
                new CrustType(1L, "Thin Crust", new BigDecimal("1.00")),
                new CrustType(2L, "Hand-Tossed", new BigDecimal("2.00")),
                new CrustType(3L, "Deep Dish", new BigDecimal("2.99"))
        ));

        mockMvc.perform(get("/api/crust-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[1].crustName").value("Hand-Tossed"))
                .andExpect(jsonPath("$[2].crustName").value("Deep Dish"));
    }

    @Test
    public void getCrustTypes_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(crustTypeRepository).findAll();

        mockMvc.perform(get("/api/crust-types"))
                .andExpect(status().isInternalServerError());
    }

    // --- GET /api/pizzas ---

    @Test
    public void getPizzas_returnsOk() throws Exception {
        mockMvc.perform(get("/api/pizzas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    public void getPizzas_containsExpectedPizzas() throws Exception {
        mockMvc.perform(get("/api/pizzas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Margherita"))
                .andExpect(jsonPath("$[0].price").value(12.99))
                .andExpect(jsonPath("$[1].name").value("Pepperoni"))
                .andExpect(jsonPath("$[2].name").value("Supreme"))
                .andExpect(jsonPath("$[3].name").value("BBQ Chicken"));
    }

    @Test
    public void getPizzas_eachItemHasRequiredFields() throws Exception {
        mockMvc.perform(get("/api/pizzas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").isNotEmpty())
                .andExpect(jsonPath("$[0].description").isNotEmpty())
                .andExpect(jsonPath("$[0].price").isNumber());
    }

    // --- GET /api/orders ---

    @Test
    public void getOrders_returnsOk() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void getOrders_containsExpectedOrders() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1042))
                .andExpect(jsonPath("$[0].status").value("Delivered"))
                .andExpect(jsonPath("$[1].id").value(1043))
                .andExpect(jsonPath("$[1].status").value("In Progress"));
    }

    @Test
    public void getOrders_eachItemHasRequiredFields() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].items").isNotEmpty())
                .andExpect(jsonPath("$[0].total").isNumber())
                .andExpect(jsonPath("$[0].status").isNotEmpty());
    }

    // --- GET /api/stats ---

    @Test
    public void getStats_returnsOk() throws Exception {
        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk());
    }

    @Test
    public void getStats_containsExpectedFields() throws Exception {
        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordersToday").value(127))
                .andExpect(jsonPath("$.revenueToday").value(3842.0))
                .andExpect(jsonPath("$.menuItems").value(12));
    }
}