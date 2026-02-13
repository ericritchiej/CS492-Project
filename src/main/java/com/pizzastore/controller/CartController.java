package com.pizzastore.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CartController {

    @GetMapping("/cart")
    public Map<String, Object> getCart() {
        Map<String, Object> cart = new HashMap<>();
        cart.put("items", Arrays.asList(
            cartItem("Margherita", 2, 12.99),
            cartItem("Pepperoni", 1, 14.99)
        ));
        cart.put("total", 40.97);
        return cart;
    }

    private Map<String, Object> cartItem(String name, int quantity, double price) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("quantity", quantity);
        item.put("price", price);
        return item;
    }
}
