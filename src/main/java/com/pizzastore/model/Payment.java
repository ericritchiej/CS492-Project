package com.pizzastore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payment_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pay_id")
    private Long payId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "cc_number")
    private String ccNumber;

    @Column(name = "exp_month")
    private Long expMonth;

    @Column(name = "exp_year")
    private Long expYear;

    @Column(name = "ccv_number")
    private String ccvNumber;
    // getters and setters come from Lombok
}
