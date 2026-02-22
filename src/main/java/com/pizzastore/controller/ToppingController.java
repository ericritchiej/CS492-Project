package com.pizzastore.controller;

import com.pizzastore.model.Topping;
import com.pizzastore.repository.ToppingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topping")
public class ToppingController {

    private static final Logger logger = LoggerFactory.getLogger(ToppingController.class);

    private final ToppingRepository toppingRepository;

    public ToppingController(ToppingRepository  toppingRepository) {
        this.toppingRepository = toppingRepository;
    }

    @GetMapping("/getToppings")
    public ResponseEntity<List<Topping>> getToppings() {
        logger.info("Fetching all toppings");

        List<Topping> toppings = toppingRepository.findAll();

        return ResponseEntity.ok(toppings);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addItem(@RequestBody Topping topping) {
        logger.info("Adding topping {}", topping);

        toppingRepository.insertNewTopping(topping);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        logger.info("Deleting topping {}", id);

        toppingRepository.deleteTopping(id);

        return ResponseEntity.noContent().build();
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateItem(@PathVariable Long id, @RequestBody Topping topping) {
        logger.info("Updating topping id={}, body={}", id, topping);

        if (!id.equals(topping.getToppingId())) {
            return ResponseEntity.badRequest().build();
        }

        toppingRepository.updateTopping(topping);

        return ResponseEntity.noContent().build();
    }
}
