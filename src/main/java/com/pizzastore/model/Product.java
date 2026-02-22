package com.pizzastore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "is_customizable")
    private boolean customizable;

    // getters and setters come from Lombok
}
