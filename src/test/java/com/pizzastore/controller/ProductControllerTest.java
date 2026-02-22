package com.pizzastore.controller;

import com.pizzastore.model.Product;
import com.pizzastore.repository.ProductRepository;
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

@WebMvcTest({ProductController.class, GlobalExceptionHandler.class})
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    private Product buildProduct() {
        return new Product(1L, 1L, "Margherita", new BigDecimal("12.99"), true);
    }

    // --- GET /api/product/getProducts ---

    @Test
    public void getProducts_returnsOk() throws Exception {
        when(productRepository.findAll()).thenReturn(List.of(buildProduct()));

        mockMvc.perform(get("/api/product/getProducts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].productName").value("Margherita"))
                .andExpect(jsonPath("$[0].basePrice").value(12.99))
                .andExpect(jsonPath("$[0].customizable").value(true));
    }

    @Test
    public void getProducts_returnsMultipleProducts() throws Exception {
        Product p1 = new Product(1L, 1L, "Margherita",  new BigDecimal("12.99"), true);
        Product p2 = new Product(2L, 1L, "Pepperoni",   new BigDecimal("14.99"), true);
        Product p3 = new Product(3L, 2L, "Garlic Bread", new BigDecimal("4.99"), false);
        when(productRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        mockMvc.perform(get("/api/product/getProducts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].productName").value("Margherita"))
                .andExpect(jsonPath("$[1].productName").value("Pepperoni"))
                .andExpect(jsonPath("$[2].productName").value("Garlic Bread"))
                .andExpect(jsonPath("$[2].customizable").value(false));
    }

    @Test
    public void getProducts_returnsOkWithEmptyList() throws Exception {
        when(productRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/product/getProducts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void getProducts_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(productRepository).findAll();

        mockMvc.perform(get("/api/product/getProducts"))
                .andExpect(status().isInternalServerError());
    }

    // --- POST /api/product/add ---

    @Test
    public void addProduct_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/product/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":1,\"productName\":\"Margherita\",\"basePrice\":12.99,\"customizable\":true}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void addProduct_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(productRepository).insertNewProduct(any(Product.class));

        mockMvc.perform(post("/api/product/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":1,\"productName\":\"Margherita\",\"basePrice\":12.99,\"customizable\":true}"))
                .andExpect(status().isInternalServerError());
    }

    // --- DELETE /api/product/delete/{id} ---

    @Test
    public void deleteProduct_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/product/delete/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteProduct_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(productRepository).deleteProduct(1L);

        mockMvc.perform(delete("/api/product/delete/1"))
                .andExpect(status().isInternalServerError());
    }

    // --- PUT /api/product/update/{id} ---

    @Test
    public void updateProduct_returnsNoContent() throws Exception {
        mockMvc.perform(put("/api/product/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"categoryId\":1,\"productName\":\"Pepperoni\",\"basePrice\":14.99,\"customizable\":true}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateProduct_returnsBadRequestWhenIdsMismatch() throws Exception {
        mockMvc.perform(put("/api/product/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":99,\"categoryId\":1,\"productName\":\"Pepperoni\",\"basePrice\":14.99,\"customizable\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateProduct_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(productRepository).updateProduct(any(Product.class));

        mockMvc.perform(put("/api/product/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"categoryId\":1,\"productName\":\"Pepperoni\",\"basePrice\":14.99,\"customizable\":true}"))
                .andExpect(status().isInternalServerError());
    }
}