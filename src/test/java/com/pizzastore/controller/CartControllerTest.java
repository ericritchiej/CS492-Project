package com.pizzastore.controller;

import com.pizzastore.model.CartItem;
import com.pizzastore.model.Promotion;
import com.pizzastore.repository.CartRepository;
import com.pizzastore.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CartControllerTest {

    private CartRepository cartRepository;
    private PromotionRepository promotionRepository;
    private CartController cartController;

    @BeforeEach
    void setUp() {
        cartRepository    = new CartRepository();
        promotionRepository = mock(PromotionRepository.class);
        cartController    = new CartController(cartRepository, promotionRepository);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Promotion promo(String code, double discount, double minOrderAmt, LocalDate expDt) {
        Promotion p = new Promotion();
        p.setCode(code);
        p.setDiscountValue(discount);
        p.setMinOrderAmt(minOrderAmt);
        p.setExpDt(expDt);
        return p;
    }

    private CartItem menuItem(long productId, int quantity) {
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setSauceName("REGULAR");
        item.setToppingIdsFull(new Long[]{});
        return item;
    }

    private CartItem customItem(int quantity) {
        CartItem item = new CartItem();
        item.setProductId(null);  // custom pizza has no productId
        item.setQuantity(quantity);
        item.setSauceName("REGULAR");
        item.setToppingIdsFull(new Long[]{1L});
        return item;
    }

    // ── addToCart ─────────────────────────────────────────────────────────────

    @Test
    void addToCart_returnsOkWithCartItemId() {
        ResponseEntity<CartItem> response = cartController.addToCart(menuItem(1L, 1));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCartItemId());
    }

    @Test
    void addToCart_assignsUniqueCartItemIds() {
        CartItem first  = cartController.addToCart(menuItem(1L, 1)).getBody();
        CartItem second = cartController.addToCart(menuItem(2L, 1)).getBody();

        assertNotNull(first);
        assertNotNull(second);
        assertNotEquals(first.getCartItemId(), second.getCartItemId());
    }

    @Test
    void addToCart_duplicateProductId_mergesQuantity() {
        cartController.addToCart(menuItem(1L, 2));
        CartItem merged = cartController.addToCart(menuItem(1L, 3)).getBody();

        assertNotNull(merged);
        assertEquals(5, merged.getQuantity());
        assertEquals(1, cartRepository.findAll().size(), "Should be one entry, not two");
    }

    @Test
    void addToCart_customPizza_alwaysCreatesNewEntry() {
        cartController.addToCart(customItem(1));
        cartController.addToCart(customItem(1));

        assertEquals(2, cartRepository.findAll().size(),
                "Each custom pizza should be a separate cart entry");
    }

    @Test
    void addToCart_customPizzasGetDistinctCartItemIds() {
        CartItem first  = cartController.addToCart(customItem(1)).getBody();
        CartItem second = cartController.addToCart(customItem(1)).getBody();

        assertNotNull(first);
        assertNotNull(second);
        assertNotEquals(first.getCartItemId(), second.getCartItemId());
    }

    // ── getCart ───────────────────────────────────────────────────────────────

    @Test
    void getCart_returnsItemsAndTotal() {
        Map<String, Object> cart = cartController.getCart();

        assertTrue(cart.containsKey("items"));
        assertTrue(cart.containsKey("total"));
    }

    @Test
    void getCart_calculatesSubtotalTaxAndTotal() {
        CartItem item = menuItem(1L, 2);
        item.setPrice(10.0); // 2 × $10 = $20 subtotal
        cartController.addToCart(item);

        Map<String, Object> cart = cartController.getCart();

        assertEquals(20.0,  cart.get("subtotal"));
        assertEquals(1.6,   cart.get("tax"));     // 20 × 0.08 = 1.60
        assertEquals(21.6,  cart.get("total"));
        assertEquals(0.0,   cart.get("discount"));
        assertNull(cart.get("promoCode"));
    }

    @Test
    void getCart_reflectsAppliedPromo() {
        CartItem item = menuItem(1L, 1);
        item.setPrice(20.0);
        cartController.addToCart(item);

        when(promotionRepository.findByCode("SAVE5"))
            .thenReturn(Optional.of(promo("SAVE5", 5.0, 0.0, LocalDate.now().plusDays(30))));
        cartController.applyPromo("SAVE5");

        Map<String, Object> cart = cartController.getCart();

        assertEquals(20.0,   cart.get("subtotal"));
        assertEquals(5.0,    cart.get("discount"));
        assertEquals(1.2,    cart.get("tax"));    // (20 - 5) × 0.08 = 1.20
        assertEquals(16.2,   cart.get("total"));
        assertEquals("SAVE5", cart.get("promoCode"));
    }

    // ── updateQuantity ────────────────────────────────────────────────────────

    @Test
    void updateQuantity_byCartItemId_updatesCorrectItem() {
        CartItem saved = cartController.addToCart(menuItem(1L, 1)).getBody();
        assertNotNull(saved);

        ResponseEntity<Map<String, Object>> response = cartController.updateQuantity(new UpdateQuantityRequest(saved.getCartItemId(), 5));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, cartRepository.findAll().get(0).getQuantity());
    }

    @Test
    void updateQuantity_onlyUpdatesTargetItem() {
        CartItem first  = menuItem(1L, 1); first.setPrice(10.0);
        CartItem second = menuItem(2L, 1); second.setPrice(10.0);
        CartItem savedFirst  = cartController.addToCart(first).getBody();
        CartItem savedSecond = cartController.addToCart(second).getBody();
        assertNotNull(savedFirst);
        assertNotNull(savedSecond);

        cartController.updateQuantity(new UpdateQuantityRequest(savedFirst.getCartItemId(), 3));

        assertEquals(3, cartRepository.findAll().get(0).getQuantity(), "First item should be updated");
        assertEquals(1, cartRepository.findAll().get(1).getQuantity(), "Second item should be unchanged");
    }

    @Test
    void updateQuantity_unknownCartItemId_returnsNotFound() {
        ResponseEntity<Map<String, Object>> response = cartController.updateQuantity(new UpdateQuantityRequest(999L, 2));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateQuantity_zeroQuantity_removesItem() {
        CartItem saved = cartController.addToCart(menuItem(1L, 3)).getBody();
        assertNotNull(saved);

        cartController.updateQuantity(new UpdateQuantityRequest(saved.getCartItemId(), 0));

        assertTrue(cartRepository.findAll().isEmpty(), "Item should be removed when quantity is 0");
    }

    // ── applyPromo ────────────────────────────────────────────────────────────

    @Test
    void applyPromo_unknownCode_returnsNotFound() {
        when(promotionRepository.findByCode("BOGUS")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = cartController.applyPromo("BOGUS");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void applyPromo_expiredCode_returnsBadRequest() {
        when(promotionRepository.findByCode("OLD10"))
            .thenReturn(Optional.of(promo("OLD10", 5.0, 0.0, LocalDate.now().minusDays(1))));

        ResponseEntity<Map<String, Object>> response = cartController.applyPromo("OLD10");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void applyPromo_belowMinOrder_returnsBadRequest() {
        CartItem item = menuItem(1L, 1);
        item.setPrice(10.0); // $10 subtotal, below the $20 minimum
        cartController.addToCart(item);
        when(promotionRepository.findByCode("SAVE5"))
            .thenReturn(Optional.of(promo("SAVE5", 5.0, 20.0, LocalDate.now().plusDays(30))));

        ResponseEntity<Map<String, Object>> response = cartController.applyPromo("SAVE5");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void applyPromo_validCode_appliesDiscountToSummary() {
        CartItem item = menuItem(1L, 1);
        item.setPrice(15.0);
        cartController.addToCart(item);

        when(promotionRepository.findByCode("SAVE5"))
            .thenReturn(Optional.of(promo("SAVE5", 5.0, 0.0, LocalDate.now().plusDays(30))));

        ResponseEntity<Map<String, Object>> response = cartController.applyPromo("SAVE5");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(5.0,    body.get("discount"));
        assertEquals("SAVE5", body.get("promoCode"));
    }

    // ── removePromo ───────────────────────────────────────────────────────────

    @Test
    void removePromo_withNoPromoApplied_returnsOkWithZeroDiscount() {
        ResponseEntity<Map<String, Object>> response = cartController.removePromo();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(0.0, body.get("discount"));
        assertNull(body.get("promoCode"));
    }

    @Test
    void removePromo_clearsDiscountFromSummary() {
        cartRepository.applyPromo("SAVE5", 5.0);

        ResponseEntity<Map<String, Object>> response = cartController.removePromo();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(0.0,  body.get("discount"));
        assertNull(body.get("promoCode"));
    }
}