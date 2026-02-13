package com.pizzastore.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("items", Arrays.asList(
            summaryItem("Margherita", 2, 25.98),
            summaryItem("Pepperoni", 1, 14.99)
        ));
        summary.put("subtotal", 40.97);
        summary.put("tax", 3.28);
        summary.put("total", 44.25);
        return summary;
    }

    private Map<String, Object> summaryItem(String name, int quantity, double lineTotal) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("quantity", quantity);
        item.put("lineTotal", lineTotal);
        return item;
    }
}
