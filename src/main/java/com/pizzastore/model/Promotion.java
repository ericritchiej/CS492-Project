package com.pizzastore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Long promotionId;

    @Column(name = "code")
    private String code;

    @Column(name = "discount_value")
    private Double discountValue;

    @Column(name = "promotion_desc")
    private String promotionDesc;

    @Column(name = "promotion_summary")
    private String promotionSummary;

    @Column(name = "exp_dt")
    private LocalDate expDt;

    @Column(name = "min_order_amt")
    private Double minOrderAmt;

    // getters and setters come from Lombok
}