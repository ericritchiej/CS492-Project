package com.pizzastore.controller;

import com.pizzastore.model.Promotion;
import com.pizzastore.repository.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private static final Logger logger = LoggerFactory.getLogger(PromotionController.class);

    private final PromotionRepository promotionRepository;

    public PromotionController(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @GetMapping
    public ResponseEntity<List<Promotion>> getPromotions() {
        logger.info("Fetching all promotions");
        List<Promotion> promotions = promotionRepository.findByAllPromotions();
        logger.info("found promotions: {}",promotions.size());
        return ResponseEntity.ok(promotions);
    }
}
