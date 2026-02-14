package com.pizzastore.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProfileController {

    @GetMapping("/profile")
    public Map<String, Object> getProfile() {
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", "John Doe");
        profile.put("email", "john.doe@example.com");
        profile.put("address", "456 Oak Avenue, Springfield, IL 62702");
        profile.put("phone", "(555) 987-6543");
        return profile;
    }
}
