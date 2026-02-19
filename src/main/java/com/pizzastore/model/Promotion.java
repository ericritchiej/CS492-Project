package com.pizzastore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Long promotion_id;

    @Column(name = "code")
    private String code;

    @Column(name = "discount_value")
    private Double  discount_value;

    @Column(name = "promotion_desc")
    private String promotion_desc;

    @Column(name = "promotion_summary")
    private String promotion_summary;

    @Column(name = "exp_dt")
    private Date exp_dt;

    @Column(name = "min_order_amt")
    private Double  min_order_amt;

    // getters and setters come from Lombok
}