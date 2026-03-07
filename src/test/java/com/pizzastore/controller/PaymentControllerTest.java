package com.pizzastore.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({PaymentController.class, GlobalExceptionHandler.class})
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String VALID_PAYMENT_JSON =
            "{\"cardNumber\":\"4111111111111111\",\"expirationDate\":\"12/27\",\"cvv\":\"123\",\"deliveryMethod\":\"DELIVERY\"}";

    // --- POST /api/payment/process ---

    @Test
    public void processPayment_returnsOkWithConfirmationNumber() throws Exception {
        mockMvc.perform(post("/api/payment/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYMENT_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Payment processed successfully!"))
                .andExpect(jsonPath("$.confirmationNumber").isNotEmpty())
                .andExpect(jsonPath("$.deliveryMethod").value("DELIVERY"));
    }

    @Test
    public void processPayment_returnsOkWithPickupDeliveryMethod() throws Exception {
        String json = "{\"cardNumber\":\"4111111111111111\",\"expirationDate\":\"12/27\",\"cvv\":\"123\",\"deliveryMethod\":\"PICKUP\"}";

        mockMvc.perform(post("/api/payment/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryMethod").value("PICKUP"));
    }

    @Test
    public void processPayment_returnsBadRequestWhenCardNumberMissing() throws Exception {
        String json = "{\"expirationDate\":\"12/27\",\"cvv\":\"123\",\"deliveryMethod\":\"DELIVERY\"}";

        mockMvc.perform(post("/api/payment/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("All payment fields are required."));
    }

    @Test
    public void processPayment_returnsBadRequestWhenCardNumberBlank() throws Exception {
        String json = "{\"cardNumber\":\"\",\"expirationDate\":\"12/27\",\"cvv\":\"123\",\"deliveryMethod\":\"DELIVERY\"}";

        mockMvc.perform(post("/api/payment/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("All payment fields are required."));
    }

    @Test
    public void processPayment_returnsBadRequestWhenExpirationDateMissing() throws Exception {
        String json = "{\"cardNumber\":\"4111111111111111\",\"cvv\":\"123\",\"deliveryMethod\":\"DELIVERY\"}";

        mockMvc.perform(post("/api/payment/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("All payment fields are required."));
    }

    @Test
    public void processPayment_returnsBadRequestWhenCvvMissing() throws Exception {
        String json = "{\"cardNumber\":\"4111111111111111\",\"expirationDate\":\"12/27\",\"deliveryMethod\":\"DELIVERY\"}";

        mockMvc.perform(post("/api/payment/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("All payment fields are required."));
    }

    @Test
    public void processPayment_returnsBadRequestWhenDeliveryMethodMissing() throws Exception {
        String json = "{\"cardNumber\":\"4111111111111111\",\"expirationDate\":\"12/27\",\"cvv\":\"123\"}";

        mockMvc.perform(post("/api/payment/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("All payment fields are required."));
    }

    @Test
    public void processPayment_returnsBadRequestWhenDeliveryMethodBlank() throws Exception {
        String json = "{\"cardNumber\":\"4111111111111111\",\"expirationDate\":\"12/27\",\"cvv\":\"123\",\"deliveryMethod\":\"   \"}";

        mockMvc.perform(post("/api/payment/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("All payment fields are required."));
    }
}