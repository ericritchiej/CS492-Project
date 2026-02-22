package com.pizzastore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pizza_sizes")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class PizzaSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "size_id")
    private Long sizeId;

    @Column(name = "size_name")
    private String sizeName;

    @Column(name = "price")
    private BigDecimal price;

    // getters and setters come from Lombok
}
