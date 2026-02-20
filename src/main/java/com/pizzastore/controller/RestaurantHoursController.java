package com.pizzastore.controller;

import com.pizzastore.model.RestaurantHours;
import com.pizzastore.repository.RestaurantHoursRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurant-hours")
public class RestaurantHoursController {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantHoursController.class);

    private final RestaurantHoursRepository restaurantHoursRepository;

    public RestaurantHoursController(RestaurantHoursRepository restaurantHoursRepository) {
        this.restaurantHoursRepository = restaurantHoursRepository;
    }

    @GetMapping
    public ResponseEntity<List<RestaurantHours>> getRestaurantHours() {
        logger.info("Fetching restaurant hours");
        List<RestaurantHours> restaurantHours = restaurantHoursRepository.findRestaurantHours();

        return ResponseEntity.ok(restaurantHours);
    }
}
