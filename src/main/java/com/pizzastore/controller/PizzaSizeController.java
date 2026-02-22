package com.pizzastore.controller;

import com.pizzastore.model.PizzaSize;
import com.pizzastore.repository.PizzaSizeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pizzaSize")
public class PizzaSizeController {

    private static final Logger logger = LoggerFactory.getLogger(PizzaSizeController.class);

    private final PizzaSizeRepository pizzaSizeRepository;

    public PizzaSizeController(PizzaSizeRepository pizzaSizeRepository) {
        this.pizzaSizeRepository = pizzaSizeRepository;
    }

    @GetMapping("/getPizzaSizes")
    public ResponseEntity<List<PizzaSize>> getPizzaSizes() {
        logger.info("Fetching all pizza sizes");

        List<PizzaSize> pizzaSizes = pizzaSizeRepository.findAll();

        return ResponseEntity.ok(pizzaSizes);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addPizzaSize(@RequestBody PizzaSize pizzaSize) {
        logger.info("Adding pizza size{}", pizzaSize);

        pizzaSizeRepository.insertNewPizzaSize(pizzaSize);

        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePizzaSize(@PathVariable Long id) {
        logger.info("Deleting pizza size {}", id);

        pizzaSizeRepository.deletePizzaSize(id);

        return ResponseEntity.noContent().build();
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updatePizzaSize(@PathVariable Long id, @RequestBody PizzaSize pizzaSize) {
        logger.info("Updating pizza size id={}, body={}", id, pizzaSize);

        if (!id.equals(pizzaSize.getSizeId())) {
            return ResponseEntity.badRequest().build();
        }

        pizzaSizeRepository.updatePizzaSize(pizzaSize);

        return ResponseEntity.noContent().build();
    }
}
