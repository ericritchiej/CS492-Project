package com.pizzastore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "toppings")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Topping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topping_id")
    private Long toppingId;

    @Column(name = "topping_name")
    private String toppingName;

    @Column(name = "extra_cost")
    private BigDecimal extraCost;

    // getters and setters come from Lombok
}
