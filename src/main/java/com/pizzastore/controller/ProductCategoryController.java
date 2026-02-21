package com.pizzastore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productCategory")
public class ProductCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(ProductCategoryController.class);


    public ProductCategoryController() { }

    @GetMapping("/getProductCategories")
    public ResponseEntity<List<Map<String, Object>>> getProductCategories() {
        logger.info("Fetching all product categories");

        // TODO: Replace with call to ProductCategoryRepository

        return ResponseEntity.ok(List.of(
                Map.of("id", 1, "name", "Drinks Stub"),
                Map.of("id", 2, "name", "Breadsticks Stub"),
                Map.of("id", 3, "name", "Pizza Stub")
        ));
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addProductCategory(@RequestBody Map<String, Object> productCategory) {
        logger.info("Adding product category");
        // TODO: save to database
        return ResponseEntity.ok(productCategory);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteProductCategory(@PathVariable int id) {
        logger.info("Deleting product category");
        // TODO: delete from database
        return ResponseEntity.ok().build();
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> update(@RequestBody Map<String, Object> productCategory) {
        logger.info("Updating product category");

        // TODO: update database
        return ResponseEntity.ok(productCategory);
    }
}
