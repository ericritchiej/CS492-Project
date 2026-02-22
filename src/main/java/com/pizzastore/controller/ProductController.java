package com.pizzastore.controller;

import com.pizzastore.model.Product;
import com.pizzastore.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/getProducts")
    public ResponseEntity<List<Product>> getProducts() {
        logger.info("Fetching all products");

        List<Product> products = productRepository.findAll();

        return ResponseEntity.ok(products);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addProduct(@RequestBody Product product) {
        logger.info("Adding product {}", product);

        productRepository.insertNewProduct(product);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.info("Deleting product {}", id);

        productRepository.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        logger.info("Updating product id={}, body={}", id, product);

        if (!id.equals(product.getProductId())) {
            return ResponseEntity.badRequest().build();
        }

        productRepository.updateProduct(product);

        return ResponseEntity.noContent().build();
    }

}
