package com.pizzastore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/topping")
public class ToppingController {

    private static final Logger logger = LoggerFactory.getLogger(ToppingController.class);


    public ToppingController() { }

    @GetMapping("/getToppings")
    public ResponseEntity<List<Map<String, Object>>> getToppings() {
        logger.info("Fetching all toppings");

        // TODO: Replace with call to ToppingRepository

        return ResponseEntity.ok(List.of(
                Map.of("id", 1, "name", "Pepperoni Stub",  "cost", 1.00),
                Map.of("id", 2, "name", "Extra Cheese Stub", "cost", 2.00),
                Map.of("id", 3, "name", "Onions Stub",   "cost", 2.99)
        ));
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addTopping(@RequestBody Map<String, Object> topping) {
        logger.info("Adding topping");
        // TODO: save to database
        return ResponseEntity.ok(topping);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteTopping(@PathVariable int id) {
        logger.info("Deleting topping");
        // TODO: delete from database
        return ResponseEntity.ok().build();
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> update(@RequestBody Map<String, Object> topping) {
        logger.info("Updating topping");

        // TODO: update database
        return ResponseEntity.ok(topping);
    }
}
