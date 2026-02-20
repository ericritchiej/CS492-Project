package com.pizzastore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "restaurant_hours")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantHours {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Column(name = "display_text")
    private String displayText;

    @Column(name = "sort_order")
    private Integer sortOrder;

    // getters and setters come from Lombok
}