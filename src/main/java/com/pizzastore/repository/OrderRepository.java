package com.pizzastore.repository;

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
    public Long findExistingAddressId(Long customerId) {
        String pk = resolveAddressPkColumn();

        Record1<Integer> existing = dsl.select(DSL.field(pk, Integer.class))
                .from(DSL.table("addresses"))
                .where(DSL.field("customer_id").eq(customerId))
                .limit(1)
                .fetchOne();

        if (existing == null || existing.value1() == null) {
            return null;
        }
        return existing.value1().longValue();
    }

    @SuppressWarnings("resource")
    public Long findOrCreateAddressId(Long customerId, String deliveryAddress) {
        logger.info("findOrCreateAddressId customerId={}", customerId);

        ParsedAddress addr = parseAddress(deliveryAddress);
        String pk = resolveAddressPkColumn();

        Record1<Integer> existing = dsl.select(DSL.field(pk, Integer.class))
                .from(DSL.table("addresses"))
                .where(DSL.field("customer_id").eq(customerId))
                .and(DSL.field("street_addr_1").eq(addr.street1))
                .and(DSL.field("city").eq(addr.city))
                .and(DSL.field("state").eq(addr.state))
                .and(DSL.field("zip_code").eq(addr.zip))
                .fetchOne();

        if (existing != null && existing.value1() != null) {
            logger.info("address already exists");
            return existing.value1().longValue();
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

        Integer addressId = inserted == null ? null : inserted.get(DSL.field(pk, Integer.class));
        if (addressId == null) {
            logger.error("address id is null");
            throw new RuntimeException("Failed to create address record.");
        }

        return addressId.longValue();
    }

    @SuppressWarnings("resource")
    public Long save(Order order) {
        logger.info("saveOrder order={}", order);

        Record inserted = dsl.insertInto(DSL.table("orders"))
                .set(DSL.field("customer_id", Integer.class), order.getCustomerId() == null ? null : order.getCustomerId().intValue())
                .set(DSL.field("address_id", Integer.class), order.getAddressId() == null ? null : order.getAddressId().intValue())
                .set(DSL.field("promotion_id", Integer.class), order.getPromotionsId() == null ? null : order.getPromotionsId().intValue())
                .set(DSL.field("employee_id", Integer.class), order.getEmployeeId() == null ? null : order.getEmployeeId().intValue())
                .set(DSL.field("order_timestamp", java.time.LocalDateTime.class), order.getOrderTimestamp())
                .set(DSL.field("total_amount", java.math.BigDecimal.class), order.getTotalAmount())
                .set(DSL.field("discount_amount", java.math.BigDecimal.class), order.getDiscountAmount())
                .set(DSL.field("status", String.class), order.getStatus())
                .returning(DSL.field("order_id", Integer.class))
                .fetchOne();

        Integer orderId = inserted == null ? null : inserted.get(DSL.field("order_id", Integer.class));
        if (orderId == null) {
            logger.error("order id is null");
            throw new RuntimeException("Failed to save order.");
        }

        return orderId.longValue();
    }
}
