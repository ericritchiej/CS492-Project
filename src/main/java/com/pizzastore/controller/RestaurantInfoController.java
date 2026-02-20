package com.pizzastore.controller;

import com.pizzastore.model.RestaurantInfo;
import com.pizzastore.repository.RestaurantInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurant-info")
public class RestaurantInfoController {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantInfoController.class);

    private final RestaurantInfoRepository restaurantInfoRepository;

    public RestaurantInfoController(RestaurantInfoRepository restaurantInfoRepository) {
        this.restaurantInfoRepository = restaurantInfoRepository;
    }

    @GetMapping
    public ResponseEntity<RestaurantInfo> getRestaurantInfo() {
        logger.info("Fetching restaurant information");
        List<RestaurantInfo> restaurantInfo = restaurantInfoRepository.findRestaurantInfo();

        if  (restaurantInfo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(restaurantInfo.get(0));
    }
}
