package com.pizzastore.controller;

import com.pizzastore.model.User;
import com.pizzastore.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Define the logger for this specific class
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("loggedIn", false);
        status.put("message", "No user is currently logged in.");
        return status;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> handleSignIn(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        logger.info("Sign-in attempt for user: {}", username);
        logger.info("Password: {}", password);

        /// 1. Look up user by username
        // 3. Use the INSTANCE (userRepository), not the CLASS (UserRepository)
        List<User> users = userRepository.findByUsername(username);

        if (users != null && !users.isEmpty()) {
            User user = users.get(0);
            logger.info("Found user: {}", username);
//            logger.info("Password: {}", user.toString());

            if (passwordEncoder.matches(password, user.getPassword())) {
                Map<String, Object> userDto = new HashMap<>();
                userDto.put("id", user.getId());
                userDto.put("email", user.getEmail());
                userDto.put("firstName", user.getFirstName());
                userDto.put("lastName", user.getLastName());

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("user", userDto);

                return ResponseEntity.ok(response);
            }
        }

        logger.info("User not found: {}", username);
        Map<String, String> error = new HashMap<>();
        error.put("message", "Invalid username or password.");
        logger.error("Invalid username or password.");
        return ResponseEntity.status(401).body(error);

    }
}