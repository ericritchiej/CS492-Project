package com.pizzastore.repository;

import com.pizzastore.controller.CrustTypeController;
import com.pizzastore.model.Order;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
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

    @SuppressWarnings("resource")
    public Long findOrCreateAddressId(Long customerId, String deliveryAddress) {
        logger.info("findOrCreateAddressId customerId={}", customerId);

        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            logger.error("deliveryAddress is empty");
            return null;
        }

        Record1<Integer> existing = dsl.select(DSL.field("address_id", Integer.class))
                .from(DSL.table("addresses"))
                .where(DSL.field("customer_id").eq(customerId))
                .and(DSL.field("street_addr_1").eq(deliveryAddress.trim()))
                .fetchOne();

        if (existing != null && existing.value1() != null) {
            logger.info("address already exists");
            return existing.value1().longValue();
        }

        Record inserted = dsl.insertInto(DSL.table("addresses"))
                .set(DSL.field("customer_id"), customerId)
                .set(DSL.field("street_addr_1"), deliveryAddress.trim())
                .set(DSL.field("street_addr_2"), (String) null)
                .set(DSL.field("city"), (String) null)
                .set(DSL.field("state"), (String) null)
                .set(DSL.field("zip_code"), (String) null)
                .returning(DSL.field("address_id", Integer.class))
                .fetchOne();

        Integer addressId = inserted == null ? null : inserted.get(DSL.field("address_id", Integer.class));
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
                .set(DSL.field("customer_id"), order.getCustomerId())
                .set(DSL.field("address_id"), order.getAddressId())
                .set(DSL.field("promotions_id"), order.getPromotionsId())
                .set(DSL.field("employee_id"), order.getEmployeeId())
                .set(DSL.field("order_timestamp"), order.getOrderTimestamp())
                .set(DSL.field("total_amount"), order.getTotalAmount())
                .set(DSL.field("discount_amount"), order.getDiscountAmount())
                .set(DSL.field("status"), order.getStatus())
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
