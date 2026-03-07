package com.pizzastore.repository;

import com.pizzastore.model.CartItem;
import com.pizzastore.model.Order;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);

    private final DSLContext dsl;

    public OrderRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    private static class ParsedAddress {
        String street1;
        String city;
        String state;
        String zip;
    }

    /**
     * Some environments name the addresses primary key column "address_id",
     * others name it "id". This helper makes our checkout code tolerant.
     */
    private String resolveAddressPkColumn() {
        try {
            dsl.select(DSL.field("address_id"))
                    .from(DSL.table("addresses"))
                    .limit(1)
                    .fetch();
            return "address_id";
        } catch (DataAccessException ex) {
            logger.warn("addresses.address_id not found, falling back to addresses.id");
            return "id";
        }
    }

    private ParsedAddress parseAddress(String deliveryAddress) {
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery address is required.");
        }

        String normalized = deliveryAddress.trim().replace("\r", "");
        String[] lines = normalized.split("\n");

        if (lines.length < 2) {
            throw new IllegalArgumentException(
                    "Delivery address must be in format: street on first line, City, ST, ZIP on second line.");
        }

        ParsedAddress parsed = new ParsedAddress();
        parsed.street1 = lines[0].trim();

        String[] parts = lines[1].split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException(
                    "Delivery address second line must be in format: City, ST, ZIP");
        }

        parsed.city = parts[0].trim();
        parsed.state = parts[1].trim();
        parsed.zip = parts[2].trim();

        if (parsed.street1.isEmpty() || parsed.city.isEmpty()
                || parsed.state.isEmpty() || parsed.zip.isEmpty()) {
            throw new IllegalArgumentException("All address fields are required.");
        }

        return parsed;
    }

    @SuppressWarnings("resource")
    public Long findExistingAddressId(Long addressId) {
        if (addressId == null) return null;

        String pk = resolveAddressPkColumn();

        Record1<Integer> existing = dsl.select(DSL.field(pk, Integer.class))
                .from(DSL.table("addresses"))
                .where(DSL.field("address_id", Long.class).eq(addressId))
                .limit(1)
                .fetchOne();

        if (existing == null || existing.value1() == null) {
            return null;
        }
        return existing.value1().longValue();
    }

    @SuppressWarnings("resource")
    public Long findOrCreateAddressId(Long addressId, Long customerId, String deliveryAddress) {
        logger.info("findOrCreateAddressId addressId={}, customerId={}", addressId, customerId);

        ParsedAddress addr = parseAddress(deliveryAddress);
        String pk = resolveAddressPkColumn();

        // If we already have an addressId, look it up and return it
        if (addressId != null) {
            Record1<Integer> existing = dsl.select(DSL.field(pk, Integer.class))
                    .from(DSL.table("addresses"))
                    .where(DSL.field("address_id", Long.class).eq(addressId))
                    .fetchOne();

            if (existing != null && existing.value1() != null) {
                logger.info("address already exists");
                return existing.value1().longValue();
            }
        }

        Record inserted = dsl.insertInto(DSL.table("addresses"))
                .set(DSL.field("customer_id"), customerId)
                .set(DSL.field("street_addr_1"), addr.street1)
                .set(DSL.field("street_addr_2"), (String) null)
                .set(DSL.field("city"), addr.city)
                .set(DSL.field("state"), addr.state)
                .set(DSL.field("zip_code"), addr.zip)
                .returning(DSL.field(pk, Integer.class))
                .fetchOne();

        Integer addressIdReturn = inserted == null ? null : inserted.get(DSL.field(pk, Integer.class));
        if (addressIdReturn == null) {
            logger.error("address id is null after insert");
            throw new RuntimeException("Failed to create address record.");
        }

        return addressIdReturn.longValue();
    }

    @SuppressWarnings("resource")
    public Long save(Order order) {
        logger.info("save order={}", order);

        Record inserted = dsl.insertInto(DSL.table("orders"))
                .set(DSL.field("customer_id", Integer.class), order.getCustomerId() == null ? null : order.getCustomerId().intValue())
                .set(DSL.field("address_id", Integer.class), order.getAddressId() == null ? null : order.getAddressId().intValue())
                .set(DSL.field("promotion_id", Integer.class), order.getPromotionsId() == null ? null : order.getPromotionsId().intValue())
                .set(DSL.field("employee_id", Integer.class), order.getEmployeeId() == null ? null : order.getEmployeeId().intValue())
                .set(DSL.field("order_timestamp", java.time.LocalDateTime.class), order.getOrderTimestamp())
                .set(DSL.field("total_amount", java.math.BigDecimal.class), order.getTotalAmount())
                .set(DSL.field("discount_amount", java.math.BigDecimal.class), order.getDiscountAmount())
                .set(DSL.field("status", String.class), order.getStatus())
                .set(DSL.field("delivery_method", String.class), order.getDeliveryMethod())
                .returning(DSL.field("order_id", Integer.class))
                .fetchOne();

        Integer orderId = inserted == null ? null : inserted.get(DSL.field("order_id", Integer.class));
        if (orderId == null) {
            logger.error("order id is null");
            throw new RuntimeException("Failed to save order.");
        }

        return orderId.longValue();
    }

    @SuppressWarnings("resource")
    public Long saveRegularItem(Long orderId, CartItem cartItem) {
        logger.info("saveRegularItem orderId={}, cartItem={}", orderId, cartItem);

        Record inserted = dsl.insertInto(DSL.table("order_items"))
                .set(DSL.field("order_id", Integer.class), orderId.intValue())
                .set(DSL.field("product_id", Integer.class), cartItem.getProductId().intValue())
                .set(DSL.field("size_id", Integer.class), cartItem.getSizeId() == null ? null : cartItem.getSizeId().intValue())
                .set(DSL.field("crust_id", Integer.class), cartItem.getCrustTypeId() == null ? null : cartItem.getCrustTypeId().intValue())
                .set(DSL.field("sauce_name", String.class), cartItem.getSauceName() == null ? null : cartItem.getSauceName())
                .set(DSL.field("quantity", Integer.class), cartItem.getQuantity())
                .set(DSL.field("price_per", Double.class), cartItem.getPrice())
                .set(DSL.field("item_notes", String.class), (String) null)
                .returning(DSL.field("order_item_id", Integer.class))
                .fetchOne();

        Integer orderItemId = inserted == null ? null : inserted.get(DSL.field("order_item_id", Integer.class));
        if (orderItemId == null) {
            logger.error("order item id is null");
            throw new RuntimeException("Failed to save order item.");
        }

        return orderItemId.longValue();
    }

    @SuppressWarnings("resource")
    public Long saveCustomItem(Long orderId, CartItem cartItem) {
        logger.info("saveCustomItem orderId={}, cartItem={}", orderId, cartItem);

        Record inserted = dsl.insertInto(DSL.table("order_custom_item"))
                .set(DSL.field("order_id", Integer.class), orderId.intValue())
                .set(DSL.field("size_id", Integer.class), cartItem.getSizeId() == null ? null : cartItem.getSizeId().intValue())
                .set(DSL.field("crust_id", Integer.class), cartItem.getCrustTypeId() == null ? null : cartItem.getCrustTypeId().intValue())
                .set(DSL.field("sauce_name", String.class), cartItem.getSauceName() == null ? null : cartItem.getSauceName())
                .set(DSL.field("quantity", Integer.class), cartItem.getQuantity())
                .set(DSL.field("price_per", Double.class), cartItem.getPrice())
                .returning(DSL.field("order_item_id", Integer.class))
                .fetchOne();

        Integer orderItemId = inserted == null ? null : inserted.get(DSL.field("order_item_id", Integer.class));
        if (orderItemId == null) {
            logger.error("custom order item id is null");
            throw new RuntimeException("Failed to save custom order item.");
        }

        return orderItemId.longValue();
    }

    @SuppressWarnings("resource")
    public Long saveCustomItemTopping(Long orderItemId, String pizzaHalf, Long toppingId) {
        logger.info("saveCustomItemTopping orderItemId={} pizzaHalf={} toppingId={}", orderItemId, pizzaHalf, toppingId);

        Record inserted = dsl.insertInto(DSL.table("order_custom_item_topping"))
                .set(DSL.field("order_item_id", Integer.class), orderItemId.intValue())
                .set(DSL.field("pizza_half", String.class), pizzaHalf)
                .set(DSL.field("topping_id", Integer.class), toppingId.intValue())
                .returning(DSL.field("custom_topping_id", Integer.class))
                .fetchOne();

        Integer customToppingId = inserted == null ? null : inserted.get(DSL.field("custom_topping_id", Integer.class));
        if (customToppingId == null) {
            logger.error("custom topping id is null");
            throw new RuntimeException("Failed to save custom topping.");
        }

        return customToppingId.longValue();
    }
}
