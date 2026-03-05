package com.pizzastore.controller;

import com.pizzastore.dto.CheckoutRequestDto;
import com.pizzastore.dto.OrderConfirmationDto;
import com.pizzastore.model.CartItem;
import com.pizzastore.model.Order;
import com.pizzastore.repository.CartRepository;
import com.pizzastore.repository.OrderRepository;
import com.pizzastore.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CheckoutControllerTest {

    private CartRepository cartRepository;
    private OrderRepository orderRepository;
    private PromotionRepository promotionRepository;
    private CheckoutController controller;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        cartRepository = new CartRepository();
        orderRepository = mock(OrderRepository.class);
        promotionRepository = mock(PromotionRepository.class);
        controller = new CheckoutController(cartRepository, orderRepository, promotionRepository);
        session = new MockHttpSession();
        session.setAttribute("userId", 1L);
    }

    @Test
    void summaryContainsExpectedKeys() {
        Map<String, Object> summary = controller.getSummary();
        assertTrue(summary.containsKey("items"));
        assertTrue(summary.containsKey("subtotal"));
        assertTrue(summary.containsKey("tax"));
        assertTrue(summary.containsKey("total"));
    }

    @Test
    void processCheckout_emptyCart_returnsBadRequest() {
        ResponseEntity<OrderConfirmationDto> response = controller.processCheckout(
                new CheckoutRequestDto("PICKUP", ""), session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cannot checkout with an empty cart", response.getBody().getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void processCheckout_deliveryWithoutAddress_returnsBadRequest() {
        CartItem item = new CartItem();
        item.setProductId(1L);
        item.setName("Pepperoni");
        item.setQuantity(1);
        item.setPrice(10.0);
        cartRepository.addItem(item);

        ResponseEntity<OrderConfirmationDto> response = controller.processCheckout(
                new CheckoutRequestDto("DELIVERY", ""), session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Delivery address is required for DELIVERY", response.getBody().getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void processCheckout_success_clearsCartAndReturnsConfirmation() {
        CartItem item = new CartItem();
        item.setProductId(1L);
        item.setName("Pepperoni");
        item.setQuantity(2);
        item.setPrice(12.5);
        cartRepository.addItem(item);

        when(orderRepository.save(any(Order.class))).thenReturn(123L);

        ResponseEntity<OrderConfirmationDto> response = controller.processCheckout(
                new CheckoutRequestDto("PICKUP", ""), session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(123L, response.getBody().getOrderId());
        assertEquals("PICKUP", response.getBody().getDeliveryMethod());
        assertEquals("PENDING", response.getBody().getStatus());
        assertTrue(cartRepository.findAll().isEmpty(), "Cart should be cleared after successful checkout");
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}
