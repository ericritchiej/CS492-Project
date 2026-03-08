package com.pizzastore.controller;

import com.pizzastore.dto.OrderDto;
import com.pizzastore.dto.OrderItemDto;
import com.pizzastore.model.Order;
import com.pizzastore.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/history")
    public ResponseEntity<?> getOrderHistory(HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        Long customerId = ((Number) userIdObj).longValue();
        logger.info("getOrderHistory customerId={}", customerId);

        List<Order> orders = orderRepository.findByCustomerId(customerId);

        List<OrderDto> result = orders.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private OrderDto toDto(Order order) {
        List<OrderItemDto> items = new ArrayList<>();
        items.addAll(orderRepository.findRegularItemsByOrderId(order.getOrderId()));
        items.addAll(orderRepository.findCustomItemsByOrderId(order.getOrderId()));

        BigDecimal subtotal = items.stream()
                .map(i -> i.getLineTotal() != null ? i.getLineTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);

        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setPlacedAt(order.getOrderTimestamp().toInstant(ZoneOffset.UTC));
        dto.setStatus(order.getStatus());
        dto.setDeliveryMethod(order.getDeliveryMethod());
        dto.setSubtotal(subtotal);
        dto.setTax(tax);
        dto.setTotal(order.getTotalAmount());
        dto.setDiscount(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO);
        dto.setItems(items);

        return dto;
    }
}
