package com.pizzastore.controller;

import com.pizzastore.model.CartItem;
import com.pizzastore.model.Promotion;
import com.pizzastore.repository.CartRepository;
import com.pizzastore.repository.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

record UpdateQuantityRequest(Long cartItemId, int quantity) {}

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartRepository cartRepository;
    private final PromotionRepository promotionRepository;

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    public CartController(CartRepository cartRepository, PromotionRepository promotionRepository) {
        this.cartRepository = cartRepository;
        this.promotionRepository = promotionRepository;
    }

    @GetMapping
    public Map<String, Object> getCart() {
        logger.info("getCart called");
        return buildCartSummary();
    }

    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(@RequestBody CartItem item) {
        logger.info("Adding to cart item {}", item);
        CartItem saved = cartRepository.addItem(item);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateQuantity(@RequestBody UpdateQuantityRequest body) {
        logger.info("Updating quantity for cart item {}", body.cartItemId());
        boolean updated = cartRepository.updateQuantity(body.cartItemId(), body.quantity());
        if (updated) {
            logger.info("updated cart item {}", body.cartItemId());
            return ResponseEntity.ok(Map.of("message", "Quantity updated"));
        } else {
            logger.info("cart item {} not updated", body.cartItemId());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/promo")
    public ResponseEntity<Map<String, Object>> applyPromo(@RequestParam String code) {
        logger.info("Applying promo code {}", code);

        Optional<Promotion> promoOpt = promotionRepository.findByCode(code);
        if (promoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Promo code not found."));
        }

        Promotion promo = promoOpt.get();

        if (promo.getExpDt() != null && !promo.getExpDt().isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "This promo code has expired."));
        }

        double subtotal = cartRepository.getTotal();
        if (promo.getMinOrderAmt() != null && subtotal < promo.getMinOrderAmt()) {
            return ResponseEntity.badRequest().body(
                Map.of("message", String.format("Minimum order of $%.2f required for this promo.", promo.getMinOrderAmt()))
            );
        }

        cartRepository.applyPromo(promo.getCode(), promo.getDiscountValue());
        logger.info("Promo {} applied, discount {}", promo.getCode(), promo.getDiscountValue());
        return ResponseEntity.ok(buildCartSummary());
    }

    @DeleteMapping("/promo")
    public ResponseEntity<Map<String, Object>> removePromo() {
        logger.info("Removing promo code");
        cartRepository.clearPromo();
        return ResponseEntity.ok(buildCartSummary());
    }

    private Map<String, Object> buildCartSummary() {
        double subtotal  = cartRepository.getTotal();
        double discount  = cartRepository.getAppliedDiscount();
        double taxable   = Math.max(0, subtotal - discount);
        double tax       = Math.round(taxable * 0.08 * 100.0) / 100.0;
        double total     = Math.round((taxable + tax) * 100.0) / 100.0;

        Map<String, Object> cart = new HashMap<>();
        cart.put("items",     cartRepository.findAll());
        cart.put("subtotal",  subtotal);
        cart.put("discount",  discount);
        cart.put("promoCode", cartRepository.getAppliedPromoCode());
        cart.put("tax",       tax);
        cart.put("total",     total);
        return cart;
    }
}