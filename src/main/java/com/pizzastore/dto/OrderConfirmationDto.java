package com.pizzastore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmationDto {
    private Long orderId;
    private String status;
    private String deliveryMethod;
    private BigDecimal total;
    private String message;
}
