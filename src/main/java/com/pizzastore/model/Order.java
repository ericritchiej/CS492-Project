package com.pizzastore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "promotions_id")
    private Long promotionsId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "order_timestamp")
    private LocalDateTime orderTimestamp;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "status")
    private String status;

    @Column(name = "delivery_method")
    private String deliveryMethod;
}
