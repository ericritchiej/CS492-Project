package com.pizzastore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private Long cartItemId;
    private String name;
    private Integer quantity;
    private BigDecimal lineTotal;
    private Integer sizeId;
    private String sizeName;
    private Integer crustTypeId;
    private String crustName;
    private String sauceName;
    private List<Integer> toppingIdsFull;
    private List<Integer> toppingIdsLeft;
    private List<Integer> toppingIdsRight;
}