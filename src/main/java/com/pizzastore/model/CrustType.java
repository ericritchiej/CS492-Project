package com.pizzastore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "crust_types")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CrustType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crust_id")
    private Long crustId;

    @Column(name = "crust_name")
    private String crustName;

    @Column(name = "price")
    private BigDecimal price;

    // getters and setters come from Lombok
}
