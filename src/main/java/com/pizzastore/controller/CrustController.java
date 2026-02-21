package com.pizzastore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crust")
public class CrustController {

    private static final Logger logger = LoggerFactory.getLogger(CrustController.class);


    public CrustController() { }

    @GetMapping("/getCrusts")
    public ResponseEntity<List<Map<String, Object>>> getCrusts() {
        logger.info("Fetching all pizza crusts");

        // TODO: Replace with call to CrustRepository

        return ResponseEntity.ok(List.of(
                Map.of("id", 1, "name", "Thin Crust Stub",  "price", 1.00),
                Map.of("id", 2, "name", "Hand-Tossed Stub", "price", 2.00),
                Map.of("id", 3, "name", "Deep Dish Stub",   "price", 2.99)
        ));
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addCrust(@RequestBody Map<String, Object> crust) {
        logger.info("Adding pizza crust");
        // TODO: save to database
        return ResponseEntity.ok(crust);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteCrust(@PathVariable int id) {
        logger.info("Deleting pizza crust");
        // TODO: delete from database
        return ResponseEntity.ok().build();
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> update(@RequestBody Map<String, Object> crust) {
        logger.info("Updating pizza crust");

        // TODO: update database
        return ResponseEntity.ok(crust);
    }
}
