package com.pizzastore.controller;

import com.pizzastore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * A "Controller" is the entry point for HTTP requests coming from the frontend (Angular).
 * Think of it like a receptionist — it receives requests, figures out what to do,
 * and sends back a response.
 * "@RestController" tells Spring this class handles web requests and automatically
 * converts our return values to JSON.
 * "@RequestMapping("/api/auth")" means every endpoint in this class starts with
 * /api/auth — so our signIn URL becomes /api/auth/signIn, etc.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    public record RegisterRequest(
            String firstName,
            String lastName,
            String phone,
            String address1,
            String address2,
            String city,
            String state,
            String zip,
            String email
    ) {}

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/getUser")
    public ResponseEntity<?> getUserInfo() {
        logger.info("Fetching users data");

        // TODO: Replace with call to retrieve address and user info
        RegisterRequest repsonse = new RegisterRequest(
                "John",
                "Doe",
                "555-1234",
                "123 Main St",
                "street2",
                "Eau Claire",
                "WI",
                "54701",
                "john.doe@example.com"
        );

        return ResponseEntity.ok(repsonse);
    }

    @PostMapping("/updateDemographics")
    public ResponseEntity<?> updateDemographics(@RequestBody RegisterRequest request) {
        logger.info("Update Demographics: {}", request);

        // TODO: Replace with call to UserRepository and AddressRepository

        return ResponseEntity.ok(request);
    }
}