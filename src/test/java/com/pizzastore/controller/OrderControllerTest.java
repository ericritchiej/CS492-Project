package com.pizzastore.controller;

import com.pizzastore.dto.OrderDto;
import com.pizzastore.dto.OrderItemDto;
import com.pizzastore.model.Order;
import com.pizzastore.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    private OrderRepository orderRepository;
    private OrderController controller;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        controller      = new OrderController(orderRepository);
        session         = new MockHttpSession();
        session.setAttribute("userId", 1L);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Order order(long orderId, String status, String deliveryMethod, double total, Double discount) {
        Order o = new Order();
        o.setOrderId(orderId);
        o.setCustomerId(1L);
        o.setOrderTimestamp(LocalDateTime.of(2025, 1, 15, 12, 0));
        o.setStatus(status);
        o.setDeliveryMethod(deliveryMethod);
        o.setTotalAmount(BigDecimal.valueOf(total));
        o.setDiscountAmount(discount != null ? BigDecimal.valueOf(discount) : null);
        return o;
    }

    private OrderItemDto item(long cartItemId, String name, int qty, double lineTotal) {
        OrderItemDto dto = new OrderItemDto();
        dto.setCartItemId(cartItemId);
        dto.setName(name);
        dto.setQuantity(qty);
        dto.setLineTotal(BigDecimal.valueOf(lineTotal));
        return dto;
    }

    @SuppressWarnings("unchecked")
    private List<OrderDto> bodyOf(ResponseEntity<?> response) {
        return (List<OrderDto>) response.getBody();
    }

    // ── Authentication ────────────────────────────────────────────────────────

    @Test
    void getOrderHistory_unauthenticated_returns401() {
        ResponseEntity<?> response = controller.getOrderHistory(new MockHttpSession());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(orderRepository, never()).findByCustomerId(anyLong());
    }

    @Test
    void getOrderHistory_queriesRepositoryWithCorrectCustomerId() {
        session.setAttribute("userId", 99);
        when(orderRepository.findByCustomerId(99L)).thenReturn(List.of());

        controller.getOrderHistory(session);

        verify(orderRepository).findByCustomerId(99L);
    }

    // ── Order list ────────────────────────────────────────────────────────────

    @Test
    void getOrderHistory_noOrders_returnsEmptyList() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of());

        ResponseEntity<?> response = controller.getOrderHistory(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<OrderDto> body = bodyOf(response);
        assertNotNull(body);
        assertTrue(body.isEmpty());
    }

    @Test
    void getOrderHistory_multipleOrders_returnsAll() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(
                order(1L, "DELIVERED", "PICKUP",   10.80, null),
                order(2L, "CANCELLED", "DELIVERY", 21.60, null)
        ));
        when(orderRepository.findRegularItemsByOrderId(anyLong())).thenReturn(List.of());
        when(orderRepository.findCustomItemsByOrderId(anyLong())).thenReturn(List.of());

        List<OrderDto> body = bodyOf(controller.getOrderHistory(session));

        assertNotNull(body);
        assertEquals(2, body.size());
    }

    @Test
    void getOrderHistory_mapsOrderFieldsCorrectly() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(
                order(42L, "DELIVERED", "PICKUP", 21.60, null)
        ));
        when(orderRepository.findRegularItemsByOrderId(42L)).thenReturn(List.of());
        when(orderRepository.findCustomItemsByOrderId(42L)).thenReturn(List.of());

        OrderDto dto = bodyOf(controller.getOrderHistory(session)).get(0);

        assertEquals(42L,         dto.getOrderId());
        assertEquals("DELIVERED", dto.getStatus());
        assertEquals("PICKUP",    dto.getDeliveryMethod());
        assertEquals(0, BigDecimal.valueOf(21.60).compareTo(dto.getTotal()));
        assertNotNull(dto.getPlacedAt());
    }

    // ── Items ─────────────────────────────────────────────────────────────────

    @Test
    void getOrderHistory_populatesRegularItems() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(
                order(1L, "PLACED", "PICKUP", 10.80, null)
        ));
        when(orderRepository.findRegularItemsByOrderId(1L)).thenReturn(List.of(
                item(10L, "Pepperoni Pizza", 1, 10.00)
        ));
        when(orderRepository.findCustomItemsByOrderId(1L)).thenReturn(List.of());

        OrderDto dto = bodyOf(controller.getOrderHistory(session)).get(0);

        assertEquals(1, dto.getItems().size());
        assertEquals("Pepperoni Pizza", dto.getItems().get(0).getName());
    }

    @Test
    void getOrderHistory_populatesCustomItems() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(
                order(2L, "PREPARING", "DELIVERY", 12.96, null)
        ));
        when(orderRepository.findRegularItemsByOrderId(2L)).thenReturn(List.of());
        when(orderRepository.findCustomItemsByOrderId(2L)).thenReturn(List.of(
                item(20L, "Custom Pizza", 1, 12.00)
        ));

        OrderDto dto = bodyOf(controller.getOrderHistory(session)).get(0);

        assertEquals(1, dto.getItems().size());
        assertEquals("Custom Pizza", dto.getItems().get(0).getName());
    }

    @Test
    void getOrderHistory_combinesRegularAndCustomItems() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(
                order(3L, "PLACED", "PICKUP", 23.76, null)
        ));
        when(orderRepository.findRegularItemsByOrderId(3L)).thenReturn(List.of(
                item(10L, "Margherita", 1, 11.00)
        ));
        when(orderRepository.findCustomItemsByOrderId(3L)).thenReturn(List.of(
                item(20L, "Custom Pizza", 1, 11.00)
        ));

        OrderDto dto = bodyOf(controller.getOrderHistory(session)).get(0);

        assertEquals(2, dto.getItems().size());
    }

    // ── Subtotal & Tax ────────────────────────────────────────────────────────

    @Test
    void getOrderHistory_calculatesSubtotalFromItemLineTotals() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(
                order(4L, "DELIVERED", "PICKUP", 32.40, null)
        ));
        when(orderRepository.findRegularItemsByOrderId(4L)).thenReturn(List.of(
                item(1L, "Pizza A", 1, 10.00),
                item(2L, "Pizza B", 1, 20.00)
        ));
        when(orderRepository.findCustomItemsByOrderId(4L)).thenReturn(List.of());

        OrderDto dto = bodyOf(controller.getOrderHistory(session)).get(0);

        assertEquals(0, new BigDecimal("30.00").compareTo(dto.getSubtotal()));
    }

    @Test
    void getOrderHistory_calculatesTaxAsEightPercent() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(
                order(5L, "DELIVERED", "PICKUP", 32.40, null)
        ));
        when(orderRepository.findRegularItemsByOrderId(5L)).thenReturn(List.of(
                item(1L, "Pizza", 1, 30.00)
        ));
        when(orderRepository.findCustomItemsByOrderId(5L)).thenReturn(List.of());

        OrderDto dto = bodyOf(controller.getOrderHistory(session)).get(0);

        assertEquals(0, new BigDecimal("2.40").compareTo(dto.getTax()));
    }

    @Test
    void getOrderHistory_emptyItems_subtotalAndTaxAreZero() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(
                order(6L, "PLACED", "PICKUP", 0.0, null)
        ));
        when(orderRepository.findRegularItemsByOrderId(6L)).thenReturn(List.of());
        when(orderRepository.findCustomItemsByOrderId(6L)).thenReturn(List.of());

        OrderDto dto = bodyOf(controller.getOrderHistory(session)).get(0);

        assertEquals(0, BigDecimal.ZERO.compareTo(dto.getSubtotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(dto.getTax()));
    }

    // ── Discount ──────────────────────────────────────────────────────────────

    @Test
    void getOrderHistory_nullDiscountDefaultsToZero() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(
                order(7L, "DELIVERED", "PICKUP", 10.80, null)
        ));
        when(orderRepository.findRegularItemsByOrderId(7L)).thenReturn(List.of());
        when(orderRepository.findCustomItemsByOrderId(7L)).thenReturn(List.of());

        OrderDto dto = bodyOf(controller.getOrderHistory(session)).get(0);

        assertEquals(0, BigDecimal.ZERO.compareTo(dto.getDiscount()));
    }

    @Test
    void getOrderHistory_setsDiscountFromOrder() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(
                order(8L, "DELIVERED", "PICKUP", 26.28, 5.00)
        ));
        when(orderRepository.findRegularItemsByOrderId(8L)).thenReturn(List.of());
        when(orderRepository.findCustomItemsByOrderId(8L)).thenReturn(List.of());

        OrderDto dto = bodyOf(controller.getOrderHistory(session)).get(0);

        assertEquals(0, new BigDecimal("5.00").compareTo(dto.getDiscount()));
    }
}