package com.pizzastore.controller;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final DSLContext dsl;

    public OrderController(DSLContext dsl) {
        this.dsl = dsl;
    }

    @GetMapping("/history")
    public ResponseEntity<?> getOrderHistory(HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        Long customerId = ((Number) userIdObj).longValue();

        Result<Record> orders = dsl.select()
                .from(DSL.table("orders"))
                .where(DSL.field("customer_id").eq(customerId))
                .orderBy(DSL.field("order_timestamp").desc())
                .fetch();

        List<Map<String, Object>> result = orders.stream().map(record -> Map.of(
                "orderId",        record.get(DSL.field("order_id", Long.class)),
                "orderTimestamp", record.get(DSL.field("order_timestamp")).toString(),
                "totalAmount",    record.get(DSL.field("total_amount")),
                "discountAmount", record.get(DSL.field("discount_amount")),
                "status",         record.get(DSL.field("status", String.class))
        )).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}