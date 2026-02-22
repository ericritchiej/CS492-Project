package com.pizzastore.controller;

import com.pizzastore.model.CrustType;
import com.pizzastore.repository.CrustTypeRepository;
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

@WebMvcTest({CrustTypeController.class, GlobalExceptionHandler.class})
public class CrustTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CrustTypeRepository crustTypeRepository;

    private CrustType buildCrust() {
        return new CrustType(1L, "Thin Crust", new BigDecimal("1.00"));
    }

    // --- GET /api/crust/getCrusts ---

    @Test
    public void getCrusts_returnsOk() throws Exception {
        when(crustTypeRepository.findAll()).thenReturn(List.of(buildCrust()));

        mockMvc.perform(get("/api/crust/getCrusts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].crustId").value(1))
                .andExpect(jsonPath("$[0].crustName").value("Thin Crust"))
                .andExpect(jsonPath("$[0].price").value(1.00));
    }

    @Test
    public void getCrusts_returnsMultipleCrusts() throws Exception {
        CrustType crust1 = new CrustType(1L, "Thin Crust", new BigDecimal("1.00"));
        CrustType crust2 = new CrustType(2L, "Hand-Tossed", new BigDecimal("2.00"));
        CrustType crust3 = new CrustType(3L, "Deep Dish", new BigDecimal("2.99"));
        when(crustTypeRepository.findAll()).thenReturn(List.of(crust1, crust2, crust3));

        mockMvc.perform(get("/api/crust/getCrusts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[1].crustName").value("Hand-Tossed"))
                .andExpect(jsonPath("$[2].crustName").value("Deep Dish"));
    }

    @Test
    public void getCrusts_returnsOkWithEmptyList() throws Exception {
        when(crustTypeRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/crust/getCrusts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void getCrusts_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(crustTypeRepository).findAll();

        mockMvc.perform(get("/api/crust/getCrusts"))
                .andExpect(status().isInternalServerError());
    }

    // --- POST /api/crust/add ---

    @Test
    public void addCrust_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/crust/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"crustName\":\"Thin Crust\",\"price\":1.00}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void addCrust_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(crustTypeRepository).insertNewCrustType(any(CrustType.class));

        mockMvc.perform(post("/api/crust/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"crustName\":\"Thin Crust\",\"price\":1.00}"))
                .andExpect(status().isInternalServerError());
    }

    // --- DELETE /api/crust/delete/{id} ---

    @Test
    public void deleteCrust_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/crust/delete/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteCrust_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(crustTypeRepository).deleteCrustType(1L);

        mockMvc.perform(delete("/api/crust/delete/1"))
                .andExpect(status().isInternalServerError());
    }

    // --- PUT /api/crust/update/{id} ---

    @Test
    public void updateCrust_returnsNoContent() throws Exception {
        mockMvc.perform(put("/api/crust/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"crustId\":1,\"crustName\":\"Hand-Tossed\",\"price\":2.00}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateCrust_returnsBadRequestWhenIdsMismatch() throws Exception {
        mockMvc.perform(put("/api/crust/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"crustId\":99,\"crustName\":\"Hand-Tossed\",\"price\":2.00}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateCrust_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(crustTypeRepository).updateCrustType(any(CrustType.class));

        mockMvc.perform(put("/api/crust/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"crustId\":1,\"crustName\":\"Hand-Tossed\",\"price\":2.00}"))
                .andExpect(status().isInternalServerError());
    }
}
