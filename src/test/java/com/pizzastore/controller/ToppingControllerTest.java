package com.pizzastore.controller;

import com.pizzastore.model.Topping;
import com.pizzastore.repository.ToppingRepository;
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

@WebMvcTest({ToppingController.class, GlobalExceptionHandler.class})
public class ToppingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ToppingRepository toppingRepository;

    private Topping buildTopping() {
        return new Topping(1L, "Mozzarella", new BigDecimal("0.99"));
    }

    // --- GET /api/topping/getToppings ---

    @Test
    public void getToppings_returnsOk() throws Exception {
        when(toppingRepository.findAll()).thenReturn(List.of(buildTopping()));

        mockMvc.perform(get("/api/topping/getToppings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].toppingId").value(1))
                .andExpect(jsonPath("$[0].toppingName").value("Mozzarella"))
                .andExpect(jsonPath("$[0].extraCost").value(0.99));
    }

    @Test
    public void getToppings_returnsMultipleToppings() throws Exception {
        Topping t1 = new Topping(1L, "Mozzarella",  new BigDecimal("0.99"));
        Topping t2 = new Topping(2L, "Pepperoni",   new BigDecimal("1.49"));
        Topping t3 = new Topping(3L, "Mushrooms",   new BigDecimal("0.75"));
        when(toppingRepository.findAll()).thenReturn(List.of(t1, t2, t3));

        mockMvc.perform(get("/api/topping/getToppings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].toppingName").value("Mozzarella"))
                .andExpect(jsonPath("$[1].toppingName").value("Pepperoni"))
                .andExpect(jsonPath("$[2].toppingName").value("Mushrooms"));
    }

    @Test
    public void getToppings_returnsOkWithEmptyList() throws Exception {
        when(toppingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/topping/getToppings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void getToppings_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(toppingRepository).findAll();

        mockMvc.perform(get("/api/topping/getToppings"))
                .andExpect(status().isInternalServerError());
    }

    // --- POST /api/topping/add ---

    @Test
    public void addTopping_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/topping/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toppingName\":\"Mozzarella\",\"extraCost\":0.99}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void addTopping_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(toppingRepository).insertNewTopping(any(Topping.class));

        mockMvc.perform(post("/api/topping/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toppingName\":\"Mozzarella\",\"extraCost\":0.99}"))
                .andExpect(status().isInternalServerError());
    }

    // --- DELETE /api/topping/delete/{id} ---

    @Test
    public void deleteTopping_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/topping/delete/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteTopping_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(toppingRepository).deleteTopping(1L);

        mockMvc.perform(delete("/api/topping/delete/1"))
                .andExpect(status().isInternalServerError());
    }

    // --- PUT /api/topping/update/{id} ---

    @Test
    public void updateTopping_returnsNoContent() throws Exception {
        mockMvc.perform(put("/api/topping/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toppingId\":1,\"toppingName\":\"Pepperoni\",\"extraCost\":1.49}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateTopping_returnsBadRequestWhenIdsMismatch() throws Exception {
        mockMvc.perform(put("/api/topping/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toppingId\":99,\"toppingName\":\"Pepperoni\",\"extraCost\":1.49}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateTopping_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(toppingRepository).updateTopping(any(Topping.class));

        mockMvc.perform(put("/api/topping/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toppingId\":1,\"toppingName\":\"Pepperoni\",\"extraCost\":1.49}"))
                .andExpect(status().isInternalServerError());
    }
}