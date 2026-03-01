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
    private Long crustTypeId;
    private String sauceName;
    private Long[] toppingIdsFull;
    private Long[] toppingIdsLeft;
    private Long[] toppingIdsRight;
    private int quantity;
    private Double price;

    public double getLineTotal() {
        return price != null ? Math.round(price * quantity * 100.0) / 100.0 : 0.0;
    }
}
