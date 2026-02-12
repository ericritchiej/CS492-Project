package com.pizzastore.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PizzaController {

    @GetMapping("/pizzas")
    public List<Map<String, Object>> getPizzas() {
        return Arrays.asList(
            pizza("Margherita", "Fresh mozzarella, tomato sauce, basil", 12.99),
            pizza("Pepperoni", "Pepperoni, mozzarella, tomato sauce", 14.99),
            pizza("Supreme", "Pepperoni, sausage, peppers, onions, olives", 16.99),
            pizza("BBQ Chicken", "Grilled chicken, BBQ sauce, red onion, cilantro", 15.99)
        );
    }

    @GetMapping("/orders")
    public List<Map<String, Object>> getOrders() {
        return Arrays.asList(
            order(1042, "2x Margherita, 1x Pepperoni", 40.97, "Delivered"),
            order(1043, "1x Supreme, 1x BBQ Chicken", 32.98, "In Progress")
        );
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("ordersToday", 127);
        stats.put("revenueToday", 3842.00);
        stats.put("menuItems", 12);
        return stats;
    }

    private Map<String, Object> pizza(String name, String description, double price) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("description", description);
        item.put("price", price);
        return item;
    }

    private Map<String, Object> order(int id, String items, double total, String status) {
        Map<String, Object> o = new HashMap<>();
        o.put("id", id);
        o.put("items", items);
        o.put("total", total);
        o.put("status", status);
        return o;
    }
}
