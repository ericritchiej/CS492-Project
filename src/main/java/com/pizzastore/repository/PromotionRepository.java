package com.pizzastore.repository;

import com.pizzastore.model.Promotion;
import com.pizzastore.model.User;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class PromotionRepository {

    private final DSLContext dsl;
    private static final Logger logger = LoggerFactory.getLogger(PromotionRepository.class);

    /**
     * Constructor — Spring calls this automatically at startup and injects
     * the DSLContext for us. This is called "Dependency Injection."
     * We store it in a field so all methods in this class can use it.
     */
    public PromotionRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @SuppressWarnings("resource")
    public List<Promotion> findByAllPromotions() {
        logger.info("Finding users all promotions");

        // Build the SELECT query using jOOQ.
        // This is equivalent to the following SQL:
        //   SELECT *
        //   FROM promotions
        //   WHERE exp_dt is null or exp_dt > current date
        //
        return dsl.select(
                        DSL.field("promotion_id"),
                        DSL.field("code"),
                        DSL.field("discount_value"),
                        DSL.field("promotion_desc"),
                        DSL.field("promotion_summary"),
                        DSL.field("exp_dt"),
                        DSL.field("min_order_amt")
                )
                .from(DSL.table("promotions"))
                .where(DSL.field("exp_dt").isNull()
                        .or(DSL.field("exp_dt", LocalDate.class).greaterThan(LocalDate.now()))
                )
                .fetchInto(Promotion.class);
    }

}