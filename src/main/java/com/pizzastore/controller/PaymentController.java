package com.pizzastore.controller;

import com.pizzastore.model.Payment;
import com.pizzastore.model.PaymentRequest;
import com.pizzastore.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody PaymentRequest request) {
        logger.info("processPayment {}", request);
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

    public void savePayment(Long orderId, Long addressId, String cardNumber, String cvv, String expDt) {
        String maskedCard = cardNumber != null && cardNumber.length() > 4
                ? "**** **** **** " + cardNumber.substring(cardNumber.length() - 4)
                : "****";
        logger.info("savePayment orderId={} addressId={} card={}", orderId, addressId, maskedCard);

        if (expDt == null || !expDt.contains("/")) {
            throw new IllegalArgumentException("Expiration date must be in MM/YY format.");
        }
        if (cvv == null || cvv.isBlank()) {
            throw new IllegalArgumentException("CVV is required.");
        }

        String[] parts = expDt.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Expiration date must be in MM/YY format.");
        }
        Long expMonth, expYear;
        try {
            expMonth = Long.parseLong(parts[0].trim());
            expYear  = Long.parseLong(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Expiration date must be in MM/YY format.");
        }

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAddressId(addressId);
        payment.setCcNumber(cardNumber);
        payment.setCcvNumber(cvv);
        payment.setExpMonth(expMonth);
        payment.setExpYear(expYear);

        paymentRepository.insertNewPayment(payment);
    }
}
