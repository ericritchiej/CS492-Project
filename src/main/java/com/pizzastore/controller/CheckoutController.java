package com.pizzastore.controller;

import com.pizzastore.dto.CheckoutRequestDto;
import com.pizzastore.dto.OrderConfirmationDto;
import com.pizzastore.model.CartItem;
import com.pizzastore.model.Order;
import com.pizzastore.model.Promotion;
import com.pizzastore.repository.CartRepository;
import com.pizzastore.repository.OrderRepository;
import com.pizzastore.repository.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PromotionRepository promotionRepository;

    public CheckoutController(CartRepository cartRepository,
                              OrderRepository orderRepository,
                              PromotionRepository promotionRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.promotionRepository = promotionRepository;
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        return buildSummary();
    }

    @PostMapping("/process")
    public ResponseEntity<OrderConfirmationDto> processCheckout(
            @RequestBody CheckoutRequestDto request,
            HttpSession session) {

        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            logger.error("not authorized to process checkout");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new OrderConfirmationDto(null, null, null, null, "Authentication required"));
        }

        List<CartItem> items = cartRepository.findAll();
        if (items.isEmpty()) {
            logger.error("cart is empty");
            return ResponseEntity.badRequest()
                    .body(new OrderConfirmationDto(null, null, null, null, "Cannot checkout with an empty cart"));
        }

        String deliveryMethod = request == null || request.getDeliveryMethod() == null
                ? ""
                : request.getDeliveryMethod().trim().toUpperCase();

        if (!"DELIVERY".equals(deliveryMethod) && !"PICKUP".equals(deliveryMethod)) {
            logger.error("delivery method not supported");
            return ResponseEntity.badRequest()
                    .body(new OrderConfirmationDto(null, null, null, null,
                            "deliveryMethod must be DELIVERY or PICKUP"));
        }

        if ("DELIVERY".equals(deliveryMethod)) {
            String address = request.getDeliveryAddress() == null ? "" : request.getDeliveryAddress().trim();
            if (address.isEmpty()) {
                logger.error("address is empty");
                return ResponseEntity.badRequest()
                        .body(new OrderConfirmationDto(null, null, deliveryMethod, null,
                                "Delivery address is required for DELIVERY"));
            }
        }

        Long customerId = ((Number) userIdObj).longValue();
        String deliveryAddress = request.getDeliveryAddress() == null ? null : request.getDeliveryAddress().trim();
        Long addressId = "DELIVERY".equals(deliveryMethod)
                ? orderRepository.findOrCreateAddressId(customerId, deliveryAddress)
                : null;

        BigDecimal subtotal = BigDecimal.valueOf(cartRepository.getTotal()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = BigDecimal.valueOf(cartRepository.getAppliedDiscount()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxable = subtotal.subtract(discount).max(BigDecimal.ZERO);
        BigDecimal tax = taxable.multiply(BigDecimal.valueOf(0.08)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = taxable.add(tax).setScale(2, RoundingMode.HALF_UP);

        Long promotionsId = null;
        String promoCode = cartRepository.getAppliedPromoCode();
        if (promoCode != null && !promoCode.isBlank()) {
            Optional<Promotion> promo = promotionRepository.findByCode(promoCode);
            if (promo.isPresent()) {
                promotionsId = promo.get().getPromotionId();
            }
        }

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setAddressId(addressId);
        order.setPromotionsId(promotionsId);
        order.setEmployeeId(null);
        order.setOrderTimestamp(LocalDateTime.now());
        order.setTotalAmount(total);
        order.setDiscountAmount(discount);
        order.setStatus("PENDING");

        Long orderId = orderRepository.save(order);
        cartRepository.clearCart();

        return ResponseEntity.ok(new OrderConfirmationDto(
                orderId,
                order.getStatus(),
                deliveryMethod,
                total,
                "Order processed successfully"
        ));
    }

    private Map<String, Object> buildSummary() {
        double subtotal = cartRepository.getTotal();
        double discount = cartRepository.getAppliedDiscount();
        double taxable = Math.max(0, subtotal - discount);
        double tax = Math.round(taxable * 0.08 * 100.0) / 100.0;
        double total = Math.round((taxable + tax) * 100.0) / 100.0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("items", cartRepository.findAll().stream().map(this::summaryItem).toList());
        summary.put("subtotal", subtotal);
        summary.put("discount", discount);
        summary.put("tax", tax);
        summary.put("total", total);
        return summary;
    }

    private Map<String, Object> summaryItem(CartItem item) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("name", item.getName());
        summary.put("quantity", item.getQuantity());
        summary.put("lineTotal", item.getLineTotal());
        return summary;
    }
}
