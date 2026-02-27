package com.pizzastore.repository;

import com.pizzastore.model.CartItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class CartRepository {

    private final List<CartItem> cartItems = new ArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public List<CartItem> findAll() {
        return cartItems;
    }

    private static final Logger logger = LoggerFactory.getLogger(CartRepository.class);

    public CartItem addItem(CartItem newItem) {
        logger.info("Adding item to cart {}", newItem);

        Optional<CartItem> existing = newItem.getProductId() == null
                ? Optional.empty()
                : cartItems.stream()
                        .filter(i -> Objects.equals(i.getProductId(), newItem.getProductId()))
                        .findFirst();

        if (existing.isPresent()) {
            logger.info("existing product id {}", existing.get().getProductId());
            existing.get().setQuantity(existing.get().getQuantity() + newItem.getQuantity());
            return existing.get();
        } else {
            logger.info("new product id {}", newItem.getProductId());
            newItem.setCartItemId(idSequence.getAndIncrement());
            cartItems.add(newItem);
            return newItem;
        }
    }

    public double getTotal() {
        logger.info("Calculating total of cart items");
        return cartItems.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
    }

    public boolean updateQuantity(Long cartItemId, int quantity) {
        logger.info("Updating quantity for cart item {} quantity {}", cartItemId, quantity);
        Optional<CartItem> existing = cartItems.stream()
                .filter(i -> Objects.equals(i.getCartItemId(), cartItemId))
                .findFirst();

        if (existing.isPresent()) {
            if (quantity <= 0) {
                cartItems.remove(existing.get());
            } else {
                existing.get().setQuantity(quantity);
            }
            return true;
        }
        return false;
    }
}
