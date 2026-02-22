package com.pizzastore.controller;

import com.pizzastore.model.CrustType;
import com.pizzastore.repository.CrustTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crust")
public class CrustTypeController {

    private static final Logger logger = LoggerFactory.getLogger(CrustTypeController.class);

    private final CrustTypeRepository crustTypeRepository;

    public CrustTypeController(CrustTypeRepository  crustTypeRepository) {
       this.crustTypeRepository = crustTypeRepository;

    }

    @GetMapping("/getCrusts")
    public ResponseEntity<List<CrustType>> getCrusts() {
        logger.info("Fetching all pizza crusts");

        List<CrustType> crustTypes = crustTypeRepository.findAll();

        return ResponseEntity.ok(crustTypes);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addCrust(@RequestBody CrustType crust) {
        logger.info("Adding pizza crust{}", crust);

        crustTypeRepository.insertNewCrustType(crust);

        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCrust(@PathVariable Long id) {
        logger.info("Deleting pizza crust {}", id);

        crustTypeRepository.deleteCrustType(id);

        return ResponseEntity.noContent().build();
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<CrustType> updateCrust(@PathVariable Long id, @RequestBody CrustType crust) {
        logger.info("Updating pizza crust id={}, body={}", id, crust);

        if (!id.equals(crust.getCrustId())) {
            return ResponseEntity.badRequest().build();
        }

        crustTypeRepository.updateCrustType(crust);

        return ResponseEntity.noContent().build();
    }

}
