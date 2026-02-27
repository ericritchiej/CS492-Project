package com.pizzastore.controller;

import com.pizzastore.model.CartItem;
import com.pizzastore.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartRepository cartRepository;

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    public CartController(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @GetMapping
    public Map<String, Object> getCart() {
        logger.info("getCart called");
        Map<String, Object> cart = new HashMap<>();
        cart.put("items", cartRepository.findAll());
        cart.put("total", cartRepository.getTotal());
        return cart;
    }

    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(@RequestBody CartItem item) {
        logger.info("Adding to cart item {}", item);
        CartItem saved = cartRepository.addItem(item);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateQuantity(@RequestParam Long cartItemId, @RequestParam int quantity) {
        logger.info("Updating quantity for cart item {}", cartItemId);
        boolean updated = cartRepository.updateQuantity(cartItemId, quantity);
        if (updated) {
            return ResponseEntity.ok("Quantity updated");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
