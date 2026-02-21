package com.pizzastore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);


    public ProductController() { }

    @GetMapping("/getProducts")
    public ResponseEntity<List<Map<String, Object>>> getProducts() {
        logger.info("Fetching all products");

        // TODO: Replace with call to ProductRepository

        return ResponseEntity.ok(List.of(
                Map.of("id", 1, "name", "Soda Stub",  "catId", 1, "price", 1.00, "img" ,"img1"),
                Map.of("id", 1, "name", "Brownie Stub",  "catId", 2, "price", 2.00, "img" ,"img1"),
                Map.of("id", 1, "name", "Breadstick Stub",  "catId", 3, "price", 5.88, "img" ,"img1")
        ));
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addProduct(@RequestBody Map<String, Object> product) {
        logger.info("Adding product");
        // TODO: save to database
        return ResponseEntity.ok(product);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable int id) {
        logger.info("Deleting product");
        // TODO: delete from database
        return ResponseEntity.ok().build();
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> update(@RequestBody Map<String, Object> product) {
        logger.info("Updating product");

        // TODO: update database
        return ResponseEntity.ok(product);
    }
}
