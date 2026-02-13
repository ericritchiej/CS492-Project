package com.pizzastore.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RestaurantInfoController {

    @GetMapping("/restaurant-info")
    public Map<String, Object> getRestaurantInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Pizza Store");
        info.put("address", "123 Main Street, Springfield, IL 62701");
        info.put("phone", "(555) 123-4567");
        info.put("hours", Arrays.asList(
            "Monday-Friday: 11:00 AM - 10:00 PM",
            "Saturday: 10:00 AM - 11:00 PM",
            "Sunday: 12:00 PM - 9:00 PM"
        ));
        return info;
    }
}
