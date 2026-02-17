package com.pizzastore.controller;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutControllerTest {

    private final CheckoutController controller = new CheckoutController();

    @Test
    void summaryContainsExpectedKeys() {
        Map<String, Object> summary = controller.getSummary();
        assertTrue(summary.containsKey("items"));
        assertTrue(summary.containsKey("subtotal"));
        assertTrue(summary.containsKey("tax"));
        assertTrue(summary.containsKey("total"));
    }

    @Test
    void summaryItemsIsNotEmpty() {
        Map<String, Object> summary = controller.getSummary();
        List<?> items = (List<?>) summary.get("items");
        assertFalse(items.isEmpty(), "Summary should have at least one item");
    }

    @Test
    void summarySubtotalPlusTaxEqualsTotal() {
        Map<String, Object> summary = controller.getSummary();
        double subtotal = (double) summary.get("subtotal");
        double tax = (double) summary.get("tax");
        double total = (double) summary.get("total");
        assertEquals(subtotal + tax, total, 0.01, "Subtotal + tax should equal total");
    }
}