package com.pizzastore.controller;

import com.pizzastore.repository.PromotionsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {PromotionsController.class, GlobalExceptionHandler.class},
            excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class PromotionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionsRepository promotionsRepository;

    private static final String VALID_BODY = """
            {
              "code": "SAVE10",
              "discount_value": "10.00",
              "promotion_desc": "10 percent off",
              "promotion_summary": "Save 10",
              "exp_dt": "2027-01-01",
              "min_order_amt": "15.00"
            }
            """;

    // --- GET /api/promotions ---

    @Test
    public void getPromotions_returnsListFromRepository() throws Exception {
        when(promotionsRepository.findAll())
                .thenReturn(List.of(Map.of("code", "SAVE10", "discount_value", 10.00)));

        mockMvc.perform(get("/api/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SAVE10"));
    }

    @Test
    public void getPromotions_returnsEmptyList() throws Exception {
        when(promotionsRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void getPromotions_returns500WhenRepositoryThrows() throws Exception {
        when(promotionsRepository.findAll()).thenThrow(new RuntimeException("DB down"));

        mockMvc.perform(get("/api/promotions"))
                .andExpect(status().isInternalServerError());
    }

    // --- POST /api/promotions ---

    @Test
    public void createPromotion_validBody_returnsSuccess() throws Exception {
        when(promotionsRepository.createPromotion(any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        mockMvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Promotion added successfully."));
    }

    @Test
    public void createPromotion_missingField_returnsBadRequest() throws Exception {
        String body = """
                {
                  "code": "SAVE10",
                  "discount_value": "10.00"
                }
                """;

        mockMvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("All fields are required."));
    }

    @Test
    public void createPromotion_nonNumericDiscount_returnsBadRequest() throws Exception {
        String body = """
                {
                  "code": "SAVE10",
                  "discount_value": "abc",
                  "promotion_desc": "10 percent off",
                  "promotion_summary": "Save 10",
                  "exp_dt": "2027-01-01",
                  "min_order_amt": "15.00"
                }
                """;

        mockMvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Discount must be numeric."));
    }

    @Test
    public void createPromotion_nonNumericMinOrderAmt_returnsBadRequest() throws Exception {
        String body = """
                {
                  "code": "SAVE10",
                  "discount_value": "10.00",
                  "promotion_desc": "10 percent off",
                  "promotion_summary": "Save 10",
                  "exp_dt": "2027-01-01",
                  "min_order_amt": "xyz"
                }
                """;

        mockMvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Minimum order amount must be numeric."));
    }

    @Test
    public void createPromotion_invalidDateFormat_returnsBadRequest() throws Exception {
        String body = """
                {
                  "code": "SAVE10",
                  "discount_value": "10.00",
                  "promotion_desc": "10 percent off",
                  "promotion_summary": "Save 10",
                  "exp_dt": "01-01-2027",
                  "min_order_amt": "15.00"
                }
                """;

        mockMvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Expiration date must be in YYYY-MM-DD format."));
    }

    @Test
    public void createPromotion_repositoryReturnsFalse_returnsBadRequest() throws Exception {
        when(promotionsRepository.createPromotion(any(), any(), any(), any(), any(), any()))
                .thenReturn(false);

        mockMvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to add promotion."));
    }

    @Test
    public void createPromotion_repositoryThrows_returns500() throws Exception {
        when(promotionsRepository.createPromotion(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to add promotion due to a server error."));
    }

    // --- PUT /api/promotions/{id} ---

    @Test
    public void updatePromotion_validBody_returnsSuccess() throws Exception {
        when(promotionsRepository.updatePromotion(eq(1L), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        mockMvc.perform(put("/api/promotions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Promotion updated successfully."));
    }

    @Test
    public void updatePromotion_notFound_returnsBadRequest() throws Exception {
        when(promotionsRepository.updatePromotion(eq(99L), any(), any(), any(), any(), any(), any()))
                .thenReturn(false);

        mockMvc.perform(put("/api/promotions/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Update failed. Promotion not found or no changes saved."));
    }

    @Test
    public void updatePromotion_missingField_returnsBadRequest() throws Exception {
        String body = """
                {
                  "code": "SAVE10"
                }
                """;

        mockMvc.perform(put("/api/promotions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("All fields are required."));
    }

    @Test
    public void updatePromotion_repositoryThrows_returns500() throws Exception {
        when(promotionsRepository.updatePromotion(eq(1L), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(put("/api/promotions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Update failed due to a server error."));
    }

    // --- DELETE /api/promotions/{id} ---

    @Test
    public void deletePromotion_success_returnsOk() throws Exception {
        when(promotionsRepository.deletePromotion(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/promotions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Promotion deleted successfully."));
    }

    @Test
    public void deletePromotion_notFound_returnsBadRequest() throws Exception {
        when(promotionsRepository.deletePromotion(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/promotions/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Delete failed. Promotion not found."));
    }

    @Test
    public void deletePromotion_repositoryThrows_returns500() throws Exception {
        when(promotionsRepository.deletePromotion(1L)).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(delete("/api/promotions/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Delete failed due to a server error."));
    }
}