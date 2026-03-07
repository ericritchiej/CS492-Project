package com.pizzastore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDto {
    private String deliveryMethod;
    private String deliveryAddress;
    private Long addressId;
}
