package com.pizzastore.controller;

import com.pizzastore.model.PizzaSize;
import com.pizzastore.repository.PizzaSizeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({PizzaSizeController.class, GlobalExceptionHandler.class})
public class PizzaSizeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PizzaSizeRepository pizzaSizeRepository;

    private PizzaSize buildSize() {
        return new PizzaSize(1L, "Medium", new BigDecimal("10.99"));
    }

    // --- GET /api/pizzaSize/getPizzaSizes ---

    @Test
    public void getPizzaSizes_returnsOk() throws Exception {
        when(pizzaSizeRepository.findAll()).thenReturn(List.of(buildSize()));

        mockMvc.perform(get("/api/pizzaSize/getPizzaSizes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sizeId").value(1))
                .andExpect(jsonPath("$[0].sizeName").value("Medium"))
                .andExpect(jsonPath("$[0].price").value(10.99));
    }

    @Test
    public void getPizzaSizes_returnsMultipleSizes() throws Exception {
        PizzaSize small  = new PizzaSize(1L, "Small",  new BigDecimal("8.99"));
        PizzaSize medium = new PizzaSize(2L, "Medium", new BigDecimal("10.99"));
        PizzaSize large  = new PizzaSize(3L, "Large",  new BigDecimal("13.99"));
        when(pizzaSizeRepository.findAll()).thenReturn(List.of(small, medium, large));

        mockMvc.perform(get("/api/pizzaSize/getPizzaSizes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].sizeName").value("Small"))
                .andExpect(jsonPath("$[1].sizeName").value("Medium"))
                .andExpect(jsonPath("$[2].sizeName").value("Large"));
    }

    @Test
    public void getPizzaSizes_returnsOkWithEmptyList() throws Exception {
        when(pizzaSizeRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/pizzaSize/getPizzaSizes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void getPizzaSizes_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(pizzaSizeRepository).findAll();

        mockMvc.perform(get("/api/pizzaSize/getPizzaSizes"))
                .andExpect(status().isInternalServerError());
    }

    // --- POST /api/pizzaSize/add ---

    @Test
    public void addPizzaSize_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/pizzaSize/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sizeName\":\"Medium\",\"price\":10.99}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void addPizzaSize_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(pizzaSizeRepository).insertNewPizzaSize(any(PizzaSize.class));

        mockMvc.perform(post("/api/pizzaSize/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sizeName\":\"Medium\",\"price\":10.99}"))
                .andExpect(status().isInternalServerError());
    }

    // --- DELETE /api/pizzaSize/delete/{id} ---

    @Test
    public void deletePizzaSize_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/pizzaSize/delete/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deletePizzaSize_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(pizzaSizeRepository).deletePizzaSize(1L);

        mockMvc.perform(delete("/api/pizzaSize/delete/1"))
                .andExpect(status().isInternalServerError());
    }

    // --- PUT /api/pizzaSize/update/{id} ---

    @Test
    public void updatePizzaSize_returnsNoContent() throws Exception {
        mockMvc.perform(put("/api/pizzaSize/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sizeId\":1,\"sizeName\":\"Large\",\"price\":13.99}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updatePizzaSize_returnsBadRequestWhenIdsMismatch() throws Exception {
        mockMvc.perform(put("/api/pizzaSize/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sizeId\":99,\"sizeName\":\"Large\",\"price\":13.99}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updatePizzaSize_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(pizzaSizeRepository).updatePizzaSize(any(PizzaSize.class));

        mockMvc.perform(put("/api/pizzaSize/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sizeId\":1,\"sizeName\":\"Large\",\"price\":13.99}"))
                .andExpect(status().isInternalServerError());
    }
}