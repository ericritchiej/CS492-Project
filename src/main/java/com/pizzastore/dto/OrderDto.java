package com.pizzastore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long orderId;
    private Instant placedAt;
    private String status;
    private String deliveryMethod;
    private List<OrderItemDto> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private String promoCode;
    private BigDecimal tax;
    private BigDecimal total;
}