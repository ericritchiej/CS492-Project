package com.pizzastore.controller;

import com.pizzastore.model.Promotion;
import com.pizzastore.repository.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromotionController.class)
public class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionRepository promotionRepository;

    @Test
    public void shouldReturnPromotionsList() throws Exception {
        // Arrange - create fake data
        Promotion promo = new Promotion();
        promo.setPromotion_id(1L);
        promo.setCode("SAVE10");
        promo.setDiscount_value(10.00);
        promo.setPromotion_summary("10% Off Your Order");
        promo.setPromotion_desc("Get 10% off any order over $20");
        promo.setExp_dt(Date.valueOf("2026-12-31"));
        promo.setMin_order_amt(20.00);

        when(promotionRepository.findByAllPromotions()).thenReturn(List.of(promo));

        // Act & Assert
        mockMvc.perform(get("/api/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SAVE10"))
                .andExpect(jsonPath("$[0].discount_value").value(10.00))
                .andExpect(jsonPath("$[0].promotion_summary").value("10% Off Your Order"));
    }

    @Test
    public void shouldReturnEmptyListWhenNoPromotions() throws Exception {
        when(promotionRepository.findByAllPromotions()).thenReturn(List.of());

        mockMvc.perform(get("/api/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
