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

    private static final Logger logger = LoggerFactory.getLogger(CartRepository.class);

    private final List<CartItem> cartItems = new ArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    private String appliedPromoCode = null;
    private double appliedDiscount  = 0.0;

    public void applyPromo(String code, double discount) {
        this.appliedPromoCode = code;
        this.appliedDiscount  = discount;
    }

    public void clearPromo() {
        this.appliedPromoCode = null;
        this.appliedDiscount  = 0.0;
    }

    public String getAppliedPromoCode() { return appliedPromoCode; }
    public double getAppliedDiscount()  { return appliedDiscount; }

    public List<CartItem> findAll() {
        return List.copyOf(cartItems);
    }

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


    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public void clearCart() {
        cartItems.clear();
        clearPromo();
    }

    public double getTotal() {
        logger.debug("Calculating total of cart items");
        return cartItems.stream()
                .mapToDouble(i -> i.getPrice() != null ? i.getPrice() * i.getQuantity() : 0.0)
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
