package com.pizzastore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Long cartItemId;
    private Long productId;
    private String name;
    private Long sizeId;
    private long crustTypeId;
    private String sauceName;
    private long[] toppingIdsFull;
    private long[] toppingIdsLeft;
    private long[] toppingIdsRight;
    private int quantity;
    private Double price;
}
