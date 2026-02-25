package com.pizzastore.controller;

import com.pizzastore.model.CartItem;
import com.pizzastore.repository.CartRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartRepository cartRepository;

    public CartController(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @GetMapping
    public Map<String, Object> getCart() {
        Map<String, Object> cart = new HashMap<>();
        cart.put("items", cartRepository.findAll());
        cart.put("total", cartRepository.getTotal());
        return cart;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestBody CartItem item) {
        cartRepository.addItem(item);
        return ResponseEntity.ok("Item added to cart");
    }
}
