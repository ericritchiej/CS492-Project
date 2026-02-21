package com.pizzastore.controller;

import com.pizzastore.model.Promotion;
import com.pizzastore.repository.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({PromotionController.class, GlobalExceptionHandler.class})
public class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionRepository promotionRepository;

    private Promotion buildPromo() {
        Promotion promo = new Promotion();
        promo.setPromotionId(1L);
        promo.setCode("SAVE10");
        promo.setDiscountValue(10.00);
        promo.setPromotionSummary("10% Off Your Order");
        promo.setPromotionDesc("Get 10% off any order over $20");
        promo.setExpDt(LocalDate.parse("2026-12-31"));
        promo.setMinOrderAmt(20.00);
        return promo;
    }

    // --- GET /api/restaurant-info/promotions ---

    @Test
    public void getPromotions_returnsActiveList() throws Exception {
        when(promotionRepository.findAllPromotions()).thenReturn(List.of(buildPromo()));

        mockMvc.perform(get("/api/restaurant-info/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SAVE10"))
                .andExpect(jsonPath("$[0].discountValue").value(10.00))
                .andExpect(jsonPath("$[0].promotionSummary").value("10% Off Your Order"));
    }

    @Test
    public void getPromotions_returnsEmptyListWhenNoneActive() throws Exception {
        when(promotionRepository.findAllPromotions()).thenReturn(List.of());

        mockMvc.perform(get("/api/restaurant-info/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void getPromotions_returns500WhenRepositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(promotionRepository).findAllPromotions();

        mockMvc.perform(get("/api/restaurant-info/promotions"))
                .andExpect(status().isInternalServerError());
    }
}
