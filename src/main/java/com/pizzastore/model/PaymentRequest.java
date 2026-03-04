package com.pizzastore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String cardNumber;
    private String expirationDate;
    private String cvv;
    private String deliveryMethod; // "DELIVERY" or "PICKUP"
}