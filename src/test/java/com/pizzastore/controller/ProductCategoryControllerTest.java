package com.pizzastore.controller;

import com.pizzastore.model.ProductCategory;
import com.pizzastore.repository.ProductCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ProductCategoryController.class, GlobalExceptionHandler.class})
public class ProductCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductCategoryRepository productCategoryRepository;

    private ProductCategory buildCategory() {
        return new ProductCategory(1L, "Pizzas");
    }

    // --- GET /api/productCategory/getProductCategories ---

    @Test
    public void getProductCategories_returnsOk() throws Exception {
        when(productCategoryRepository.findAll()).thenReturn(List.of(buildCategory()));

        mockMvc.perform(get("/api/productCategory/getProductCategories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Pizzas"));
    }

    @Test
    public void getProductCategories_returnsMultipleCategories() throws Exception {
        ProductCategory pizzas    = new ProductCategory(1L, "Pizzas");
        ProductCategory sides     = new ProductCategory(2L, "Sides");
        ProductCategory beverages = new ProductCategory(3L, "Beverages");
        when(productCategoryRepository.findAll()).thenReturn(List.of(pizzas, sides, beverages));

        mockMvc.perform(get("/api/productCategory/getProductCategories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].categoryName").value("Pizzas"))
                .andExpect(jsonPath("$[1].categoryName").value("Sides"))
                .andExpect(jsonPath("$[2].categoryName").value("Beverages"));
    }

    @Test
    public void getProductCategories_returnsOkWithEmptyList() throws Exception {
        when(productCategoryRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/productCategory/getProductCategories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void getProductCategories_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(productCategoryRepository).findAll();

        mockMvc.perform(get("/api/productCategory/getProductCategories"))
                .andExpect(status().isInternalServerError());
    }

    // --- POST /api/productCategory/add ---

    @Test
    public void addProductCategory_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/productCategory/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryName\":\"Pizzas\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void addProductCategory_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(productCategoryRepository).insertNewProductCategory(any(ProductCategory.class));

        mockMvc.perform(post("/api/productCategory/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryName\":\"Pizzas\"}"))
                .andExpect(status().isInternalServerError());
    }

    // --- DELETE /api/productCategory/delete/{id} ---

    @Test
    public void deleteProductCategory_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/productCategory/delete/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteProductCategory_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(productCategoryRepository).deleteProductCategory(1L);

        mockMvc.perform(delete("/api/productCategory/delete/1"))
                .andExpect(status().isInternalServerError());
    }

    // --- PUT /api/productCategory/update/{id} ---

    @Test
    public void updateProductCategory_returnsNoContent() throws Exception {
        mockMvc.perform(put("/api/productCategory/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":1,\"categoryName\":\"Specialty Pizzas\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateProductCategory_returnsBadRequestWhenIdsMismatch() throws Exception {
        mockMvc.perform(put("/api/productCategory/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":99,\"categoryName\":\"Specialty Pizzas\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateProductCategory_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(productCategoryRepository).updateProductCategory(any(ProductCategory.class));

        mockMvc.perform(put("/api/productCategory/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":1,\"categoryName\":\"Specialty Pizzas\"}"))
                .andExpect(status().isInternalServerError());
    }
}