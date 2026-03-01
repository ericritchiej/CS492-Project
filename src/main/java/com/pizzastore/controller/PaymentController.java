package com.pizzastore.controller;

import com.pizzastore.model.PaymentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody PaymentRequest request) {
        // Validate required fields
        if (request.getCardNumber() == null || request.getCardNumber().isBlank() ||
                request.getExpirationDate() == null || request.getExpirationDate().isBlank() ||
                request.getCvv() == null || request.getCvv().isBlank() ||
                request.getDeliveryMethod() == null || request.getDeliveryMethod().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "All payment fields are required."));
        }

        // Generate a confirmation number
        String confirmationNumber = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Payment processed successfully!");
        response.put("confirmationNumber", confirmationNumber);
        response.put("deliveryMethod", request.getDeliveryMethod());

        return ResponseEntity.ok(response);
    }
}
