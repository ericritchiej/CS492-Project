package com.pizzastore.repository;

import com.pizzastore.model.Payment;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {

    private final DSLContext dsl;

    public PaymentRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insertNewPayment(Payment payment) {

        dsl.insertInto(DSL.table("payment_methods"))
                .set(DSL.field("order_id"), payment.getOrderId())
                .set(DSL.field("address_id"), payment.getAddressId())
                .set(DSL.field("cc_number"), payment.getCcNumber())
                .set(DSL.field("exp_month"), payment.getExpMonth())
                .set(DSL.field("exp_year"), payment.getExpYear())
                .set(DSL.field("ccv_number"), payment.getCcvNumber())
                .execute();
    }

}
