package com.pizzastore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pizzaSize")
public class PizzaSizeController {

    private static final Logger logger = LoggerFactory.getLogger(PizzaSizeController.class);


    public PizzaSizeController() { }

    @GetMapping("/getPizzaSizes")
    public ResponseEntity<List<Map<String, Object>>> getPizzaSizes() {
        logger.info("Fetching all pizza sizes");

        // TODO: Replace with call to PizzaSizeRepository

        return ResponseEntity.ok(List.of(
                Map.of("id", 1, "name", "Small Stub",  "price", 1.00),
                Map.of("id", 2, "name", "Medium Stub", "price", 2.00),
                Map.of("id", 3, "name", "Large Stub",   "price", 2.99)
        ));
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addPizzaSize(@RequestBody Map<String, Object> pizzaSize) {
        logger.info("Adding pizza size{}", pizzaSize.toString());
        // TODO: save to database
        return ResponseEntity.ok(pizzaSize);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deletePizzaSize(@PathVariable int id) {
        logger.info("Deleting pizza size {}", id);
        // TODO: delete from database
        return ResponseEntity.ok().build();
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updatePizzaSize(@PathVariable int id, @RequestBody Map<String, Object> pizzaSize) {
        logger.info("Updating pizza size id={}, body={}", id, pizzaSize);

        // TODO: update database
        return ResponseEntity.ok(pizzaSize);
    }
}
