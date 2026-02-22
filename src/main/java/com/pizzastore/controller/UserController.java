package com.pizzastore.controller;

import com.pizzastore.model.Address;
import com.pizzastore.model.User;
import com.pizzastore.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // DTO for update request
    public static class UpdateRequest {
        public String firstName;
        public String lastName;
        public String phone;
        public String address1;
        public String address2;
        public String city;
        public String state;
        public String zip;
    }

    // =========================
    // GET Profile
    // =========================
    @GetMapping
    public ResponseEntity<?> getUserProfile(HttpSession session) {

        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        long userId = ((Number) userIdObj).longValue();

        User user = userRepository.findCustomerById(userId);
        Address address = userRepository.findAddressByCustomerId(userId);

        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("phone", user.getPhoneNumber());

        Map<String, Object> addr = new HashMap<>();
        if (address != null) {
            addr.put("address1", address.getAddress1());
            addr.put("address2", address.getAddress2());
            addr.put("city", address.getCity());
            addr.put("state", address.getState());
            addr.put("zip", address.getZip());
        }

        response.put("address", addr);

        return ResponseEntity.ok(response);
    }

    // =========================
    // UPDATE Profile
    // =========================
    @PutMapping
    public ResponseEntity<?> updateUserProfile(@RequestBody UpdateRequest body,
                                               HttpSession session) {

        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        long userId = ((Number) userIdObj).longValue();

        userRepository.updateCustomerProfile(
                userId,
                body.firstName,
                body.lastName,
                body.phone
        );

        userRepository.updateCustomerAddress(
                userId,
                body.address1,
                body.address2,
                body.city,
                body.state,
                body.zip
        );

        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }
}
