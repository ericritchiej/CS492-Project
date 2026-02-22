package com.pizzastore.controller;

import com.pizzastore.model.ProductCategory;
import com.pizzastore.repository.ProductCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productCategory")
public class ProductCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(ProductCategoryController.class);

    private final ProductCategoryRepository productCategoryRepository;

    public ProductCategoryController(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }

    @GetMapping("/getProductCategories")
    public ResponseEntity<List<ProductCategory>> getProductCategories() {
        logger.info("Fetching all product categories");

        List<ProductCategory> productCategories = productCategoryRepository.findAll();

        return ResponseEntity.ok(productCategories);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addProductCategory(@RequestBody ProductCategory productCategory) {
        logger.info("Adding product category {}", productCategory);

        productCategoryRepository.insertNewProductCategory(productCategory);

        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProductCategory(@PathVariable Long id) {
        logger.info("Deleting product category {}", id);

        productCategoryRepository.deleteProductCategory(id);

        return ResponseEntity.noContent().build();
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateProductCategory(@PathVariable Long id, @RequestBody ProductCategory productCategory) {
        logger.info("Updating product category id={}, body={}", id, productCategory);

        if (!id.equals(productCategory.getCategoryId())) {
            return ResponseEntity.badRequest().build();
        }

        productCategoryRepository.updateProductCategory(productCategory);

        return ResponseEntity.noContent().build();
    }

}
