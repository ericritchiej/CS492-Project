package com.pizzastore.repository;

import com.pizzastore.model.CartItem;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CartRepository {

    private final List<CartItem> cartItems = new ArrayList<>();

    public List<CartItem> findAll() {
        return cartItems;
    }

    public void addItem(CartItem newItem) {
        Optional<CartItem> existing = cartItems.stream()
                .filter(i -> i.getProductId().equals(newItem.getProductId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + newItem.getQuantity());
        } else {
            cartItems.add(newItem);
        }
    }

    public double getTotal() {
        return cartItems.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
    }
}
